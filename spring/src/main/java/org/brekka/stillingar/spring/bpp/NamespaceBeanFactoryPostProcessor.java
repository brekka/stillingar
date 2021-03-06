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
package org.brekka.stillingar.spring.bpp;

import java.util.Map;

import org.brekka.stillingar.spring.config.NamespaceRegisteringBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class NamespaceBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        Map<String, NamespaceRegisteringBean> namespaceRegisteringBeans = beanFactory.getBeansOfType(NamespaceRegisteringBean.class, true, false);
        for(NamespaceRegisteringBean namespaceRegisteringBean : namespaceRegisteringBeans.values()) {
            namespaceRegisteringBean.registerNamespaces();
        }
    }
}
