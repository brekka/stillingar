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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Change listener that will use reflection to update a specific method of a bean.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
class MethodValueChangeListener<T extends Object> extends InvocationChangeListenerSupport<T> {

    /**
     * The method being updated
     */
    private final Method method;

    /**
     * @param method
     *            The method being updated
     * @param target
     *            The object containing the method being updated.
     * @param expectedValueType
     *            The type of the value that is expected.
     * @param list
     *            Determines whether the value is a list (true if it is)
     */
    public MethodValueChangeListener(Method method, Object target, Class<?> expectedValueType, boolean list) {
        super(target, expectedValueType, list, "Method");
        this.method = method;
    }

    /**
     * Use reflection to invoke the setter with the new value on the target object.
     */
    @Override
    public void onChange(T newValue, T oldValue, Object target) {
        try {
            method.invoke(target, newValue);
        } catch (IllegalAccessException e) {
            throwError(method.getName(), newValue, e);
        } catch (InvocationTargetException e) {
            throwError(method.getName(), newValue, e);
        }
    }
}
