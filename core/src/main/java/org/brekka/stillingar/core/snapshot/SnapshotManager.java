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

/**
 * TODO
 * 
 * @author Andrew Taylor
 */
public interface SnapshotManager {

    /**
     * Obtain the initial snapshot of the configuration. This must either return a non-null value or throw an exception.
     * 
     * @return the snapshot, never null.
     * @throws NoSnapshotAvailableException
     *             if there is no snapshot available.
     */
    Snapshot retrieveInitial() throws NoSnapshotAvailableException;

    /**
     * When the underlying configuration resource changes, this method should return a new snapshot instance containing
     * the updated configuration source. If the resource becomes unavailable or fails to validate an
     * {@link InvalidSnapshotException} will be thrown.
     * 
     * @return the latest snapshot or null if no change has occurred to the configuration resource.
     * @throws InvalidSnapshotException
     *             when a change to the snapshot is detected, but it is invalid
     */
    Snapshot retrieveUpdated() throws InvalidSnapshotException;

    /**
     * Allows this manager to be informed when a snapshot failed to be loaded. The snapshot manager should not return
     * rejected snapshots in subsequent calls to {@link #retrieveLatest()}.
     * 
     * @param rejectedSnapshot
     *            the snapshot being rejected, which must have been returned by {@link #retrieveLatest()}.
     */
    void reject(Snapshot rejectedSnapshot);
}
