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

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * External namespace configurer.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
class NamespaceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected void doParse(final Element element, final ParserContext parserContext, final BeanDefinitionBuilder builder) {
        String prefix = element.getAttribute("prefix");
        String uri = element.getAttribute("uri");
        if (uri == null) {
            // Older versions had the attribute incorrectly named 'url'.
            uri = element.getAttribute("url");
        }
        String serviceRef = element.getAttribute("service-ref");
        builder.addConstructorArgReference(serviceRef + "-Namespaces");
        builder.addConstructorArgValue(uri);
        builder.addConstructorArgValue(prefix);
    }

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }

    @Override
    protected Class<?> getBeanClass(final Element element) {
        return NamespaceRegisteringBean.class;
    }
}
