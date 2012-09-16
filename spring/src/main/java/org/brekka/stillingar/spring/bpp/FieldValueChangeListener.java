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

import java.lang.reflect.Field;

import org.springframework.context.Lifecycle;

/**
 * Change listener that will use reflection to update a specific field of a bean.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
class FieldValueChangeListener<T extends Object> extends InvocationChangeListenerSupport<T> {

    /**
     * The field being updated
     */
    private final Field field;

    /**
     * 
     * @param field
     *            the field being updated
     * @param target
     *            The object containing the field being updated.
     * @param expectedValueType
     *            The type of the value that is expected.
     * @param list
     *            Determines whether the value is a list (true if it is)
     */
    public FieldValueChangeListener(Field field, Object target, Class<?> expectedValueType, boolean list) {
        super(target, expectedValueType, list, "Field");
        this.field = field;
    }

    /**
     * Set the value of the field encapsulated by this listener.
     */
    public void onChange(T newValue, Object target) {
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            Object existing = field.get(target);
            if (existing instanceof Lifecycle) {
                ((Lifecycle) existing).stop();
            }
            field.set(target, newValue);
            if (target instanceof Lifecycle) {
                ((Lifecycle) target).start();
            }
        } catch (IllegalAccessException e) {
            throwError(field.getName(), newValue, e);
        }
    }
}