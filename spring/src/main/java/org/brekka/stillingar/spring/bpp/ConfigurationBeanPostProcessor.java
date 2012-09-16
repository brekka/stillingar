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

package org.brekka.stillingar.spring.bpp;

import static java.lang.String.format;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.brekka.stillingar.annotations.ConfigurationListener;
import org.brekka.stillingar.annotations.Configured;
import org.brekka.stillingar.core.ChangeAwareConfigurationSource;
import org.brekka.stillingar.core.ConfigurationException;
import org.brekka.stillingar.core.ConfigurationSource;
import org.brekka.stillingar.core.GroupChangeListener;
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
 * Identifies and enhances Spring managed beans that are marked with the {@link Configured} annotation and contain
 * fields/methods that marked to be configured. Fields and setter methods would be marked with {@link Configured}, with
 * listener methods marked with {@link ConfigurationListener}.
 * 
 * If the {@link ConfigurationSource} passed to this post-processor is also an instance of
 * {@link ChangeAwareConfigurationSource} then all configuration will be registered to receive updates from the
 * configuration source.
 * 
 * @author Andrew Taylor
 */
public class ConfigurationBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {

    /**
     * The configuration source from which configuration values will be resolved, and potentially registered to receive
     * automatic updates.
     */
    private final ConfigurationSource configurationSource;

    /**
     * Will be used to identify whether a bean is singleton or not, and also to lookup beans for the
     * {@link ConfigurationListener} mechanism.
     */
    private BeanFactory beanFactory;

    /**
     * The class level annotation to identify beans that should be configured.
     */
    private Class<? extends Annotation> markerAnnotation = Configured.class;

    /**
     * A cache of value definition groups, used when a given type
     */
    private Map<Class<?>, ValueDefinitionGroup> onceOnlyDefinitionCache = new HashMap<Class<?>, ValueDefinitionGroup>();

    public ConfigurationBeanPostProcessor(ConfigurationSource configurationSource) {
        this.configurationSource = configurationSource;
    }

