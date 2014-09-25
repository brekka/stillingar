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
import java.util.List;

/**
 * <p>
 * Multi-purpose annotation used by {@link ConfigurationBeanPostProcessor} to apply configuration based post processing
 * to container managed beans.
 * </p>
 * <p>
 * The following table identifies the purpose of this annotation when used in a given context.
 * </p>
 * <table>
 * <tr>
 * <th>Target</th>
 * <th>Purpose</th>
 * </tr>
 * <tr>
 * <td>Type</td>
 * <td>When applied at the class level, it identifies the given type as requiring configuration. This is an optimisation
 * that allows the configuration bean post processor to completely ignore beans that do not require configuration. In
 * this context the value does nothing.</td>
 * </tr>
 * <tr>
 * <td>Field</td>
 * <td>Identifies a field as needing to be configured. The value of the annotation is an optional expression that
 * identifies what configuration value should be injected. If the type of the field is unique within the configuration,
 * or the field is a {@link List}, then it is not necessary to include an expression.</td>
 * </tr>
 * <tr>
 * <td>Method</td>
 * <td>Identifies a bean setter method as needing to be configured. Setter based configuration should be used when a
 * security model prevents field injection. The method must have a single parameter, and no return type. The value of
 * the annotation is an optional expression that identifies what configuration value should be injected. If the type of
 * the method is unique within the configuration, or the field is a {@link List}, then it is not necessary to include an
 * expression.</td>
 * </tr>
 * <tr>
 * <td>Parameter</td>
 * <td>Can be used on the parameters of configuration listener methods (those annotated with
 * {@link ConfigurationListener}), to identify a parameter as a configuration value that needs to be resolved. Otherwise
 * behaves just like a field declaration, supporting list types and expressions.</td>
 * </tr>
 * </table>
 * 
 * @author Andrew Taylor
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
public @interface Configured {

    /**
     * When used on a method, field or parameter, this value allows an optional expression to be defined which will be
     * used to resolve the correct value to set on the target. When used on a type, this value does nothing
     * 
     * @return the expression to use.
     */
    String value() default "";

    /**
     * Determine whether this field/parameter/method must be set to a value, failing if no value can be found.
     * 
     * @return whether this is required or not
     */
    boolean required() default true;
}
