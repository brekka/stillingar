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

package org.brekka.stillingar.spring.config;

import org.brekka.stillingar.api.ConfigurationException;
import org.brekka.stillingar.spring.bpp.ConfigurationBeanPostProcessor;
import org.brekka.stillingar.spring.bpp.NamespaceBeanFactoryPostProcessor;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Annotation config (external)
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
class AnnotationConfigBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected void doParse(final Element element, final ParserContext parserContext, final BeanDefinitionBuilder builder) {
        String serviceRef = element.getAttribute("service-ref");
        builder.addConstructorArgValue(":" + serviceRef);
        builder.addConstructorArgReference(serviceRef);
        String marker = element.getAttribute("marker");
        if (StringUtils.hasLength(marker)) {
            Class<?> theClass = ClassUtils.resolveClassName(marker, Thread.currentThread().getContextClassLoader());
            if (!theClass.isAnnotation()) {
                throw new ConfigurationException(String.format("The class '%s' is not an annotation", marker));
            }
            builder.addPropertyValue("markerAnnotation", theClass);
        }

        // Register the post processor if there is not already one in this context
        String beanName = NamespaceBeanFactoryPostProcessor.class.getName();
        if (!parserContext.getRegistry().containsBeanDefinition(beanName)) {
            BeanDefinitionBuilder namespacePostProcessor = BeanDefinitionBuilder.genericBeanDefinition(NamespaceBeanFactoryPostProcessor.class);
            parserContext.registerBeanComponent(new BeanComponentDefinition(namespacePostProcessor.getBeanDefinition(), beanName));
        }
    }

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }

    @Override
    protected Class<?> getBeanClass(final Element element) {
        return ConfigurationBeanPostProcessor.class;
    }
}
