/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.brekka.stillingar.core;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.brekka.stillingar.core.GroupConfigurationException.Phase;
import org.brekka.stillingar.core.snapshot.Snapshot;
import org.brekka.stillingar.core.snapshot.SnapshotManager;

/**
 * Standard implementation of {@link UpdatableConfigurationSource} which provides atomic updates to group
 * definitions and adhoc updates to standalone values. 
 * 
 * During the update process, standalone values will be updated first, then the groups. All will be performed in the order they
 * were registered.
 * 
 * @author Andrew Taylor
 */
public class DefaultConfigurationSource implements UpdatableConfigurationSource {
	
    /**
     * Where all snapshots will be obtained from
     */
	private final SnapshotManager snapshotManager;
	
	/**
	 * The group that will contain all of the {@link ValueDefinition}s that were registered via
	 * {@link #register(ValueDefinition, boolean)}.
	 */
	private ValueDefinitionGroup standaloneGroup;

	/**
	 * The list of all value groups including the standalone group above.
	 */
	private List<ValueDefinitionGroup> valueGroups = new ArrayList<ValueDefinitionGroup>();
	
	/**
	 * The current snapshot
	 */
	private Snapshot current;
	
	/**
	 * @param snapshotManager Where all snapshots will be obtained from
	 */
	public DefaultConfigurationSource(SnapshotManager snapshotManager) {
		this.standaloneGroup = new ValueDefinitionGroup("_standalone", new ArrayList<ValueDefinition<?>>(), null, null);
		this.valueGroups.add(standaloneGroup);
		this.snapshotManager = snapshotManager;
	}
	
	/**
	 * Initialise this source, loading the last good or latest snapshot from the manager.
	 */
	public void init() {
		Snapshot initSnapshot = snapshotManager.retrieveLastGood();
		if (initSnapshot == null) {
			// There is no last good.
			initSnapshot = snapshotManager.retrieveLatest();
		}
		current = initSnapshot;
	}
	
	public synchronized void register(ValueDefinition<?> valueDefinition, boolean fireImmediately) {
		ValueChangeAction valueChangeAction = prepareValueChange(valueDefinition, getCurrentSnapshot());
		if (fireImmediately) {
			enactValueChange(valueChangeAction);
		}
		standaloneGroup.getValues().add(valueDefinition);
	}

	public synchronized void register(ValueDefinitionGroup valueDefinitionGroup) {
		GroupChangeAction groupUpdateAction = prepareGroupChange(valueDefinitionGroup, getCurrentSnapshot());
		enactGroupChange(groupUpdateAction);
		valueGroups.add(valueDefinitionGroup);
	}
	
	public <T> T retrieve(Class<T> valueType) {
		return getCurrentSnapshot().retrieve(valueType);
	}
	
	public <T> T retrieve(String expression, Class<T> valueType) {
		return getCurrentSnapshot().retrieve(expression, valueType);
	}
	
	public <T> List<T> retrieveList(Class<T> valueType) {
		return getCurrentSnapshot().retrieveList(valueType);
	}
	
	public <T> List<T> retrieveList(String expression, Class<T> valueType) {
		return getCurrentSnapshot().retrieveList(expression, valueType);
	}
	
	/**
	 * Request that the configuration be updated to a new snapshot
	 */
	public UpdateReport update() {
		UpdateReport report = null;
		Snapshot snapshot = snapshotManager.retrieveLatest();
		if (snapshot != null) {
			List<GroupConfigurationException> groupErrors = new ArrayList<GroupConfigurationException>();
			List<GroupChangeAction> updateActionList = phaseOneUpdate(snapshot, groupErrors);

			// If there are no errors, move on to phase two
			if (groupErrors.isEmpty()) {
				phaseTwoUpdate(updateActionList, groupErrors);
			}
			
			// Still no errors, make the snapshot active, signal acception of the snapshot to the manager.
			if (groupErrors.isEmpty()) {
			    this.current = snapshot;
				snapshotManager.acceptLatest();
			}
			
			report = new UpdateReportImpl(snapshot.getLocation(), groupErrors);
		}
		return report;
	}
	
	/**
	 * 
	 * @param snapshot
	 * @param groupErrors
	 * @return
	 */
	protected List<GroupChangeAction> phaseOneUpdate(Snapshot snapshot, List<GroupConfigurationException> groupErrors) {
		List<ValueDefinitionGroup> valueGroups = this.valueGroups;
		List<GroupChangeAction> updateActionList = new ArrayList<GroupChangeAction>(valueGroups.size());
		
		for (ValueDefinitionGroup valueDefinitionGroup : valueGroups) {
			try {
				GroupChangeAction groupUpdateAction = prepareGroupChange(valueDefinitionGroup, snapshot);
				updateActionList.add(groupUpdateAction);
			} catch (GroupConfigurationException e) {
				groupErrors.add(e);
			}
		}
		return updateActionList;
	}
	
	protected void phaseTwoUpdate(List<GroupChangeAction> updateActionList, List<GroupConfigurationException> groupErrors) {
		// No errors encountered, proceed to the next phase
		for (GroupChangeAction groupUpdateAction : updateActionList) {
			try {
				enactGroupChange(groupUpdateAction);
			} catch (GroupConfigurationException e) {
				groupErrors.add(e);
			}
		}
	}
	
