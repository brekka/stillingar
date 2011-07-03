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
import java.util.List;

/**
 * Details the outcome of an operation to update an {@link UpdatableConfigurationSource}. In an effort to make resolving
 * configuration errors less iterative, 'Stillingar' will attempt to enact as many configuration changes as possible,
 * and then return all of the errors in this report. It is then up to the caller what happens to this report but it will
 * most likely be written out to an error log. The developer should then be able to resolve the majority of errors
 * before attempting to re-run the system being configured.
 * 
 * @author Andrew Taylor
 */
public interface UpdateReport {

    /**
     * The location where the file was loaded from.
     * 
     * @return the URL
     */
    URL getLocation();

    /**
     * The list of errors encountered (if any)
     * 
     * @return a possibly empty list of errors.
     */
    List<GroupConfigurationException> getErrors();

}
