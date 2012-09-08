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

import org.brekka.stillingar.spring.pc.ConfigurationPlaceholderConfigurer.CustomStringValueResolver;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinitionVisitor;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.util.ObjectUtils;

/**
 * 
 * TODO Description of CustomBeanDefinitionVisitor
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
class CustomBeanDefinitionVisitor extends BeanDefinitionVisitor {
    private final String beanName;
    private final boolean singleton;

    private PropertyValue currentProperty;
    
    private ValueHolder currentConstructorValue;
    
    private Integer currentConstructorIndex;
    

    public CustomBeanDefinitionVisitor(String beanName, boolean singleton, CustomStringValueResolver valueResolver) {
        super(valueResolver);
        this.singleton = singleton;
        this.beanName = beanName;
        valueResolver.setBeanDefVisitor(this);
    }

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
    
    /* (non-Javadoc)
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
    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.BeanDefinitionVisitor#visitIndexedArgumentValues(java.util.Map)
     */
    @Override
    protected void visitIndexedArgumentValues(Map<Integer, ValueHolder> ias) {
        Set<Entry<Integer,ValueHolder>> entrySet = ias.entrySet();
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