	protected GroupChangeAction prepareGroupChange(
			ValueDefinitionGroup valueDefinitionGroup,
			Snapshot snapshot) {
		List<ValueDefinition<?>> valueDefinitionList = valueDefinitionGroup.getValues();
		List<ValueChangeAction> updateActions = new ArrayList<ValueChangeAction>(valueDefinitionList.size());
		List<ConfigurationException> valueResolveErrors = new ArrayList<ConfigurationException>();
		
		/*
		 * Phase 1 - attempt to retrieve all of the updated values.
		 * Try to resolve every value so that a developer doesn't have to fix errors one-by-one. 
		 * Instead give a summary of all errors encountered.
		 */
		try {
			for (ValueDefinition<?> valueDefinition : valueDefinitionList) {
				ValueChangeAction valueChangeAction = prepareValueChange(valueDefinition, snapshot);
				updateActions.add(valueChangeAction);
			}
		} catch (ConfigurationException e) {
			valueResolveErrors.add(e);
		}
		
		if (!valueResolveErrors.isEmpty()) {
			throw new GroupConfigurationException(valueDefinitionGroup.getName(), 
					Phase.VALUE_DISCOVERY, valueResolveErrors);
		}
		return new GroupChangeAction(valueDefinitionGroup, updateActions);
	}
	
	protected void enactGroupChange(GroupChangeAction groupUpdateAction) {
		ValueDefinitionGroup valueDefinitionGroup = groupUpdateAction.getGroup();
		List<ValueChangeAction> actionList = groupUpdateAction.getActionList();
		List<ConfigurationException> valueUpdateErrors = new ArrayList<ConfigurationException>();
		
		Object semaphore = valueDefinitionGroup.getSemaphore();
		if (semaphore == null) {
			// Semaphore must be set to something
			semaphore = new Object();
		}
		
		/*
		 * Lock on the semaphore, providing an opportunity to atomically update a group of variables.
		 */
		synchronized (semaphore) {
		
			/*
			 * Phase 2 - Make the value updates by calling the listeners.
			 * Make an attempt to update all values, in case 
			 */
			try {
				for (ValueChangeAction valueUpdateAction : actionList) {
					enactValueChange(valueUpdateAction);
				}
			} catch (ConfigurationException e) {
				valueUpdateErrors.add(e);
			}
			
			if (!valueUpdateErrors.isEmpty()) {
				throw new GroupConfigurationException(valueDefinitionGroup.getName(), 
						Phase.VALUE_ASSIGNMENT, valueUpdateErrors);
			}
			
			/*
			 * Phase 3 - We are clear to notify of the change
			 */
			if (valueDefinitionGroup.getChangeListener() != null) {
				try {
					valueDefinitionGroup.getChangeListener().onChange();
				} catch (RuntimeException e) {
					throw new GroupConfigurationException(valueDefinitionGroup.getName(), 
							Phase.LISTENER_INVOCATION, e);
				}
			}
		}
	}

	protected ValueChangeAction prepareValueChange(ValueDefinition<?> valueDefinition, Snapshot snapshot) {
		String expression = valueDefinition.getExpression();
		Class<?> type = valueDefinition.getType();
		Object result;
		if (valueDefinition.isList()) {
			if (expression != null) {
				result = snapshot.retrieveList(expression, type);
			} else {
				result = snapshot.retrieveList(type);
			}
		} else {
			if (expression != null) {
				result = snapshot.retrieve(expression, type);
			} else {
				result = snapshot.retrieve(type);
			}
		}
		return new ValueChangeAction(valueDefinition, result);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void enactValueChange(ValueChangeAction valueChangeAction) {
		ValueDefinition<?> valueDefinition = valueChangeAction.getValueDefinition();
		Object newValue = valueChangeAction.getNewValue();
		ValueChangeListener listener = valueDefinition.getListener();
		try {
			listener.onChange(newValue);
		} catch (RuntimeException e) {
			throw new ValueConfigurationException("value assignment", valueDefinition, e);
		}
	}
	
	/**
	 * Retrieve the currently active snapshot.
	 * @return the active snapshot
	 */
	protected Snapshot getCurrentSnapshot() {
		if (current == null) {
			throw new ConfigurationException("This configuration source has not been initialized.");
		}
		return current;
	}
	
	private class UpdateReportImpl implements UpdateReport {
		private final URL location;
		private final List<GroupConfigurationException> errors;
		
		public UpdateReportImpl(URL location, List<GroupConfigurationException> errors) {
			this.location = location;
			this.errors = errors;
		}
		public URL getLocation() {
			return location;
		}
		public List<GroupConfigurationException> getErrors() {
			return errors;
		}
	}
	
	private class GroupChangeAction {
		public ValueDefinitionGroup getGroup() {
			return group;
		}
		public List<ValueChangeAction> getActionList() {
			return actionList;
		}
		private final ValueDefinitionGroup group;
		private final List<ValueChangeAction> actionList;
		public GroupChangeAction(ValueDefinitionGroup group,
				List<ValueChangeAction> actionList) {
			this.group = group;
			this.actionList = actionList;
		}
		
	}
	
	private class ValueChangeAction {
		private final ValueDefinition<?> valueDefinition;
		private final Object newValue;
		public ValueChangeAction(ValueDefinition<?> valueDefinition,
				Object newValue) {
			this.valueDefinition = valueDefinition;
			this.newValue = newValue;
		}
		public ValueDefinition<?> getValueDefinition() {
			return valueDefinition;
		}
		public Object getNewValue() {
			return newValue;
		}
		
	}
}
