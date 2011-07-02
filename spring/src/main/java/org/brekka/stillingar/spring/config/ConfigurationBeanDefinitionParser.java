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

package org.brekka.stillingar.spring.config;

import java.util.HashMap;
import java.util.Map;

import org.brekka.stillingar.core.DefaultConfigurationSource;
import org.brekka.stillingar.spring.ConfigBaseResolverFactoryBean;
import org.brekka.stillingar.spring.ConfigurationBeanPostProcessor;
import org.brekka.stillingar.spring.ConfigurationPlaceholderConfigurer;
import org.brekka.stillingar.spring.HomeConfigBaseResolverFactoryBean;
import org.brekka.stillingar.spring.LoggingBackgroundUpdater;
import org.brekka.stillingar.spring.ResourceBasedSnapshotManager;
import org.brekka.stillingar.spring.ResourceSelector;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.scheduling.concurrent.ScheduledExecutorFactoryBean;
import org.springframework.scheduling.concurrent.ScheduledExecutorTask;
import org.springframework.util.PropertyPlaceholderHelper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * TODO
 * @author Andrew Taylor
 */
public class ConfigurationBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	private static final int MINIMUM_RELOAD_INTERVAL = 1000;

	private static final Map<String, String> TYPE_ALIASES = new HashMap<String, String>();
	static {
	    TYPE_ALIASES.put("props", org.brekka.stillingar.core.PropertiesSnapshotLoader.class.getName());
	    TYPE_ALIASES.put("xmlbeans", "org.brekka.stillingar.xmlbeans.XmlBeansSnapshotLoader");
	}

    @Override
	protected Class<DefaultConfigurationSource> getBeanClass(Element element) {
		return DefaultConfigurationSource.class;
	}
	
	
	@Override
	protected void doParse(Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder) {
		String id = element.getAttribute("id");
		
		ManagedList<Object> locations = new ManagedList<Object>();
		BeanDefinitionBuilder configBaseResolver = BeanDefinitionBuilder.genericBeanDefinition(ConfigBaseResolverFactoryBean.class);
		BeanDefinitionBuilder homeConfigBaseResolver = BeanDefinitionBuilder.genericBeanDefinition(HomeConfigBaseResolverFactoryBean.class);
		homeConfigBaseResolver.addConstructorArgValue("." + id);
		locations.add(configBaseResolver.getBeanDefinition());
		locations.add(homeConfigBaseResolver.getBeanDefinition());
		
		BeanDefinitionBuilder resourceNameResolver = BeanDefinitionBuilder.genericBeanDefinition(org.brekka.stillingar.spring.BasicResourceNameResolver.class);
		resourceNameResolver.addConstructorArgValue(id);
		
		BeanDefinitionBuilder configSource = BeanDefinitionBuilder.genericBeanDefinition(ResourceSelector.class);
		configSource.addConstructorArgValue(locations);
		configSource.addConstructorArgValue(resourceNameResolver.getBeanDefinition());
		
		String type = element.getAttribute("type");
		if (TYPE_ALIASES.containsKey(type)) {
		    type = TYPE_ALIASES.get(type);
		}
		
		BeanDefinitionBuilder snapshotLoader = BeanDefinitionBuilder.genericBeanDefinition(type);
		NodeList nodeList = element.getChildNodes();
		ManagedMap<String, String> nsMap = new ManagedMap<String, String>();
		boolean placeHolderSet = false;
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node item = nodeList.item(i);
			if (item instanceof Element) {
				Element nsElem = (Element) item;
				if (nsElem.getLocalName().equals("namespace")) {
				    String prefix = nsElem.getAttribute("prefix");
				    String url = nsElem.getAttribute("url");
				    nsMap.put(prefix, url);
				} else if (nsElem.getLocalName().equals("property-placeholder") 
				        && !placeHolderSet) {
				    String prefix = nsElem.getAttribute("prefix");
                    String suffix = nsElem.getAttribute("suffix");
                    preparePlaceholderConfigurer(prefix, suffix, id, parserContext);
                    placeHolderSet = true;
				}
			}
		}
		snapshotLoader.addPropertyValue("xpathNamespaces", nsMap);
		
		
		BeanDefinitionBuilder manager = BeanDefinitionBuilder.genericBeanDefinition(ResourceBasedSnapshotManager.class);
		manager.addConstructorArgValue(configSource.getBeanDefinition());
		manager.addConstructorArgValue(snapshotLoader.getBeanDefinition());
		
		builder.addConstructorArgValue(manager.getBeanDefinition());
		builder.getRawBeanDefinition().setInitMethodName("init");
		
		// Bean post processor
		BeanDefinitionBuilder postProcessor = BeanDefinitionBuilder.genericBeanDefinition(ConfigurationBeanPostProcessor.class);
		postProcessor.addConstructorArgReference(id);
		parserContext.registerBeanComponent(new BeanComponentDefinition(postProcessor.getBeanDefinition(), id + "-PostProcessor"));
		
		String attribute = element.getAttribute("reload-interval");
        if (attribute != null) {
            int reloadInterval = 0;
            try {
                reloadInterval = Integer.valueOf(attribute);
            } catch (NumberFormatException e) {
                // Bad number
                // TODO log warning
            }
            if (reloadInterval >= MINIMUM_RELOAD_INTERVAL) {
                // Update task
                BeanDefinitionBuilder updateTask = BeanDefinitionBuilder.genericBeanDefinition(LoggingBackgroundUpdater.class);
                updateTask.addConstructorArgReference(id);
                
                // Scheduled executor
                BeanDefinitionBuilder scheduledExecutorTask = BeanDefinitionBuilder.genericBeanDefinition(ScheduledExecutorTask.class);
                scheduledExecutorTask.addConstructorArgValue(updateTask.getBeanDefinition());
                scheduledExecutorTask.addPropertyValue("period", reloadInterval);
                
                
                ManagedList<Object> taskList = new ManagedList<Object>();
                taskList.add(scheduledExecutorTask.getBeanDefinition());
                
                // Scheduler factory bean
                BeanDefinitionBuilder scheduledExecutorFactoryBean = BeanDefinitionBuilder.genericBeanDefinition(ScheduledExecutorFactoryBean.class);
                scheduledExecutorFactoryBean.addPropertyValue("scheduledExecutorTasks", taskList);
                scheduledExecutorFactoryBean.addPropertyValue("threadNamePrefix", id + "-reloader");
                parserContext.registerBeanComponent(new BeanComponentDefinition(scheduledExecutorFactoryBean.getBeanDefinition(), id + "-Scheduler"));
            }
        }
	}


    protected void preparePlaceholderConfigurer(String prefix, String suffix, String id, ParserContext parserContext) {
        // ConfigurationPlaceholderConfigurer
        
        BeanDefinitionBuilder placeholderConfigurer = BeanDefinitionBuilder.genericBeanDefinition(ConfigurationPlaceholderConfigurer.class);
        placeholderConfigurer.addConstructorArgReference(id);
        
        if (prefix == null || prefix.isEmpty()) {
            prefix = "$" + id + "{";
        }
        if (suffix == null || suffix.isEmpty()) {
            suffix = "}";
        }
        BeanDefinitionBuilder placeholderHelper = BeanDefinitionBuilder.genericBeanDefinition(PropertyPlaceholderHelper.class);
        placeholderHelper.addConstructorArgValue(prefix);
        placeholderHelper.addConstructorArgValue(suffix);
        
        placeholderConfigurer.addPropertyValue("placeholderHelper", placeholderHelper.getBeanDefinition());
        
        parserContext.registerBeanComponent(new BeanComponentDefinition(placeholderConfigurer.getBeanDefinition(), 
                id + "-PlaceholderConfigurer"));
    }

}
