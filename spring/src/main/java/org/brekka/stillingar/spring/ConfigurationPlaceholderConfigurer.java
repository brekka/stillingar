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

package org.brekka.stillingar.spring;

import org.brekka.stillingar.core.ConfigurationSource;
import org.brekka.stillingar.core.UpdatableConfigurationSource;
import org.brekka.stillingar.core.ValueChangeListener;
import org.brekka.stillingar.core.ValueDefinition;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
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
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.util.ObjectUtils;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;
import org.springframework.util.StringValueResolver;

/**
 * TODO
 * 
 * @author Andrew Taylor
 */
public class ConfigurationPlaceholderConfigurer implements
		BeanFactoryPostProcessor, BeanFactoryAware, BeanNameAware, InitializingBean {

    /**
     * The source for configuration values
     */
    private final ConfigurationSource configurationSource;
    
	/**
	 * Simply returns the value within the placeholder.
	 */
	private final PlaceholderResolver resolver = new CustomPlaceholderResolver();

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
	private PropertyPlaceholderHelper placeholderHelper;
	
	
	
	public ConfigurationPlaceholderConfigurer(ConfigurationSource configurationSource) {
		this.configurationSource = configurationSource;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
	    if (placeholderHelper == null) {
	        placeholderHelper = new PropertyPlaceholderHelper("${", "}");
	    }
	}
	
	/**
	 * Based on logic from
	 * {@link PropertyPlaceholderConfigurer#postProcessBeanFactory(ConfigurableListableBeanFactory)}
	 * .
	 */
	@Override
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactoryToProcess)
			throws BeansException {

		String[] beanNames = beanFactoryToProcess.getBeanDefinitionNames();
		for (String curName : beanNames) {
			CustomStringValueResolver valueResolver = new CustomStringValueResolver();
			BeanDefinitionVisitor visitor = new CustomBeanDefinitionVisitor(curName, valueResolver);
			
			// Check that we're not parsing our own bean definition,
			// to avoid failing on unresolvable placeholders in properties file
			// locations.
			if (!(curName.equals(this.beanName) && beanFactoryToProcess
					.equals(this.beanFactory))) {
				BeanDefinition bd = beanFactoryToProcess
						.getBeanDefinition(curName);
				try {
					visitor.visitBeanDefinition(bd);
				} catch (Exception ex) {
					throw new BeanDefinitionStoreException(
							bd.getResourceDescription(), curName,
							ex.getMessage());
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
	
	public void setPlaceholderHelper(PropertyPlaceholderHelper placeholderHelper) {
        this.placeholderHelper = placeholderHelper;
    }
	
	private class CustomBeanDefinitionVisitor extends BeanDefinitionVisitor {
		private final String beanName;
		
		private PropertyValue currentProperty;

		public CustomBeanDefinitionVisitor(String beanName, CustomStringValueResolver valueResolver) {
			super(valueResolver);
			this.beanName = beanName;
			valueResolver.setBeanDefVisitor(this);
		}

		@Override
		protected void visitPropertyValues(MutablePropertyValues pvs) {
			PropertyValue[] pvArray = pvs.getPropertyValues();
			for (PropertyValue pv : pvArray) {
				currentProperty = pv;
				Object newVal = resolveValue(pv.getValue());
				if (!ObjectUtils.nullSafeEquals(newVal, pv.getValue())) {
					pvs.add(pv.getName(), newVal);
				}
				currentProperty = null;
			}
		}
	}

	private class CustomPlaceholderResolver implements PlaceholderResolver {
		@Override
		public String resolvePlaceholder(String expression) {
			return expression;
		}
	}

	private class CustomStringValueResolver implements StringValueResolver {
		private CustomBeanDefinitionVisitor beanDefVisitor;
		
		@Override
		public String resolveStringValue(String strVal) {
			String value = strVal;
			String expression = placeholderHelper.replacePlaceholders(strVal, resolver);
			if (!expression.equals(strVal)) {
				// Something changed
				value = configurationSource.retrieve(String.class,
						expression);
				if (beanDefVisitor != null 
						&& beanDefVisitor.currentProperty != null
						&& configurationSource instanceof UpdatableConfigurationSource
						&& beanFactory.isSingleton(beanDefVisitor.beanName)) {
					UpdatableConfigurationSource ucs = (UpdatableConfigurationSource) configurationSource;
					BeanPropertyChangeListener listener = new BeanPropertyChangeListener(beanDefVisitor.beanName, beanDefVisitor.currentProperty.getName(), beanFactory);
					ValueDefinition<String> vd = new ValueDefinition<String>(String.class, expression, listener, false);
					ucs.register(vd, false);
				}
			}
			return value;
		}
		
		public void setBeanDefVisitor(CustomBeanDefinitionVisitor beanDefVisitor) {
			this.beanDefVisitor = beanDefVisitor;
		}
	}

	private static class BeanPropertyChangeListener implements
			ValueChangeListener<String> {
		private final String beanName;
		private final String property;
		private final BeanFactory beanFactory;
		
		public BeanPropertyChangeListener(String beanName, String property,
				BeanFactory beanFactory) {
			this.beanName = beanName;
			this.property = property;
			this.beanFactory = beanFactory;
		}

		public void onChange(String newValue) {
			Object bean = beanFactory.getBean(beanName);
			BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
			beanWrapper.setPropertyValue(new PropertyValue(property, newValue));
		}
	}
}
