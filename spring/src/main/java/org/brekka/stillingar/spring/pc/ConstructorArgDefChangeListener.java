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

import java.util.List;

import org.brekka.stillingar.spring.pc.expr.Fragment;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.util.ObjectUtils;

/**
 * TODO Description of ConstructorArgDefChangeListener
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class ConstructorArgDefChangeListener extends AbstractExpressionGroupListener {

    private final String beanName;
    private final Integer constructorArgIndex;
    private final String constructorArgType;
    private final ConfigurableListableBeanFactory beanFactory;
    
    /**
     * @param beanName
     * @param name
     * @param beanFactory
     * @param fragment
     */
    public ConstructorArgDefChangeListener(String beanName, Integer constructorArgIndex, 
            String constructorArgType, ConfigurableListableBeanFactory beanFactory, Fragment fragment) {
        super(fragment);
        this.beanName = beanName;
        this.constructorArgIndex = constructorArgIndex;
        this.constructorArgType = constructorArgType;
        this.beanFactory = beanFactory;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.spring.pc.AbstractExpressionGroupListener#onChange(java.lang.String)
     */
    @Override
    protected void onChange(String newValue) {
        BeanDefinition beanDef = beanFactory.getMergedBeanDefinition(beanName);
        ConstructorArgumentValues mutableConstructorValues = beanDef.getConstructorArgumentValues();
        ValueHolder valueHolder = null;
        List<ValueHolder> genericArgumentValues = mutableConstructorValues.getGenericArgumentValues();
        if (constructorArgIndex != null) {
            valueHolder = mutableConstructorValues.getIndexedArgumentValues().get(constructorArgIndex);
            if (valueHolder == null) {
                throw new IllegalStateException(String.format(
                        "Failed to find constructor arg at index %d", constructorArgIndex));
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
                throw new IllegalStateException(String.format(
                        "Failed to find constructor arg with type '%s'",  constructorArgType));
            }
        }
        if (!ObjectUtils.nullSafeEquals(newValue, valueHolder.getValue())) {
            valueHolder.setValue(newValue);
        }
    }

}
