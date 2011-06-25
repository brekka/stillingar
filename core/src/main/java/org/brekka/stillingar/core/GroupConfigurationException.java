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

import static java.lang.String.format;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class GroupConfigurationException extends RuntimeException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -4249124307775439267L;
	
	private final String groupName;
	
	private final Phase phase;
	
	private List<ConfigurationException> errorList = Collections.emptyList();

	public GroupConfigurationException(String groupName, Phase phase, Throwable cause) {
		super(cause);
		this.phase = phase;
		this.groupName = groupName;
	}

	public GroupConfigurationException(String groupName, Phase phase, List<ConfigurationException> errors) {
		super();
		this.groupName = groupName;
		this.phase = phase;
		this.errorList = errors;
	}

	@Override
	public String getMessage() {
		String message;
		switch (phase) {
			case LISTENER_INVOCATION:
				message = format("Failed to invoke listener method for group '%s'", groupName);
				break;
			case VALUE_ASSIGNMENT:
				message = format("Configuration group '%s' encountered %d errors during value assignment: %s", 
						groupName, errorList.size(), combineValueErrors());
				break;
			case VALUE_DISCOVERY:
				message = format("Configuration group '%s' encountered %d errors during value discovery: %s", 
						groupName, errorList.size(), combineValueErrors());
				break;
			default:
				message = format("Configuration group '%s' - unknown phase type '%s'", groupName, phase);
				break;
		}
		return message;
	}
	
	private String combineValueErrors() {
		StringBuilder sb = new StringBuilder();
		sb.append(" { ");
		int cnt = 1;
		for (Iterator<ConfigurationException> iterator = errorList.iterator(); iterator.hasNext();) {
			ConfigurationException e = iterator.next();
			sb.append("[");
			sb.append(cnt++);
			sb.append("] '");
			sb.append(e.getLocalizedMessage());
			sb.append("' (");
			sb.append(e.getClass().getName());
			sb.append(")");
			Throwable cause = e.getCause();
			while (cause != null) {
				sb.append(" caused by '");
				sb.append(cause.getLocalizedMessage());
				sb.append("' (");
				sb.append(cause.getClass().getName());
				sb.append(")");
				cause = cause.getCause();
			}
			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append(" }");
		return sb.toString();
	}

	public List<ConfigurationException> getErrorList() {
		return errorList;
	}
	
	public String getGroupName() {
		return groupName;
	}
	
	public static enum Phase {
		/**
		 * Phase 1 - value discovery
		 */
		VALUE_DISCOVERY,
		
		/**
		 * Phase 2 - value assignment
		 */
		VALUE_ASSIGNMENT,
		
		/**
		 * Phase 3 - listener invocation
		 */
		LISTENER_INVOCATION,
	}
}
