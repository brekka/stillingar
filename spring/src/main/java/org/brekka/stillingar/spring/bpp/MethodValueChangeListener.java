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

import org.springframework.context.Lifecycle;

/**
 * 
 * TODO Description of MethodValueChangeListener
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
class MethodValueChangeListener<T extends Object> extends InvocationChangeListenerSupport<T> {
    
	private final Method method;
	
	public MethodValueChangeListener(Method method, Object target, Class<?> expectedValueType, boolean list) {
		super(target, expectedValueType, list, "Method");
		this.method = method;
	}

	public void onChange(T newValue, Object target) {
	    // Attempt to locate getter to perform lifecycle check
	    
	    lifecycleStop(target);
		try {
			method.invoke(target, newValue);
			if (target instanceof Lifecycle) {
                ((Lifecycle) target).start();
            }
		} catch (IllegalAccessException e) {
			throwError(method.getName(), newValue, e);
		} catch (InvocationTargetException e) {
			throwError(method.getName(), newValue, e);
		}
	}

	/**
	 * Attempt to stop the current value of the property if it is set
	 * and an instance oflifecycle.
	 * @param target
	 */
    void lifecycleStop(Object target) {
        String name = method.getName();
	    String getter = name.replaceFirst("set", "get");
	    try {
            Method getterMethod = method.getDeclaringClass().getMethod(getter);
            Object currVal = getterMethod.invoke(target);
            if (currVal instanceof Lifecycle) {
                ((Lifecycle) currVal).stop();
            }
        } catch (SecurityException e) {
            // Ignore
        } catch (NoSuchMethodException e) {
            // Ignore
        } catch (IllegalArgumentException e) {
            // Ignore
        } catch (IllegalAccessException e) {
            // Ignore
        } catch (InvocationTargetException e) {
            // Ignore
        }
    }
}