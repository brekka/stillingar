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

import org.springframework.core.io.Resource;

/**
 * Defines a directory location from which a configuration file will be loaded.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface BaseDirectory {

    /**
     * The directory based resource from which the caller will resolve other files.
     * 
     * @return the directory resource. If no resource is available, return an instance of {@link UnresolvableResource}
     *         with a description of why it is not available.
     */
    Resource getDirResource();

    /**
     * Human-readable label for this directory location that will be displayed in error messages.
     * 
     * @return the label describing this location so it can be singled out against other failed locations.
     */
    String getDisposition();
}
