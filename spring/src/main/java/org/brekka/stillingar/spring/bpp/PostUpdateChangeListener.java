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

import static java.lang.String.format;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.brekka.stillingar.core.ConfigurationException;
import org.brekka.stillingar.core.ConfigurationSource;
import org.brekka.stillingar.core.Expirable;
import org.brekka.stillingar.core.GroupChangeListener;

/**
 * Invoke a method of a target object in response to a group change.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
class PostUpdateChangeListener implements GroupChangeListener, Expirable {
    /**
     * The target object containing the method to be invoked
     */
    private final WeakReference<Object> targetRef;

    /**
     * The method to invoke
     */
    private final WeakReference<Method> methodRef;

    /**
     * Value resolvers for the parameters of the method.
     */
    private final List<ParameterValueResolver> parameterValues;

    /**
     * @param target
     *            The target object containing the method to be invoked
     * @param method
     *            The method to invoke
     * @param parameterValues
     *            Value resolvers for the parameters of the method.
     */
    public PostUpdateChangeListener(Object target, Method method, List<ParameterValueResolver> parameterValues) {
        this.targetRef = new WeakReference<Object>(target);
        this.methodRef = new WeakReference<Method>(method);
        this.parameterValues = parameterValues;
    }

    /**
     * Delegates to {@link #onChange(ConfigurationSource, Object)} along with <code>target</code>
     */
    public final void onChange(ConfigurationSource configurationSource) {
        Object target = targetRef.get();
        Method method = methodRef.get();
        if (target == null || method == null) {
            return;
        }
        onChange(configurationSource, target, method);
    }

    /**
     * @param the
     *            source that can be used to lookup additional values if necessary.
     * @param target
     *            the target object on which the specified method will be invoked.
     */
    protected void onChange(ConfigurationSource configurationSource, Object target, Method method) {
        Object[] args = new Object[parameterValues.size()];
        for (int i = 0; i < parameterValues.size(); i++) {
            ParameterValueResolver arg = parameterValues.get(i);
            args[i] = arg.getValue();
        }
        try {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            method.invoke(target, args);
        } catch (IllegalAccessException e) {
            throwError(args, e, method);
        } catch (InvocationTargetException e) {
            throwError(args, e, method);
        } catch (IllegalArgumentException e) {
            throwError(args, e, method);
        }
    }
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.Expirable#isExpired()
     */
    @Override
    public boolean isExpired() {
        return (targetRef.isEnqueued() || methodRef.isEnqueued());
    }

    /**
     * Throws a new {@link ConfigurationException}, encapsulating information about the context of the object/method
     * being invoked.
     * 
     * @param args
     *            the values being passed to the method.
     * @param cause
     *            the underlying cause of the problem.
     */
    protected void throwError(Object[] args, Throwable cause, Method method) {
        throw new ConfigurationException(format("Listener method '%s' of type '%s' with arguments %s",
                method.getName(), method.getDeclaringClass().getName(), Arrays.toString(args)), cause);
    }

    /**
     * Retrieve the method that will be invoked.
     * 
     * @return the method
     */
    public Method getMethod() {
        return methodRef.get();
    }
}