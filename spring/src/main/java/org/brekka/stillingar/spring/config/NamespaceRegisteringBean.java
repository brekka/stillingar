/*
 * Copyright 2013 the original author or authors.
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

package org.brekka.stillingar.spring.config;

import org.brekka.stillingar.core.ConfigurationService;
import org.brekka.stillingar.core.dom.DefaultNamespaceContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Encapsulates a namespace uri to prefix mapping, also registering the mapping with the {@link DefaultNamespaceContext}
 * that belongs to the {@link ConfigurationService} with id {@link #serviceId} or any single
 * {@link DefaultNamespaceContext} in the current application context.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class NamespaceRegisteringBean implements InitializingBean, ApplicationContextAware {

    private final String serviceId;
    private final String uri;
    private final String prefix;
    
    private ApplicationContext applicationContext;

    public NamespaceRegisteringBean(String serviceId, String prefix, String uri) {
        this.serviceId = serviceId;
        this.prefix = prefix;
        this.uri = uri;
    }
    
    public String getServiceId() {
        return serviceId;
    }

    public String getUri() {
        return uri;
    }

    public String getPrefix() {
        return prefix;
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DefaultNamespaceContext namespaceContext;
        if (serviceId == null) {
            namespaceContext = applicationContext.getBean(DefaultNamespaceContext.class);
        } else {
            String beanName = serviceId + ConfigurationServiceBeanDefinitionParser.BEAN_NAMESPACES_SUFFIX;
            namespaceContext = applicationContext.getBean(beanName, DefaultNamespaceContext.class);
        }
        namespaceContext.registerNamespace(prefix, uri);
    }
}
