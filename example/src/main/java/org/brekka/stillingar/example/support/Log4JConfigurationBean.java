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

package org.brekka.stillingar.example.support;

import static java.lang.String.format;

import org.brekka.stillingar.core.ConfigurationService;
import org.brekka.stillingar.core.SingleValueDefinition;
import org.brekka.stillingar.core.ValueChangeListener;
import org.brekka.stillingar.core.ValueDefinition;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.w3c.dom.Element;

/**
 * @author Andrew Taylor
 */
public class Log4JConfigurationBean implements InitializingBean, BeanFactoryAware {

    private String expression;
    
    private ConfigurationService source;
    
    private BeanFactory beanFactory;
    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (expression == null) {
            throw new IllegalStateException("An expression must be specified");
        }
        try {
            org.apache.log4j.xml.DOMConfigurator.class.getClass();
        } catch (NoClassDefFoundError e) {
            // Log4J is not available on the classpath, do nothing.
            return;
        }
        
        ConfigurationService configSource = this.source;
        if (configSource == null) {
            // See if there is only one bean available
            
            try {
                configSource = beanFactory.getBean(ConfigurationService.class);
            } catch (NoSuchBeanDefinitionException e) {
                throw new IllegalStateException(format(
                        "Unable to resolve a configuration source for expression '%s'. " +
                        "There was none set on 'source' and there appear to be no instances in the" +
                        "context.", expression), e);
            } catch (BeansException e) {
                throw new IllegalStateException(format(
                        "Unable to resolve a configuration source for expression '%s'. " +
                        "There was none set on 'source' and there are multiple choices in the context", expression), e);
            }
        }
        
        ValueDefinition<Element, ValueChangeListener<Element>> valueDef = new SingleValueDefinition<Element>(
            Element.class, 
            expression, 
            new ValueChangeListener<Element>() {
                @Override
                public void onChange(Element configuration, Element previous) {
                    org.apache.log4j.xml.DOMConfigurator.configure(configuration);
                }
            }
        );
        configSource.register(valueDef, true);
    }
    
    /**
     * @param expression the expression to set
     */
    public void setExpression(String expression) {
        this.expression = expression;
    }
    
    /**
     * @param source the source to set
     */
    public void setSource(ConfigurationService source) {
        this.source = source;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
