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

import org.brekka.stillingar.core.Expirable;
import org.brekka.stillingar.spring.expr.Fragment;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.ObjectUtils;

/**
 * A value change listener that will resolve property changes by looking up the named bean in the {@link BeanFactory}
 * and then updating the instance directly.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
class PropertyDefChangeListener extends AbstractExpressionGroupListener implements Expirable {
    /**
     * The name of the bean within the Spring context. Will be used to lookup its current definition.
     */
    private final String beanName;

    /**
     * The name of the property being updated.
     */
    private final String propertyName;

    /**
     * The bean factory in which to resolve the bean definition of bean identified by <code>beanName</code>
     */
    private final WeakReference<ConfigurableListableBeanFactory> beanFactoryRef;

    
    /**
     * @param beanName
     *            The name of the bean within the Spring context. Will be used to lookup its current definition.
     * @param propertyName
     *            The name of the property being updated.
     * @param beanFactory
     *            The bean factory in which to resolve the bean definition of bean identified by <code>beanName</code>
     * @param fragment
     *            the fragment that will be used to evaluate and obtain the value which will be passed to
     *            {@link #onChange(String)}
     */
    public PropertyDefChangeListener(String beanName, String propertyName, ConfigurableListableBeanFactory beanFactory,
            Fragment fragment) {
        super(fragment);
        this.beanName = beanName;
        this.propertyName = propertyName;
        this.beanFactoryRef = new WeakReference<ConfigurableListableBeanFactory>(beanFactory);
    }

    /**
     * Update the property with the new value
     */
    @Override
    public void onChange(String newValue) {
        ConfigurableListableBeanFactory beanFactory = beanFactoryRef.get();
        if (beanFactory == null) {
            return;
        }
        BeanDefinition beanDef = beanFactory.getMergedBeanDefinition(beanName);
        MutablePropertyValues mutablePropertyValues = beanDef.getPropertyValues();
        PropertyValue propertyValue = mutablePropertyValues.getPropertyValue(propertyName);
        if (!ObjectUtils.nullSafeEquals(newValue, propertyValue.getValue())) {
            mutablePropertyValues.add(propertyValue.getName(), newValue);
        }
    }
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ExpiringListener#isExpired()
     */
    @Override
    public boolean isExpired() {
        return beanFactoryRef.isEnqueued();
    }
}
