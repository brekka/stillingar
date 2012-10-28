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

package org.brekka.stillingar.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Used to identify a method that will be called when the configuration on a
 * given bean has been updated. This mechanism enables the bean to perform
 * additional processing based on the configuration that can be read either from
 * instance variables or from the parameters of the method itself.
 * 
 * Note that only one method per class can be specified as a configuration
 * listener. Also, only declared methods will actually be picked up by
 * {@link ConfigurationBeanPostProcessor} so if a super type method is to be
 * called, it must be overridden explicitly.
 * 
 * @author Andrew Taylor
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ConfigurationListener {

}
