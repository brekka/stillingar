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

import org.brekka.stillingar.spring.expr.DefaultPlaceholderParser;
import org.brekka.stillingar.spring.pc.ConfigurationPlaceholderConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * External property placeholder configurer.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
class PropertyPlaceholderBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#doParse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext, org.springframework.beans.factory.support.BeanDefinitionBuilder)
     */
    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        String serviceRef = element.getAttribute("service-ref");
        String prefix = element.getAttribute("prefix");
        String suffix = element.getAttribute("suffix");

        builder.addConstructorArgReference(serviceRef);

        BeanDefinitionBuilder placeholderParser = BeanDefinitionBuilder
                .genericBeanDefinition(DefaultPlaceholderParser.class);
        placeholderParser.addConstructorArgValue(prefix);
        placeholderParser.addConstructorArgValue(suffix);

        builder.addPropertyValue("placeholderParser", placeholderParser.getBeanDefinition());
    }
    
    @Override
    protected boolean shouldGenerateId() {
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#getBeanClass(org.w3c.dom.Element)
     */
    @Override
    protected Class<?> getBeanClass(Element element) {
        return ConfigurationPlaceholderConfigurer.class;
    }
}
