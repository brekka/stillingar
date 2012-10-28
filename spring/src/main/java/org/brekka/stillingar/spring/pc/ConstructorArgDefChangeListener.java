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

package org.brekka.stillingar.spring.pc;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.List;

import org.brekka.stillingar.api.ConfigurationException;
import org.brekka.stillingar.spring.expr.Fragment;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.util.ObjectUtils;

/**
 * A listener that will update Spring's internal definition of a prototype bean. This allows new bean instances to be
 * created with the latest configuration.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class ConstructorArgDefChangeListener extends AbstractExpressionGroupListener {

    /**
     * The name of the bean that will be used to lookup its bean definition in the beanFactory.
     */
    private final String beanName;

    /**
     * The index of this constructor argument (may be null).
     */
    private final Integer constructorArgIndex;

    /**
     * The type of the constructor argument.
     */
    private final String constructorArgType;

    /**
     * Bean factory to lookup the bean definition in.
     */
    private final WeakReference<ConfigurableListableBeanFactory> beanFactoryRef;

    /**
     * @param beanName
     *            The name of the bean that will be used to lookup its bean definition in the beanFactory.
     * @param constructorArgIndex
     *            The index of this constructor argument (may be null).
     * @param constructorArgType
     *            The type of the constructor argument.
     * @param beanFactory
     *            Bean factory to lookup the bean definition in.
     * @param fragment
     *            the fragment that will be used to evaluate and obtain the value which will be passed to
     *            {@link #onChange(String)}
     */
    public ConstructorArgDefChangeListener(String beanName, Integer constructorArgIndex, String constructorArgType,
            ConfigurableListableBeanFactory beanFactory, Fragment fragment) {
        super(fragment);
        this.beanName = beanName;
        this.constructorArgIndex = constructorArgIndex;
        this.constructorArgType = constructorArgType;
        this.beanFactoryRef = new WeakReference<ConfigurableListableBeanFactory>(beanFactory);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.spring.pc.AbstractExpressionGroupListener#onChange(java.lang.String)
     */
    @Override
    protected void onChange(String newValue) {
        ConfigurableListableBeanFactory beanFactory = beanFactoryRef.get();
        if (beanFactory == null) {
            return;
        }
        
        BeanDefinition beanDef = beanFactory.getMergedBeanDefinition(beanName);
        ConstructorArgumentValues mutableConstructorValues = beanDef.getConstructorArgumentValues();
        ValueHolder valueHolder = null;
        List<ValueHolder> genericArgumentValues = mutableConstructorValues.getGenericArgumentValues();
        if (constructorArgIndex != null) {
            valueHolder = mutableConstructorValues.getIndexedArgumentValues().get(constructorArgIndex);
            if (valueHolder == null) {
                throw new IllegalStateException(String.format("Failed to find constructor arg at index %d",
                        constructorArgIndex));
            }
        } else if (genericArgumentValues.size() == 1) {
            valueHolder = genericArgumentValues.get(0);
        } else {
            for (ValueHolder vh : genericArgumentValues) {
                if (vh.getType().equals(constructorArgType)) {
                    valueHolder = vh;
                }
            }
            if (valueHolder == null) {
                throw new IllegalStateException(String.format("Failed to find constructor arg with type '%s'",
                        constructorArgType));
            }
        }
        if (!ObjectUtils.nullSafeEquals(newValue, valueHolder.getValue())) {
            valueHolder.setValue(newValue);
            try {
                /*
                 * Spring implements caching of constructor values, which can be reset by clearing the package-private
                 * field 'resolvedConstructorOrFactoryMethod' on RootBeanDefinition. Naturally this will fail if a
                 * security manager is present but there doesn't seem to be any other way to do it. Make sure to warn
                 * about this in the documentation!
                 */
                Field field = beanDef.getClass().getDeclaredField("resolvedConstructorOrFactoryMethod");
                field.setAccessible(true);
                field.set(beanDef, null);
            } catch (Exception e) {
                throw new ConfigurationException(String.format("Unable to update value for constructor argument '%s'. "
                        + "Failed to reset the cached constructor state for bean '%s'",
                        (constructorArgIndex != null ? constructorArgIndex.toString() : constructorArgType), beanName),
                        e);
            }
        }
    }

}
