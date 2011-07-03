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

/**
 * Allows customisation of the resource names that will be loaded.
 * 
 * @author Andrew Taylor
 */
public interface ResourceNaming {

    /**
     * Compose the full name of the 'original' resource that will be resolved within the configuration base location.
     * The original name will be what the administrator will edit to update the values.
     * 
     * @return the original configuration resource name.
     */
    String prepareOriginalName();

    /**
     * The name of the 'last good' file that will be generated when the 'original' is successfully loaded. This will
     * reside in the same configuration base location as the original so cannot have the same name.
     * 
     * @return the 'last good' configuration resource name.
     */
    String prepareLastGoodName();
}
