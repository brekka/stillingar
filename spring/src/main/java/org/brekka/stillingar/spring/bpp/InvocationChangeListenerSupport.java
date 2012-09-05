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

import org.brekka.stillingar.core.ReferentUpdateException;
import org.brekka.stillingar.core.ValueChangeListener;

/**
 * 
 * TODO Description of InvocationChangeListenerSupport
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
abstract class InvocationChangeListenerSupport<T extends Object> implements ValueChangeListener<T> {
	private final Object target;
	private final Class<?> expectedValueType;
	private final boolean list;
	private final String type;
	public InvocationChangeListenerSupport(Object target,
			Class<?> expectedValueType, boolean list, String type) {
		this.target = target;
		this.expectedValueType = expectedValueType;
		this.list = list;
		this.type = type;
	}
	
	public final void onChange(T newValue) {
	    onChange(newValue, target);
    }
	
	public abstract void onChange(T newValue, Object target);
	
	protected void throwError(String name, Object value, Throwable cause) {
		Class<?> valueType = (value != null ? value.getClass() : null);
		Class<?> targetClass = target.getClass();
		if (target instanceof TargetClass) {
		    targetClass = ((TargetClass) target).get();
		}
		throw new ReferentUpdateException(type, name, valueType, expectedValueType, list, targetClass, cause);
	}
}