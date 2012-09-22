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
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanFactory;

/**
 * A value change listener that will resolve property changes by looking up the named bean in the {@link BeanFactory}
 * and then updating the instance directly.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
class BeanPropertyChangeListener extends AbstractExpressionGroupListener implements Expirable {
    /**
     * The name of the bean that will be used to lookup its bean definition in the beanFactory.
     */
    private final String beanName;

    /**
     * The name of the property being updated.
     */
    private final String property;

    /**
     * Bean factory to lookup the bean in.
     */
    private final WeakReference<BeanFactory> beanFactoryRef;

    /**
     * @param beanName
     *            The name of the bean that will be used to lookup its bean definition in the beanFactory.
     * @param property
     *            The name of the property being updated.
     * @param beanFactory
     *            Bean factory to lookup the bean in.
     * @param fragment
     *            the fragment that will be used to evaluate and obtain the value which will be passed to
     *            {@link #onChange(String)}
     */
    public BeanPropertyChangeListener(String beanName, String property, BeanFactory beanFactory, Fragment fragment) {
        super(fragment);
        this.beanName = beanName;
        this.property = property;
        this.beanFactoryRef = new WeakReference<BeanFactory>(beanFactory);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ExpiringListener#isExpired()
     */
    @Override
    public boolean isExpired() {
        return beanFactoryRef.isEnqueued();
    }

    /**
     * Handle the change by using reflection to change the bean property.
     */
    public void onChange(String newValue) {
        BeanFactory beanFactory = beanFactoryRef.get();
        if (beanFactory == null) {
            return;
        }
        Object bean = beanFactory.getBean(beanName);
        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
        beanWrapper.setPropertyValue(new PropertyValue(property, newValue));
    }
}