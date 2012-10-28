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

package org.brekka.stillingar.spring.bpp;

import org.brekka.stillingar.api.ConfigurationSource;

/**
 * Used to resolve the value that should be set for a method parameter used when calling a configuration listener
 * method. Implementations could for example, retrieve the value from the {@link ConfigurationSource} or from the spring
 * context itself.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
interface ParameterValueResolver {

    /**
     * Retrieve the value that should be assigned to a method parameter.
     * 
     * @return the value.
     */
    Object getValue();
}