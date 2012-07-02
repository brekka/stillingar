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

package org.brekka.stillingar;

import static java.lang.String.format;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * <p>
 * Creates placeholders for interface based instance variables. Intended for use
 * in singleton classes that expect injected dependencies. Should any method of
 * a placeholder be invoked, an {@link IllegalStateException} will be thrown
 * providing descriptive information about the problem (particularly if the name
 * of the field is supplied).
 * </p>
 * <p>
 * Placeholder instances are internally implemented using the Java {@link Proxy}
 * mechanism. Under normal circumstances, the placeholder instance will be very
 * short-lived being freed the moment the real dependency is injected. It will
 * however add a small overhead to the application startup time, particularly
 * as the stack is inspected to identify the caller.
 * </p>
 * 
 * @author Andrew Taylor
 */
public final class Placeholder {
	/**
	 * At what location in the stack trace should we expect to find details
	 * about where the placeholder was called.
	 */
	private static final int TARGET_DEPTH = 3;
	
	/**
	 * Create a new placeholder for the type <code>valueType</code> which must
	 * be an interface. When any method of the returned instance is invoked, an
	 * {@link IllegalStateException} will be thrown whose message indicates the
	 * expected type and location where the placeholder was instantiated.
	 * 
	 * @param <T>
	 *            the type of the placeholder that will be returned
	 * @param valueType
	 *            the interface type to generate a placeholder for.
	 * @return the new placeholder instance.
	 * @throws IllegalArgumentException
	 *             if <code>valueType</code> is null or not an interface.
	 */
	public static <T> T of(Class<T> valueType) {
		return ofInternal(valueType, null);
	}
	
	/**
	 * Create a new placeholder for the type <code>valueType</code> which must
	 * be an interface. The parameter <code>fieldName</code> should contain the
	 * name of the instance variable this placeholder is being applied to. When
	 * any method of the returned instance is invoked, an
	 * {@link IllegalStateException} will be thrown whose message indicates the
	 * expected type and location where the placeholder was instantiated.
	 * 
	 * @param <T>
	 *            the type of the placeholder that will be returned
	 * @param valueType
	 *            the interface type to generate a placeholder for.
	 * @param fieldName
	 *            the name of the instance variable this placeholder will be
	 *            assigned to.
	 * @return the new placeholder instance.
	 * @throws IllegalArgumentException
	 *             if <code>valueType</code> is null or not an interface.
	 */
	public static <T> T of(final Class<T> valueType, final String fieldName) {
		return ofInternal(valueType, fieldName);
	}
	
	/**
	 * Determine whether the <code>value</code> parameter is a placeholder.
	 * @param value the instance to check
	 * @return true if <code>value</code> is a placeholder value, false otherwise.
	 */
	public static boolean isPlaceholder(Object value) {
		return (Proxy.getInvocationHandler(value) instanceof PlaceholderInvocationHandler);
	}

	/**
	 * Performs the actual work of creating the placeholder. Note that the order
	 * of execution is important to maintain the correct stack depth defined by
	 * {@link #TARGET_DEPTH}.
	 */
	@SuppressWarnings("unchecked")
	private static <T> T ofInternal(final Class<T> valueType, final String fieldName) {
		// Validate inputs
		if (valueType == null) {
			throw new IllegalArgumentException("No type specified for this placeholder.");
		}
		
		if (!valueType.isInterface()) {
			throw new IllegalArgumentException(format(
					"Can only create placeholders for interfaces, the type '%s' is not.",
					valueType.getName()));
		}
		
		String type = "unknown";
		int line = 0;
		StackTraceElement[] stackTraceArr = Thread.currentThread().getStackTrace();
		if (stackTraceArr != null 
				&& stackTraceArr.length > TARGET_DEPTH) {
			type = stackTraceArr[TARGET_DEPTH].getClassName();
			line = stackTraceArr[TARGET_DEPTH].getLineNumber();
		}
		
		ClassLoader cl = valueType.getClassLoader();
		cl = (cl != null ? cl : Thread.currentThread().getContextClassLoader());
		
		PlaceholderInvocationHandler handler = new PlaceholderInvocationHandler(type, line, fieldName, valueType);
		return (T) Proxy.newProxyInstance(cl, new Class[] { valueType }, handler);
	}
	
	/**
	 * When any method of the placeholder instance is called, the
	 * {@link #invoke(Object, Method, Object[])} method will handle it, throwing
	 * an {@link IllegalStateException} with details of the location where the
	 * placeholder was created.
	 */
	private static class PlaceholderInvocationHandler implements
			InvocationHandler {
		private final String type;
		private final int line;
		private final String fieldName;
		private final Class<?> valueType;
		
		/**
		 * The exception, cached in case multiple calls are made.
		 */
		private IllegalStateException exception;
		
		public PlaceholderInvocationHandler(String type, int line, String fieldName, Class<?> valueType) {
			this.type = type;
			this.line = line;
			this.fieldName = fieldName;
			this.valueType = valueType;
		}

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			if (exception == null) {
				if (fieldName != null) {
					exception = new IllegalStateException(format(
							"Instance variable '%s' <%s> at line %d of class '%s' has not yet been set", 
							fieldName, valueType.getName(), line, type));
				} else {
					exception = new IllegalStateException(format(
							"Instance variable <%s> at line %d of class '%s' has not yet been set", 
							valueType.getName(), line, type));
				}
			}
			throw exception;
		}
	}
}
