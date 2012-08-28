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

package org.brekka.stillingar.spring.resource;

import org.brekka.stillingar.core.snapshot.NoSnapshotAvailableException;
import org.springframework.core.io.Resource;

/**
 * Select the resource which will be used to load snapshots from.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface ResourceSelector {

    /**
     * The resource selected by this implementation.
     * @return the resource
     */
    Resource getResource() throws NoSnapshotAvailableException;
}
