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

package org.brekka.stillingar.spring.resource;

import java.util.Set;

/**
 * Allows customisation of the resource names that will be loaded.
 * 
 * @author Andrew Taylor
 */
public interface ResourceNameResolver {

    /**
     * The set of possible resource names, in order of specificity. That is, the more complex and less likely names
     * appear first in decreasing order of complexity.
     * 
     * @return the ordered set of names.
     */
    Set<String> getNames();
}
