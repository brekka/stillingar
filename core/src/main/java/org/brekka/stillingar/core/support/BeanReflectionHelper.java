/*
 * Copyright 2014 the original author or authors.
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

package org.brekka.stillingar.core.support;

import static java.lang.String.format;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.brekka.stillingar.api.ValueConfigurationException;

/**
 * Use reflection to find instances of a given type on the specified bean.
 *
 * @author Andrew Taylor
 */
public class BeanReflectionHelper {
    protected final Object bean;

    /**
     * @param bean
     */
    public BeanReflectionHelper(Object bean) {
        this.bean = bean;
    }

    /**
     * Determine if one or more instances of the specified class can be found somewhere in the bean property tree.
     * 
     * @param lookingFor
     *            the type to look for
     * @return true if at least one value is found
     */
    public boolean isAvailable(Class<?> lookingFor) {
        Map<Object, Void> seen = new IdentityHashMap<Object, Void>();
        try {
            return collect(bean, lookingFor, seen, null);
        } catch (IllegalStateException e) {
            throw new ValueConfigurationException(format(
                    "Finding all values of the requested type under fields of the class '%s'", bean.getClass()
                            .getName()), lookingFor, null, e);
        }
    }

    /**
     * Locate a single instance of the specified {@link Class}.
     * 
     * @param valueType
     *            the class to look for
     * @return the value of this type or null if it cannot be found.
     * @throws ValueConfigurationException
     *             if more than one instance of the specified Class is found.
     */
    public <T> T findValueOf(Class<T> valueType) {
        T retVal;
        List<T> values = new ArrayList<T>();
        collect(bean, valueType, values);
        if (values.size() == 0) {
            retVal = null;
        } else if (values.size() == 1) {
            retVal = values.get(0);
        } else {
            throw new ValueConfigurationException(format("Expected a single value, found %d", values.size()),
                    valueType, null);
        }
        return retVal;
    }

    /**
     * Find all instances of the specified type.
     * 
     * @param valueType
     *            the class to look for instances of.
     * @return the list of values found.
     */
    public <T> List<T> findListOf(Class<T> valueType) {
        List<T> values = new ArrayList<T>();
        collect(bean, valueType, values);
        return values;
    }
    
    @SuppressWarnings("unused")
    protected boolean acceptClass(final Class<?> clazz) {
        return true;
    }
    
    @SuppressWarnings("unused")
    protected boolean acceptField(final Field field) {
        return true;
    }

    protected <T> boolean collect(Object current, Class<T> lookingFor, List<T> values) {
        Map<Object, Void> seen = new IdentityHashMap<Object, Void>();
        try {
            return collect(current, lookingFor, seen, values);
        } catch (IllegalStateException e) {
            throw new ValueConfigurationException(
                    format("Looking for requested value type field on the JAXB model class '%s'", current.getClass()
                            .getName()), lookingFor, null, e);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> boolean collect(Object current, Class<T> lookingFor, Map<Object, Void> seen, List<T> values) {
        if (current == null) {
            return false;
        }
        if (seen.containsKey(current)) {
            return false;
        }
        Class<? extends Object> currentClass = current.getClass();
        if (currentClass == lookingFor) {
            if (values != null) {
                values.add((T) current);
            }
            return true;
        }
        seen.put(current, null);
        
        if (!acceptClass(currentClass)) {
            return false;
        }

        Field[] declaredFields = current.getClass().getDeclaredFields();
        boolean found = false;
        for (Field field : declaredFields) {
            if (!acceptField(field)) {
                continue;
            }
            Object fieldValue = extractFieldValue(current, field);
            boolean result = collect(fieldValue, lookingFor, seen, values);
            if (result && values == null) {
                return true;
            }
            found |= result;
        }
        return found;
    }
    
    /**
     * @param current
     * @param field
     * @return
     */
    public static Object extractFieldValue(Object current, Field field) {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        Object next;
        try {
            next = field.get(current);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(format("Unable to access the value of field '%s' of object with type '%s'",
                    field.getName(), current.getClass().getName()), e);
        }
        return next;
    }
}
