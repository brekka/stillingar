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

import org.brekka.stillingar.spring.pc.ConfigurationPlaceholderConfigurer.CustomStringValueResolver;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinitionVisitor;
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
    
    /**
     * @return the currentProperty
     */
    public PropertyValue getCurrentProperty() {
        return currentProperty;
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