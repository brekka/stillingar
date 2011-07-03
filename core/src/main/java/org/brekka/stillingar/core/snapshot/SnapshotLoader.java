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

import java.net.URL;

/**
 * Loader for snapshots. Implementations will generally be specific to the technology used to implement the snapshot. As
 * such it is the loaders responsibility to take care of encoding issues.
 * 
 * @author Andrew Taylor
 */
public interface SnapshotLoader {

    /**
     * Retrieve the snapshot representation of the URL <code>toLoad</code>. Both the timestamp and URL should be
     * captured within the snapshot so that they can be returned with the corresponding methods in {@link Snapshot}.
     * 
     * @param toLoad
     *            the URL of the resource to load.
     * @param timestamp
     *            the timestamp which should be included within the snapshot.
     * @return the snapshot.
     */
    Snapshot load(URL toLoad, long timestamp);
}
