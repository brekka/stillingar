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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.brekka.stillingar.core.ChangeAwareConfigurationSource;
import org.brekka.stillingar.core.ConfigurationSource;
import org.brekka.stillingar.core.GroupChangeListener;
import org.brekka.stillingar.core.ValueDefinition;
import org.brekka.stillingar.core.ValueDefinitionGroup;
import org.brekka.stillingar.spring.expr.ExpressionFragment;
import org.brekka.stillingar.spring.expr.DefaultPlaceholderParser;
import org.brekka.stillingar.spring.expr.Fragment;
import org.brekka.stillingar.spring.expr.PlaceholderParser;
import org.brekka.stillingar.spring.expr.StringFragment;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.util.StringValueResolver;

class CustomStringValueResolver implements StringValueResolver {
    
    private final PlaceholderParser placeholderHelper;
    
    private final ConfigurationSource configurationSource;
    
    private final BeanFactory beanFactory;
    
    private CustomBeanDefinitionVisitor beanDefVisitor;

    /**
     * @param beanDefVisitor
     * @param placeholderHelper
     * @param configurationSource
     * @param beanFactory
     */
    public CustomStringValueResolver(PlaceholderParser placeholderHelper, ConfigurationSource configurationSource,
            BeanFactory beanFactory) {
        this.placeholderHelper = placeholderHelper;
        this.configurationSource = configurationSource;
        this.beanFactory = beanFactory;
    }


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
        List<ExpressionFragment> expressionFragments = DefaultPlaceholderParser.findExpressionFragments(fragment);
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