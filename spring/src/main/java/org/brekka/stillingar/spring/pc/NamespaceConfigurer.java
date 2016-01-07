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

package org.brekka.stillingar.spring.pc;


import java.util.Map;

import org.brekka.stillingar.spring.config.NamespaceRegisteringBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;

/**
 * Adds namespaces to the configuration but will do so after all the beans have been loaded so that the list
 * cannot be overriden by multiple stil:configuration-service being added to the context
 * 
 * @author Anthony Mayfield
 */
public class NamespaceConfigurer implements BeanFactoryPostProcessor, ApplicationContextAware, Ordered {

    /**
     * The application context that loaded this instance
     */
    private ApplicationContext applicationContext;

    /**
     * The name of this bean within the container.
     */
    private String beanName;

    public NamespaceConfigurer() {
    }
    
    @Override
    public int getOrder() {
        return 1;
    }

    /**
     * This pulls out the list of {@link NamepsaceRegisteringBean} in the context. It then uses this list
     * to request the namespaces to be added to the context.
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactoryToProcess) throws BeansException {
        Map<String,NamespaceRegisteringBean> namespaceRegisteringBeans = applicationContext.getBeansOfType(NamespaceRegisteringBean.class, true, false);
        for(NamespaceRegisteringBean namespaceRegisteringBean : namespaceRegisteringBeans.values()) {
            namespaceRegisteringBean.addNamespaceToContext();
        }
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
