/*
 * Copyright 2016 the original author or authors.
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

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.brekka.stillingar.core.dom.DefaultNamespaceContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Prepares a new {@link DefaultNamespaceContext} and registers all {@link NamespaceRegisteringBean}s that can be found
 * in the {@link ApplicationContext}. Only {@link NamespaceRegisteringBean}s whose serviceId is null or matches
 * {@link #serviceId} will be registered with the {@link DefaultNamespaceContext}.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class DefaultNamespaceContextFactoryBean
        implements FactoryBean<DefaultNamespaceContext>, InitializingBean, ApplicationContextAware {

    private final String serviceId;
    
    private ApplicationContext applicationContext;
    
    private final DefaultNamespaceContext context = new DefaultNamespaceContext();
    
    
    public DefaultNamespaceContextFactoryBean(String serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, NamespaceRegisteringBean> namespaceRegBeanMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(
                applicationContext, NamespaceRegisteringBean.class);
        Collection<NamespaceRegisteringBean> namespaceRegBeans = namespaceRegBeanMap.values();
        for (NamespaceRegisteringBean bean : namespaceRegBeans) {
            String nsServiceId = bean.getServiceId();
            if (nsServiceId == null || Objects.equals(nsServiceId, serviceId)) {
                context.registerNamespace(bean.getPrefix(), bean.getUri());
            }
        }
    }
    
    @Override
    public DefaultNamespaceContext getObject() throws Exception {
        return context;
    }

    @Override
    public Class<?> getObjectType() {
        return DefaultNamespaceContext.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