    /**
     * Checks whether the bean is annotated with {@link Configured} (or other) and determines whether the bean needs to
     * be registered for updates or just configured one-time with the current configuration state.
     */
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        if (hasMarkerAnnotation(beanClass)) {

            boolean singleton = beanFactory.isSingleton(beanName);

            if (singleton && configurationSource instanceof ChangeAwareConfigurationSource) {
                processWithUpdates(bean, beanName);
            } else {
                processOnceOnly(bean, beanName);
            }

        }
        return bean;
    }

    /**
     * When configuration values are encountered, they will be retrieved and applied only. No updates will be performed.
     * Listeners will be called once to ensure we don't break their contract.
     * 
     * @param bean
     *            the bean being configured
     * @param beanName
     *            the name of the bean used in log messages etc.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void processOnceOnly(Object bean, String beanName) {
        Class<? extends Object> targetClass = bean.getClass();
        ValueDefinitionGroup valueDefinitionGroup = onceOnlyDefinitionCache.get(targetClass);
        if (valueDefinitionGroup == null) {
            synchronized (onceOnlyDefinitionCache) {
                /*
                 * Capture the type of the target being configured. We don't want to use the bean itself as the
                 * definition will be reused for other instances, none of which should be updated.
                 */
                OnceOnlyTypeHolder target = new OnceOnlyTypeHolder(targetClass);
                valueDefinitionGroup = prepareValueGroup(beanName, target);
                // Cache the type.
                onceOnlyDefinitionCache.put(targetClass, valueDefinitionGroup);
            }
        }
        List<ValueDefinition<?>> values = valueDefinitionGroup.getValues();
        for (ValueDefinition<?> valueDefinition : values) {
            Object value;
            if (valueDefinition.isList()) {
                if (valueDefinition.getExpression() != null) {
                    value = configurationSource
                            .retrieveList(valueDefinition.getExpression(), valueDefinition.getType());
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
            changeListener.onChange(configurationSource);
        }
    }

    /**
     * Process configured field/method/listeners, also registering them for updates.
     * 
     * @param bean
     *            the bean being configured
     * @param beanName
     *            the name of the bean used in log messages etc.
     */
    protected void processWithUpdates(Object bean, String beanName) {
        ValueDefinitionGroup group = prepareValueGroup(beanName, bean);
        ((ChangeAwareConfigurationSource) configurationSource).register(group, true);
    }

    /**
     * Prepare the {@link ValueDefinitionGroup} for the specified bean.
     * 
     * @param beanName
     *            the name of the bean used in log messages etc.
     * @param target
     *            the bean being configured
     * @return the value definition group
     */
    protected ValueDefinitionGroup prepareValueGroup(String beanName, Object target) {
        List<ValueDefinition<?>> valueList = new ArrayList<ValueDefinition<?>>();

        Class<? extends Object> beanClass = target.getClass();
        if (target instanceof OnceOnlyTypeHolder) {
            /*
             * The target bean is not available, just the type.
             */
            beanClass = ((OnceOnlyTypeHolder) target).get();
        }

        Class<?> inpectClass = beanClass;
        while (inpectClass != null) {
            Field[] declaredFields = inpectClass.getDeclaredFields();
            for (Field field : declaredFields) {
                processField(field, valueList, target);
            }
            inpectClass = inpectClass.getSuperclass();
        }

        PostUpdateChangeListener beanChangeListener = null;

        Method[] declaredMethods = beanClass.getDeclaredMethods();
        for (Method method : declaredMethods) {
            Configured configured = method.getAnnotation(Configured.class);

            ConfigurationListener configurationListener = method.getAnnotation(ConfigurationListener.class);
            if (configurationListener != null) {
                if (beanChangeListener != null) {
                    throw new ConfigurationException(format(
                            "Unable to create a configuration listener for the method '%s' "
                                    + "as it already contains a configuration listener on the method '%s'", method,
                            beanName, beanClass.getName(), beanChangeListener.getMethod()));
                }
                beanChangeListener = processListenerMethod(method, valueList, target);

            } else if (configured != null) {
                processSetterMethod(configured, method, valueList, target);
            }
        }
        ValueDefinitionGroup group = new ValueDefinitionGroup(beanName, valueList, beanChangeListener, target);
        return group;
    }

    /**
     * Encapsulates a field in a {@link ValueDefinition} so that it can be registered for updates.
     * 
     * @param field
     *            the field being processed
     * @param valueList
     *            the list of value definitions that the new {@link ValueDefinition} for this field will be added to.
     * @param bean
     *            the bean being configured.
     */
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

    /**
     * Encapsulate a setter method in a {@link ValueDefinition} so that it can be registered for configuration updates.
     * 
     * @param configured
     *            the {@link Configured} attribute applied to the method.
     * @param method
     *            the method itself
     * @param valueList
     *            the list of value definitions that the new {@link ValueDefinition} for this field will be added to.
     * @param bean
     *            the bean being configured.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void processSetterMethod(Configured configured, Method method, List<ValueDefinition<?>> valueList,
            Object bean) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new ConfigurationException(format("The method '%s' does not appear to be a setter. "
                    + "A bean setter method should take only a single parameter.", method));
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

    /**
     * Encapsulate the 'listener' method that will be invoked once all fields/setter methods have been updated. The
     * parameters of this method will be added as individual {@link ValueDefinition}'s to <code>valueList</code>.
     * 
     * @param method
     *            the method being encapsulated
     * @param valueList
     *            the list of value definitions that the new {@link ValueDefinition}s for this field will be added to.
     * @param bean
     *            the bean being configured.
     * @return the {@link PostUpdateChangeListener} that will invoke the listener method on configuration update.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected PostUpdateChangeListener processListenerMethod(Method method, List<ValueDefinition<?>> valueList,
            Object bean) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        List<ParameterValueResolver> argList = new ArrayList<ParameterValueResolver>();
        for (int i = 0; i < parameterTypes.length; i++) {
            ParameterValueResolver arg = null;
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
                    ValueDefinition<Object> value = new ValueDefinition<Object>(type, paramConfigured.value(), mpl,
                            list);
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
                        arg = new BeanReferenceResolver(beanFactory, qualifier, type);
                    } catch (NoSuchBeanDefinitionException e) {
                        throw new ConfigurationException(
                                format("Listener method '%s' parameter %d is not marked as %s and no bean "
                                        + "definition could be found in the container with the qualifier '%s' and type '%s'.",
                                        method.getName(), (i + 1), Configured.class.getSimpleName(), qualifier.value(),
                                        type.getName()));
                    }
                } else {
                    try {
                        beanFactory.getBean(type);
                        arg = new BeanReferenceResolver(beanFactory, type);
                    } catch (NoSuchBeanDefinitionException e) {
                        throw new ConfigurationException(format(
                                "Listener method '%s' parameter %d is not marked as %s and no bean "
                                        + "definition could be found in the container with the type '%s'.",
                                method.getName(), (i + 1), Configured.class.getSimpleName(), type.getName()));
                    }
                }
            }
            argList.add(arg);
        }
        return new PostUpdateChangeListener(bean, method, argList);
    }

    /**
     * Determine if the bean class or any of its super types have a {@link Configured} (or other set by
     * <code>markerAnnotation</code>) annotation set.
     * 
     * @param beanClass
     *            the class to inspect
     * @return true if the class or any of its super types have the annotation. Note that this does not include
     *         interfaces as they do not support fields or concrete method definitions.
     */
    private boolean hasMarkerAnnotation(Class<?> beanClass) {
        boolean retVal = false;
        Class<?> inpectClass = beanClass;
        while (inpectClass != null) {
            if (inpectClass.getAnnotation(markerAnnotation) != null) {
                retVal = true;
                break;
            }
            inpectClass = inpectClass.getSuperclass();
        }
        return retVal;
    }

    /**
     * Simply returns the bean parameter.
     */
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * Set the marker annotation that identifies the bean as being configured. By default set to {@link Configured}.
     * 
     * @param markerAnnotation
     *            the configured bean marker class.
     */
    public void setMarkerAnnotation(Class<? extends Annotation> markerAnnotation) {
        this.markerAnnotation = markerAnnotation;
    }

    /**
     * Set the bean factory
     */
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * Identifies the type of the parameterized list.
     * 
     * @param listType
     *            the list type to inspect
     * @return the list type or null if it is not parameterized.
     */
    @SuppressWarnings("rawtypes")
    private static Class<?> listType(Type listType) {
        Class<?> type = null;
        if (listType instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) listType;
            Type[] actualTypeArguments = pType.getActualTypeArguments();
            type = (Class) actualTypeArguments[0];
        }
        return type;
    }
}
