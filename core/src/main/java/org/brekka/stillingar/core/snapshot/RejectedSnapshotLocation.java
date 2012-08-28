/*
 * Copyright 2012 the original author or authors.
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
 * Details about a snapshot location that was rejected as a potential location to resolve a snapshot resource. A
 * location is essentially a directory that will be searched for a snapshot file.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface RejectedSnapshotLocation {

    /**
     * A label that identifies the disposition of the location such as 'User home' or 'Application container
     * configuration'
     * 
     * @return
     */
    String getDisposition();

    /**
     * The fully qualified path of the location being searched for snapshots.
     * 
     * @return
     */
    String getPath();

    /**
     * The reason the snapshot was rejected.
     * 
     * @return
     */
    String getMessage();

}
