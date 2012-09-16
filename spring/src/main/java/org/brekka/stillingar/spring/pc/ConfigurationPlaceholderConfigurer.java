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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.brekka.stillingar.core.ChangeAwareConfigurationSource;
import org.brekka.stillingar.core.ConfigurationSource;
import org.brekka.stillingar.core.GroupChangeListener;
import org.brekka.stillingar.core.ValueDefinition;
import org.brekka.stillingar.core.ValueDefinitionGroup;
import org.brekka.stillingar.spring.expr.ExpressionFragment;
import org.brekka.stillingar.spring.expr.ExpressionPlaceholderHelper;
import org.brekka.stillingar.spring.expr.Fragment;
import org.brekka.stillingar.spring.expr.StringFragment;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionVisitor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.util.StringValueResolver;

/**
 * Identifies and replaces placeholders defined within a Spring configuration with values obtained from a
 * {@link ConfigurationSource}.
 * 
 * Inspired by the reloadable properties example found here: http://www.wuenschenswert.net/wunschdenken/archives/127.
 * 
 * @author Andrew Taylor
 */
public class ConfigurationPlaceholderConfigurer implements BeanFactoryPostProcessor, BeanFactoryAware, BeanNameAware,
        InitializingBean {

    /**
     * The source for configuration values
     */
    private final ConfigurationSource configurationSource;

    /**
     * The bean factory that loaded this instance
     */
    private BeanFactory beanFactory;

    /**
     * The name of this bean within the container.
     */
    private String beanName;

    /**
     * Used to parse the placeholder string.
     */
    private ExpressionPlaceholderHelper placeholderHelper;

    public ConfigurationPlaceholderConfigurer(ConfigurationSource configurationSource) {
        this.configurationSource = configurationSource;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (placeholderHelper == null) {
            placeholderHelper = new ExpressionPlaceholderHelper("${", "}");
        }
    }

    /**
     * Copied in its entirety from the {@link PropertyPlaceholderConfigurer} method of the same name. The only changes
     * are to the valueResolver and BeanDefinitionVisitor instances.
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactoryToProcess) throws BeansException {

        String[] beanNames = beanFactoryToProcess.getBeanDefinitionNames();
        for (String curName : beanNames) {
            CustomStringValueResolver valueResolver = new CustomStringValueResolver();

            // Check that we're not parsing our own bean definition,
            // to avoid failing on unresolvable placeholders in properties file
            // locations.
            if (!(curName.equals(this.beanName) 
                    && beanFactoryToProcess.equals(this.beanFactory))) {
                BeanDefinition beanDef = beanFactoryToProcess.getBeanDefinition(curName);
                try {
                    BeanDefinitionVisitor visitor = new CustomBeanDefinitionVisitor(curName, beanDef.isSingleton(), valueResolver);
                    visitor.visitBeanDefinition(beanDef);
                } catch (Exception ex) {
                    throw new BeanDefinitionStoreException(beanDef.getResourceDescription(), curName, ex);
                }
            }
        }

        StringValueResolver valueResolver = new CustomStringValueResolver();

        // New in Spring 2.5: resolve placeholders in alias target names and
        // aliases as well.
        beanFactoryToProcess.resolveAliases(valueResolver);

        // New in Spring 3.0: resolve placeholders in embedded values such as
        // annotation attributes.
        beanFactoryToProcess.addEmbeddedValueResolver(valueResolver);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    public void setPlaceholderHelper(ExpressionPlaceholderHelper placeholderHelper) {
        this.placeholderHelper = placeholderHelper;
    }

    
    class CustomStringValueResolver implements StringValueResolver {
        private CustomBeanDefinitionVisitor beanDefVisitor;

        @Override
        public String resolveStringValue(String strVal) {
            Fragment fragment = placeholderHelper.parse(strVal);
            if (fragment instanceof StringFragment) {
                return ((StringFragment) fragment).evaluate(null, null);
            }
            
            String value = fragment.evaluate(configurationSource, new HashSet<String>());
            
            if (beanDefVisitor != null
                    && configurationSource instanceof ChangeAwareConfigurationSource) {
                ChangeAwareConfigurationSource ucs = (ChangeAwareConfigurationSource) configurationSource;
                PropertyValue currentProperty = beanDefVisitor.getCurrentProperty();
                ValueHolder currentConstructorValue = beanDefVisitor.getCurrentConstructorValue();
                String beanName = beanDefVisitor.getBeanName();
                GroupChangeListener listener = null;
                if (currentProperty != null) {
                    if (beanDefVisitor.isSingleton()) {
                        // Singleton, we update the single bean instance
                        listener = new BeanPropertyChangeListener(beanName,
                                currentProperty.getName(), beanFactory, fragment);
                    } else if (beanFactory instanceof ConfigurableListableBeanFactory) {
                        // Prototype (or other) - we update the definition.
                        listener = new PropertyDefChangeListener(beanName, currentProperty.getName(), 
                                (ConfigurableListableBeanFactory) beanFactory, fragment);
                    }
                } else if (currentConstructorValue != null) {
                    // Constructor handling. Can't change the value, but can change the definition
                    listener = new ConstructorArgDefChangeListener(beanName, 
                            beanDefVisitor.getCurrentConstructorIndex(),
                            currentConstructorValue.getType(),
                            (ConfigurableListableBeanFactory) beanFactory, fragment);
                }
                if (listener != null) {
                    List<ValueDefinition<?>> values = toValueDefinitions(fragment);
                    ValueDefinitionGroup group = new ValueDefinitionGroup(beanName, values, listener);
                    ucs.register(group, false);
                }
            }
            return value;
        }
        

        /**
         * Extract all {@link ExpressionFragment}s from the given fragment and generate {@link ValueDefinition} for each
         * one, returning the list of all encountered.
         * 
         * @param fragment
         *            the fragment to extract {@link ValueDefinition}s from.
         * @return the list of value definitions (never null).
         */
        public List<ValueDefinition<?>> toValueDefinitions(Fragment fragment) {
            List<ExpressionFragment> expressionFragments = ExpressionPlaceholderHelper.findExpressionFragments(fragment);
            List<ValueDefinition<?>> valueDefs = new ArrayList<ValueDefinition<?>>(expressionFragments.size());
            for (ExpressionFragment expressionFragment : expressionFragments) {
                ExpressionFragmentChangeListener changeListener = new ExpressionFragmentChangeListener(expressionFragment);
                ValueDefinition<String> valueDefinition = new ValueDefinition<String>(String.class, expressionFragment.getExpression(), changeListener, false);
                valueDefs.add(valueDefinition);
            }
            return valueDefs;
        }

        public void setBeanDefVisitor(CustomBeanDefinitionVisitor beanDefVisitor) {
            this.beanDefVisitor = beanDefVisitor;
        }
    }
}
