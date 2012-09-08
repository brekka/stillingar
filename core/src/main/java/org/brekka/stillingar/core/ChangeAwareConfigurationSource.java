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


/**
 * TODO
 * 
 * Registration allows both single value and group notification. Group notifications are intended to be used when a
 * configuration target (such as a bean) has multiple values that need to be updated atomically, optionally specifying a
 * method that will be invoked once all values have been set.
 * 
 * @author Andrew Taylor
 */
public interface ChangeAwareConfigurationSource extends ConfigurationSource {

    /**
     * Register a value definition which will be notified of configuration updates when they occur.
     * 
     * @param valueDef
     *            the value definition which will contain type/expression details and listener which will be called with
     *            updates.
     * @param fireImmediately
     *            determines whether the listener within the value definition should be called prior to control being
     *            returned to the caller.
     */
    void register(ValueDefinition<?> valueDef, boolean fireImmediately);

    /**
     * Register a group of value definitions which will be notified together when any update occurs.
     * 
     * @param valueGroup
     *            the value group to register.
     * @param fireImmediately
     *            determines whether the listener within the group definition should be called prior to control being
     *            returned to the caller.
     */
    void register(ValueDefinitionGroup valueGroup, boolean fireImmediately);
}
