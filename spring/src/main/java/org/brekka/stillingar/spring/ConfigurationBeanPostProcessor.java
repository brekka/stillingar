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

package org.brekka.stillingar.spring;

import static java.lang.String.format;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.brekka.stillingar.annotations.ConfigurationListener;
import org.brekka.stillingar.annotations.Configured;
import org.brekka.stillingar.core.ConfigurationException;
import org.brekka.stillingar.core.GroupChangeListener;
import org.brekka.stillingar.core.PropertyUpdateException;
import org.brekka.stillingar.core.UpdatableConfigurationSource;
import org.brekka.stillingar.core.ValueChangeListener;
import org.brekka.stillingar.core.ValueDefinition;
import org.brekka.stillingar.core.ValueDefinitionGroup;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 
 * 
 * @author Andrew Taylor
 */
public class ConfigurationBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {

	private BeanFactory beanFactory;
	
	private UpdatableConfigurationSource configurationSource;
	
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		Class<? extends Object> beanClass = bean.getClass();
		
		if (beanClass.getAnnotation(Configured.class) != null) {
			
			boolean prototype = beanFactory.isPrototype(beanName);
			
			if (prototype) {
				processPrototype(bean, beanName);
			} else {
				processSingleton(bean, beanName);
			}
			
		}
		return bean;
	}
	
	protected void processPrototype(Object bean, String beanName) {
		/*
		 * TODO Prototype support
		 * 
		 * Will not support updates - just @Configured on fields and setters. Will make a direct call
		 * to configurationSource to fetch current values.
		 */
	}

	protected void processSingleton(Object bean, String beanName) {
		Class<? extends Object> beanClass = bean.getClass();
		
		List<ValueDefinition<?>> valueList = new ArrayList<ValueDefinition<?>>();
		
		PostUpdateChangeListener beanChangeListener = null;
		
		Field[] declaredFields = beanClass.getDeclaredFields();
		for (Field field : declaredFields) {
			processField(field, valueList, bean);
		}
		
		Method[] declaredMethods = beanClass.getDeclaredMethods();
		for (Method method : declaredMethods) {
			Configured configured = method.getAnnotation(Configured.class);
			
			ConfigurationListener configurationListener = method.getAnnotation(ConfigurationListener.class);
			if (configurationListener != null) {
				if (beanChangeListener != null) {
					throw new ConfigurationException(format(
							"Unable to create a configuration listener for the method '%s' " +
							"as it already contains a configuration listener on the method '%s'",
							method, beanName, bean.getClass().getName(), beanChangeListener.method));
				}
				beanChangeListener = processListenerMethod(method, valueList, bean);
				
			} else if (configured != null) {
				processSetterMethod(configured, method, valueList, bean);
			}
		}
		ValueDefinitionGroup group = new ValueDefinitionGroup(beanName, valueList, beanChangeListener);
		configurationSource.register(group);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void processField(Field field, List<ValueDefinition<?>> valueList, Object bean) {
		Configured annotation = field.getAnnotation(Configured.class);
		if (annotation != null) {
			Class type = field.getType();
			boolean list = false;
			if (type == List.class) {
				type = listType(field.getGenericType());
				list = true;
			}
			FieldValueChangeListener<Object> listener = new FieldValueChangeListener<Object>(field, bean, type, list);
			ValueDefinition<Object> value = new ValueDefinition<Object>(type, annotation.value(), listener, list);
			valueList.add(value);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void processSetterMethod(Configured configured, Method method, List<ValueDefinition<?>> valueList, Object bean) {
		Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes.length != 1) {
			throw new ConfigurationException(format("The method '%s' does not appear to be a setter. " +
					"A bean setter method should take only a single parameter.", method));
		}
		Class type = parameterTypes[0];
		boolean list = false;
		if (type == List.class) {
			Type[] genericParameterTypes = method.getGenericParameterTypes();
			type = listType(genericParameterTypes[0]);
			list = true;
		}
		MethodValueChangeListener<Object> listener = new MethodValueChangeListener<Object>(method, bean, type, list);
		ValueDefinition<Object> value = new ValueDefinition<Object>(type, configured.value(), listener, list);
		valueList.add(value);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected PostUpdateChangeListener processListenerMethod(Method method, List<ValueDefinition<?>> valueList, Object bean) {
		Class<?>[] parameterTypes = method.getParameterTypes();
		Type[] genericParameterTypes = method.getGenericParameterTypes();
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		List<ValueResolver> argList = new ArrayList<ValueResolver>();
		for (int i = 0; i < parameterTypes.length; i++) {
			ValueResolver arg = null;
			Annotation[] annotations = parameterAnnotations[i];
			Class type = parameterTypes[i];
			boolean list = false;
			if (type == List.class) {
				type = listType(genericParameterTypes[i]);
				list = true;
			}
			Qualifier qualifier = null;
			for (Annotation annotation : annotations) {
				if (annotation instanceof Configured) {
					Configured paramConfigured = (Configured) annotation;
					MethodParameterListener mpl = new MethodParameterListener();
					ValueDefinition<Object> value = new ValueDefinition<Object>(type, paramConfigured.value(), mpl, list);
					valueList.add(value);
					arg = mpl;
					break;
				} else if (annotation instanceof Qualifier) {
					qualifier = (Qualifier) annotation;
				}
			}
			if (arg == null) {
				if (qualifier != null) {
					try {
						beanFactory.getBean(qualifier.value(), type);
						arg = new BeanReferenceResolver(qualifier, type);
					} catch (NoSuchBeanDefinitionException e) {
						throw new ConfigurationException(format(
								"Listener method '%s' parameter %d is not marked as %s and no bean " +
								"definition could be found in the container with the qualifier '%s' and type '%s'.",
								method.getName(), (i + 1), Configured.class.getSimpleName(),
								qualifier.value(), type.getName()
						));
					}
				} else {
					try {
						beanFactory.getBean(type);
						arg = new BeanReferenceResolver(type);
					} catch (NoSuchBeanDefinitionException e) {
						throw new ConfigurationException(format(
								"Listener method '%s' parameter %d is not marked as %s and no bean " +
								"definition could be found in the container with the type '%s'.",
								method.getName(), (i + 1), Configured.class.getSimpleName(),
								type.getName()
						));
					}
				}
			}
			argList.add(arg);
		}
		return new PostUpdateChangeListener(bean, method, argList);
	}

	
	@SuppressWarnings("rawtypes")
	private Class<?> listType(Type listType) {
		Class<?> type = null;
		if (listType instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) listType;
			Type[] actualTypeArguments = pType.getActualTypeArguments();
			type = (Class) actualTypeArguments[0];
		}
		return type;
	}

	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}
	
	public void setConfigurationSource(UpdatableConfigurationSource configurationSource) {
		this.configurationSource = configurationSource;
	}
	
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	private class FieldValueChangeListener<T extends Object> extends InvocationChangeListenerSupport<T> {
		private final Field field;
		
		public FieldValueChangeListener(Field field, Object target, Class<?> expectedValueType, boolean list) {
			super(target, expectedValueType, list);
			this.field = field;
		}

		public void onChange(T newValue) {
			try {
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}
				field.set(target, newValue);
			} catch (IllegalAccessException e) {
				throwError(field.getName(), newValue, e);
			}
		}
	}
	
	private class MethodValueChangeListener<T extends Object> extends InvocationChangeListenerSupport<T> {
		private final Method method;
		public MethodValueChangeListener(Method method, Object target, Class<?> expectedValueType, boolean list) {
			super(target, expectedValueType, list);
			this.method = method;
		}

		public void onChange(T newValue) {
			try {
				method.invoke(target, newValue);
			} catch (IllegalAccessException e) {
				throwError(method.getName(), newValue, e);
			} catch (InvocationTargetException e) {
				throwError(method.getName(), newValue, e);
			}
		}
	}
	
	private abstract class InvocationChangeListenerSupport<T extends Object> implements ValueChangeListener<T> {
		protected final Object target;
		private final Class<?> expectedValueType;
		private final boolean list;
		public InvocationChangeListenerSupport(Object target,
				Class<?> expectedValueType, boolean list) {
			this.target = target;
			this.expectedValueType = expectedValueType;
			this.list = list;
		}
		
		protected void throwError(String name, Object value, Throwable cause) {
			Class<?> valueType = (value != null ? value.getClass() : null);
			throw new PropertyUpdateException(name, valueType, expectedValueType, list, target.getClass(), cause);
		}
	}
	
	private static class PostUpdateChangeListener implements GroupChangeListener {
		private final Object target;
		private final Method method;
		private final List<ValueResolver> argValues;
		
		
		public PostUpdateChangeListener(Object target, Method method, List<ValueResolver> argValues) {
			this.target = target;
			this.method = method;
			this.argValues = argValues;
		}
		
		public Object getSemaphore() {
			return target;
		}

		public void onChange() {
			Object[] args = new Object[argValues.size()];
			for (int i = 0; i < argValues.size(); i++) {
				ValueResolver arg = argValues.get(i);
				args[i] = arg.getValue();
			}
			try {
				if (!method.isAccessible()) {
					method.setAccessible(true);
				}
				method.invoke(target, args);
			} catch (IllegalAccessException e) {
				throwError(args, e);
			} catch (InvocationTargetException e) {
				throwError(args, e);
			}
		}
		protected void throwError(Object[] args, Throwable cause) {
			throw new ConfigurationException(format("Listener method '%s' of type '%s' with arguments %s", 
					method.getName(), method.getDeclaringClass().getName(), Arrays.toString(args)));
		}
	}
	
	private class MethodParameterListener implements ValueChangeListener<Object>, ValueResolver {

		private Object value;
		
		public void onChange(Object newValue) {
			this.value = newValue;
		}
		
		public Object getValue() {
			return value;
		}
	}
	
	private class BeanReferenceResolver implements ValueResolver {
		private Qualifier qualifier;
		private Class<?> type;
		
		public BeanReferenceResolver(Qualifier qualifier, Class<?> type) {
			this.qualifier = qualifier;
			this.type = type;
		}

		public BeanReferenceResolver(Class<?> type) {
			this(null, type);
		}

		public Object getValue() {
			Object value;
			if (qualifier != null) {
				value = beanFactory.getBean(qualifier.value(), type);
			} else {
				value = beanFactory.getBean(type);
			}
			return value;
		}
	}
	
	private interface ValueResolver {
		Object getValue();
	}
}
