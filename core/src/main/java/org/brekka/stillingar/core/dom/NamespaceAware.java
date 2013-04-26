/*
 * Copyright 2013 the original author or authors.
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

package org.brekka.stillingar.core.dom;

/**
 * Identifies an class as being able to receive namespace registrations.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface NamespaceAware {

    /**
     * Assign the namespace URI to the given prefix.
     * 
     * @param prefix
     *            the prefix to assign the namespace to
     * @param uri
     *            the namespace to assign
     * @throws IllegalArgumentException
     *             if a different URI is already registered for the given prefix.
     */
    void registerNamespace(String prefix, String uri);
}
