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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinitionVisitor;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.util.ObjectUtils;

/**
 * Visit the various parts of a bean definition, capturing calls to set values on properties and constructors. The main
 * purpose of this class is to capture the property/constructor definition and make them available to the
 * {@link CustomStringValueResolver} that can then register listeners for configuration changes to update those values.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
class CustomBeanDefinitionVisitor extends BeanDefinitionVisitor {
    /**
     * The name of the bean being visited.
     */
    private final String beanName;

    /**
     * Is this a singleton bean definition
     */
    private final boolean singleton;

    /**
     * The current property being visited (only set when {@link #visitPropertyValues(MutablePropertyValues)} is called).
     */
    private PropertyValue currentProperty;

    /**
     * The current constructor being visited (only set when {@link #visitGenericArgumentValues(List)} or
     * {@link #visitIndexedArgumentValues(Map)} are called).
     */
    private ValueHolder currentConstructorValue;

    /**
     * The current constructor index being visited (only set when {@link #visitIndexedArgumentValues(Map)} is called).
     */
    private Integer currentConstructorIndex;

    /**
     * @param beanName
     *            The name of the bean being visited.
     * @param singleton
     *            Is this a singleton bean definition
     * @param valueResolver
     *            the resolver that will be used to lookup the initial value, and apply change listeners.
     */
    public CustomBeanDefinitionVisitor(String beanName, boolean singleton, CustomStringValueResolver valueResolver) {
        super(valueResolver);
        this.singleton = singleton;
        this.beanName = beanName;
        valueResolver.setBeanDefVisitor(this);
    }

    /**
     * Visit properties
     */
    @Override
    protected void visitPropertyValues(MutablePropertyValues pvs) {
        PropertyValue[] pvArray = pvs.getPropertyValues();
        for (PropertyValue pv : pvArray) {
            currentProperty = pv;
            Object newVal = resolveValue(pv.getValue());

            // Change the value for the first time.
            if (!ObjectUtils.nullSafeEquals(newVal, pv.getValue())) {
                pvs.add(pv.getName(), newVal);
            }
            currentProperty = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.beans.factory.config.BeanDefinitionVisitor#visitGenericArgumentValues(java.util.List)
     */
    @Override
    protected void visitGenericArgumentValues(List<ValueHolder> gas) {
        for (ValueHolder valueHolder : gas) {
            currentConstructorValue = valueHolder;
            Object newVal = resolveValue(valueHolder.getValue());
            if (!ObjectUtils.nullSafeEquals(newVal, valueHolder.getValue())) {
                valueHolder.setValue(newVal);
            }
            currentConstructorValue = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.beans.factory.config.BeanDefinitionVisitor#visitIndexedArgumentValues(java.util.Map)
     */
    @Override
    protected void visitIndexedArgumentValues(Map<Integer, ValueHolder> ias) {
        Set<Entry<Integer, ValueHolder>> entrySet = ias.entrySet();
        for (Entry<Integer, ValueHolder> entry : entrySet) {
            ValueHolder valueHolder = entry.getValue();
            currentConstructorValue = valueHolder;
            currentConstructorIndex = entry.getKey();
            Object newVal = resolveValue(valueHolder.getValue());
            if (!ObjectUtils.nullSafeEquals(newVal, valueHolder.getValue())) {
                valueHolder.setValue(newVal);
            }
            currentConstructorValue = null;
            currentConstructorIndex = null;
        }
    }

    /**
     * @return the currentProperty
     */
    public PropertyValue getCurrentProperty() {
        return currentProperty;
    }

    /**
     * @return the currentConstructorValue
     */
    public ValueHolder getCurrentConstructorValue() {
        return currentConstructorValue;
    }

    /**
     * @return the currentConstructorIndex
     */
    public Integer getCurrentConstructorIndex() {
        return currentConstructorIndex;
    }

    /**
     * @return the singleton
     */
    public boolean isSingleton() {
        return singleton;
    }

    /**
     * @return the beanName
     */
    public String getBeanName() {
        return beanName;
    }
}
