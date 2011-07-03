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

package org.brekka.stillingar.core.snapshot;

import org.brekka.stillingar.core.ConfigurationSource;

/**
 * Responsible for loading and managing configuration snapshots for a single {@link ConfigurationSource}. Defines a
 * mechanism for preserving the last good configuration snapshot that will be used whenever the system is started.
 * 
 * The goal of the 'last good' mechanism is to avoid the situation where an administrator has updated the configuration
 * but failed to notice that it was invalid. The consequence being that when the system restarts, it would fail due to
 * the invalid configuration.
 * 
 * When the configuration subsystem starts up, it will call {@link #retrieveLastGood()} to obtain the bootstrap
 * configuration. If it returns null, then {@link #retrieveLatest()} will be called. Subsequent calls should always be
 * to {@link #retrieveLatest()}.
 * 
 * If the snapshot resolved by {@link #retrieveLatest()} is deemed to be valid then {@link #acceptLatest()} will be
 * called to indicate that the last snapshot retrieved is good and should be used as the 'last good' on the next system
 * restart.
 * 
 * @author Andrew Taylor
 */
public interface SnapshotManager {

    /**
     * Retrieve the configuration that was last successfully loaded. If there is no last good configuration, then return
     * null.
     * 
     * @return a snapshot of the last good configuration, or null if there is none available.
     */
    Snapshot retrieveLastGood();

    /**
     * Retrieve the latest snapshot of the configuration, but only if it has changed since the last invocation. If no
     * change has occurred, just return null.
     * 
     * @return potentially the latest snapshot, or null if it has not changed since the last call.
     */
    Snapshot retrieveLatest();

    /**
     * Signal that the latest configuration returned by {@link #retrieveLatest()} was processed correctly, so the file
     * should be captured in a 'last good' file. What happens to the previous last good is left up the implementation
     * that should either overwrite or archive it. Should only be called after {@link #retrieveLatest()}.
     */
    void acceptLatest();
}
