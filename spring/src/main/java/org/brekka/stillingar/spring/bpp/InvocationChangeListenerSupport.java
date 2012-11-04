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

import java.lang.ref.WeakReference;

import org.brekka.stillingar.core.Expirable;
import org.brekka.stillingar.core.ReferentUpdateException;
import org.brekka.stillingar.core.ValueChangeListener;

/**
 * Support class that assists with updating some kind of value, be it a field or method.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
abstract class InvocationChangeListenerSupport<T extends Object> implements ValueChangeListener<T>, Expirable {
    /**
     * The object containing the value to be updated.
     */
    private final WeakReference<Object> targetRef;

    /**
     * The type of the value that is expected.
     */
    private final Class<?> expectedValueType;

    /**
     * Determines whether the value is a list (true if it is)
     */
    private final boolean list;

    /**
     * A label for the type of referent, which will be included in error messages.
     */
    private final String referentTypeLabel;

    /**
     * @param target
     *            The object containing the value to be updated.
     * @param expectedValueType
     *            The type of the value that is expected.
     * @param list
     *            Determines whether the value is a list (true if it is)
     * @param referentTypeLabel
     *            A label for the type of referent, which will be included in error messages.
     */
    public InvocationChangeListenerSupport(Object target, Class<?> expectedValueType, boolean list,
            String referentTypeLabel) {
        this.targetRef = new WeakReference<Object>(target);
        this.expectedValueType = expectedValueType;
        this.list = list;
        this.referentTypeLabel = referentTypeLabel;
    }

    /**
     * Capture the change event, calling {@link #onChange(Object, Object)} with the target object.
     */
    public final void onChange(T newValue, T oldValue) {
        Object target = targetRef.get();
        if (target == null) {
            return;
        }
        onChange(newValue, oldValue, target);
    }
    
    /**
     * Handle a value change.
     * 
     * @param newValue
     *            the new value
     * @param target
     *            that target object being processed.
     */
    protected abstract void onChange(T newValue, T oldValue, Object target);
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.Expirable#isExpired()
     */
    @Override
    public boolean isExpired() {
        return targetRef.isEnqueued();
    }

    /**
     * Generate and throw a {@link ReferentUpdateException} that will encapsulate details about this referent to produce
     * a detailed message about what went wrong.
     * 
     * @param referentName
     *            name The name of the field/method
     * @param value
     *            the new value being updated.
     * @param cause
     *            the underlying cause of the problem.
     */
    protected void throwError(String referentName, Object value, Throwable cause) {
        Class<?> valueType = (value != null ? value.getClass() : null);
        Object target = targetRef.get();
        Class<?> targetClass = null;
        if (target != null) {
            targetClass = target.getClass();
            if (target instanceof OnceOnlyTypeHolder) {
                // Make sure to grab the correct type.
                targetClass = ((OnceOnlyTypeHolder) target).get();
            }
        }
        throw new ReferentUpdateException(referentTypeLabel, referentName, valueType, expectedValueType, list,
                targetClass, cause);
    }
}