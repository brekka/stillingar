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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.brekka.stillingar.annotations.ConfigurationListener;
import org.brekka.stillingar.annotations.Configured;
import org.brekka.stillingar.core.ConfigurationException;
import org.brekka.stillingar.core.ConfigurationSource;
import org.brekka.stillingar.core.GroupChangeListener;
import org.brekka.stillingar.core.ReferentUpdateException;
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
 * TODO
 * 
 * @author Andrew Taylor
 */
public class ConfigurationBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {

	private final ConfigurationSource configurationSource;
	
	private BeanFactory beanFactory;
	
	
	private Class<? extends Annotation> markerAnnotation = Configured.class;
	
	private Map<Class<?>, ValueDefinitionGroup> onceOnlyDefinitionCache = new HashMap<Class<?>, ValueDefinitionGroup>();
	
	
	
	public ConfigurationBeanPostProcessor(ConfigurationSource configurationSource) {
		this.configurationSource = configurationSource;
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		Class<? extends Object> beanClass = bean.getClass();
		
		if (beanClass.getAnnotation(markerAnnotation) != null) {
			
			boolean singleton = beanFactory.isSingleton(beanName);
			
			if (singleton 
			        && configurationSource instanceof UpdatableConfigurationSource) {
			    processWithUpdates(bean, beanName);
			} else {
			    processOnceOnly(bean, beanName);
			}
			
		}
		return bean;
	}
	
	/**
	 * When configuration values are encountered, they will be retrieved and applied only. No updates
	 * will be performed. Listeners will be called once to ensure we don't break their contract.
	 * @param bean
	 * @param beanName
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
    protected void processOnceOnly(Object bean, String beanName) {
	    Class<? extends Object> targetClass = bean.getClass();
	    ValueDefinitionGroup valueDefinitionGroup = onceOnlyDefinitionCache.get(targetClass);
	    if (valueDefinitionGroup == null) {
	        synchronized (onceOnlyDefinitionCache) {
	            TargetClass target = new TargetClass(targetClass);
	            valueDefinitionGroup = prepareValueGroup(beanName, target);
	            onceOnlyDefinitionCache.put(targetClass, valueDefinitionGroup);
            }
	    }
	    List<ValueDefinition<?>> values = valueDefinitionGroup.getValues();
	    for (ValueDefinition<?> valueDefinition : values) {
	        Object value;
	        if (valueDefinition.isList()) {
	            if (valueDefinition.getExpression() != null) {
	                value = configurationSource.retrieveList(valueDefinition.getExpression(), valueDefinition.getType());
	            } else {
	                value = configurationSource.retrieveList(valueDefinition.getType());
	            }
	        } else {
	            if (valueDefinition.getExpression() != null) {
                    value = configurationSource.retrieve(valueDefinition.getExpression(), valueDefinition.getType());
                } else {
                    value = configurationSource.retrieve(valueDefinition.getType());
                }
	        }
	        ValueChangeListener listener = valueDefinition.getListener();
	        listener.onChange(value);
        }
	    GroupChangeListener changeListener = valueDefinitionGroup.getChangeListener();
	    if (changeListener != null) {
	        /*
	         * Note we are deliberately not obtaining the semaphore.
	         */
	        changeListener.onChange();
	    }
	}

	/**
	 * Process configured field/method/listeners, also registering them for updates.
	 * @param bean
	 * @param beanName
	 */
	protected void processWithUpdates(Object bean, String beanName) {
		ValueDefinitionGroup group = prepareValueGroup(beanName, bean);
		((UpdatableConfigurationSource) configurationSource).register(group);
	}


	
	protected ValueDefinitionGroup prepareValueGroup(String beanName, Object target) {
	    List<ValueDefinition<?>> valueList = new ArrayList<ValueDefinition<?>>();
	    
	    Class<? extends Object> beanClass = target.getClass();
	    if (target instanceof TargetClass) {
	        beanClass = ((TargetClass) target).get();
	    }
	    
        PostUpdateChangeListener beanChangeListener = null;
        
        Field[] declaredFields = beanClass.getDeclaredFields();
        for (Field field : declaredFields) {
            processField(field, valueList, target);
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
                            method, beanName, beanClass.getName(), beanChangeListener.method));
                }
                beanChangeListener = processListenerMethod(method, valueList, target);
                
            } else if (configured != null) {
                processSetterMethod(configured, method, valueList, target);
            }
        }
        ValueDefinitionGroup group = new ValueDefinitionGroup(beanName, valueList, beanChangeListener, target);
        return group;
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
	protected void processSetterMethod(Configured configured, Method method, List<ValueDefinition<?>> valueList, Object target) {
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
		MethodValueChangeListener<Object> listener = new MethodValueChangeListener<Object>(method, target, type, list);
		ValueDefinition<Object> value = new ValueDefinition<Object>(type, configured.value(), listener, list);
		valueList.add(value);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected PostUpdateChangeListener processListenerMethod(Method method, List<ValueDefinition<?>> valueList, Object target) {
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
		return new PostUpdateChangeListener(target, method, argList);
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
	
	public void setMarkerAnnotation(Class<? extends Annotation> markerAnnotation) {
		this.markerAnnotation = markerAnnotation;
	}
	
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	private class FieldValueChangeListener<T extends Object> extends InvocationChangeListenerSupport<T> {
		private final Field field;
		
		public FieldValueChangeListener(Field field, Object target, Class<?> expectedValueType, boolean list) {
			super(target, expectedValueType, list, "Field");
			this.field = field;
		}

		public void onChange(T newValue, Object target) {
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
			super(target, expectedValueType, list, "Method");
			this.method = method;
		}

		public void onChange(T newValue, Object target) {
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
	
	private static class PostUpdateChangeListener implements GroupChangeListener {
		private final Object target;
		private final Method method;
		private final List<ValueResolver> argValues;
		
		
		public PostUpdateChangeListener(Object target, Method method, List<ValueResolver> argValues) {
			this.target = target;
			this.method = method;
			this.argValues = argValues;
		}
		
		public void onChange() {
            onChange(target);
        }
		
		public void onChange(Object target) {
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
	
	private class TargetClass {
	    private final Class<?> target;
	    
	    public TargetClass(Class<?> target) {
            this.target = target;
        }

        public Class<?> get() {
            return target;
        }
	}
}
