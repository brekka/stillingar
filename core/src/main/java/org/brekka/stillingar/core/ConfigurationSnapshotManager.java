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

/**
 * Responsible for loading and managing configuration snapshots for a single
 * {@link ConfigurationSource}. TODO more detail - what method can be called and
 * when.
 * 
 * @author Andrew Taylor
 */
public interface ConfigurationSnapshotManager {

	/**
	 * Retrieve the configuration that was last successfully loaded. If there is
	 * no last good configuration, then return null.
	 * 
	 * @return a snapshot of the last good configuration, or null if there is
	 *         none available.
	 */
	ConfigurationSnapshot retrieveLastGood();

	/**
	 * Retrieve the latest snapshot of the configuration, but only if it has
	 * changed since the last invocation. If no change has occurred, just return
	 * null.
	 * 
	 * @return potentially the latest snapshot, or null if it has not changed
	 *         since the last call.
	 */
	ConfigurationSnapshot retrieveLatest();

	/**
	 * Signal that the latest configuration returned by
	 * {@link #retrieveLatest()} was processed correctly, so the file should be
	 * captured in a 'last good' file. What happens to the previous last good is
	 * left up the implementation that should either overwrite or archive it.
	 * Should only be called after {@link #retrieveLatest()}.
	 */
	void acceptLatest();
}
