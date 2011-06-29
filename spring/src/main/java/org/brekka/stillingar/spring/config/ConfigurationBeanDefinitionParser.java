package org.brekka.stillingar.spring.config;

import java.util.ArrayList;
import java.util.List;

import org.brekka.stillingar.core.DefaultConfigurationSource;
import org.brekka.stillingar.spring.ConfigBaseResolverFactoryBean;
import org.brekka.stillingar.spring.ConfigurationBeanPostProcessor;
import org.brekka.stillingar.spring.HomeConfigBaseResolverFactoryBean;
import org.brekka.stillingar.spring.LoggingBackgroundUpdater;
import org.brekka.stillingar.spring.ResourceBasedSnapshotManager;
import org.brekka.stillingar.spring.ResourceSelector;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.scheduling.concurrent.ScheduledExecutorFactoryBean;
import org.springframework.scheduling.concurrent.ScheduledExecutorTask;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ConfigurationBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

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
		
		BeanDefinitionBuilder snapshotLoader = BeanDefinitionBuilder.genericBeanDefinition("org.brekka.stillingar.xmlbeans.XmlBeansSnapshotLoader");
		NodeList nodeList = element.getChildNodes();
		ManagedMap<String, String> nsMap = new ManagedMap<String, String>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node item = nodeList.item(i);
			if (item instanceof Element
					&& item.getLocalName().equals("xpath-namespace")) {
				Element nsElem = (Element) item;
				String prefix = nsElem.getAttribute("prefix");
				String url = nsElem.getAttribute("url");
				nsMap.put(prefix, url);
			}
		}
		snapshotLoader.addPropertyValue("xpathNamespaces", nsMap);
		
		
		BeanDefinitionBuilder manager = BeanDefinitionBuilder.genericBeanDefinition(ResourceBasedSnapshotManager.class);
		manager.addConstructorArgValue(configSource.getBeanDefinition());
		manager.addConstructorArgValue(snapshotLoader.getBeanDefinition());
		
		builder.addConstructorArgValue(manager.getBeanDefinition());
		builder.getRawBeanDefinition().setInitMethodName("init");
		
		BeanDefinitionBuilder postProc = BeanDefinitionBuilder.genericBeanDefinition(ConfigurationBeanPostProcessor.class);
		
		AbstractBeanDefinition configurationSourceBeanDefinition = builder.getBeanDefinition();
		
		postProc.addPropertyValue("configurationSource", configurationSourceBeanDefinition);
		
		parserContext.registerBeanComponent(new BeanComponentDefinition(postProc.getBeanDefinition(), id + "-PostProcessor"));
		
		
		BeanDefinitionBuilder updateTask = BeanDefinitionBuilder.genericBeanDefinition(LoggingBackgroundUpdater.class);
		updateTask.addConstructorArgValue(configurationSourceBeanDefinition);
		
		BeanDefinitionBuilder scheduledExecutorTask = BeanDefinitionBuilder.genericBeanDefinition(ScheduledExecutorTask.class);
		scheduledExecutorTask.addConstructorArgValue(updateTask.getBeanDefinition());
		String attribute = element.getAttribute("reload-interval");
		if (attribute != null) {
			scheduledExecutorTask.addPropertyValue("period", Integer.valueOf(attribute));
		}
		
		ManagedList<Object> taskList = new ManagedList<Object>();
		taskList.add(scheduledExecutorTask.getBeanDefinition());
		
		BeanDefinitionBuilder scheduledExecutorFactoryBean = BeanDefinitionBuilder.genericBeanDefinition(ScheduledExecutorFactoryBean.class);
		scheduledExecutorFactoryBean.addPropertyValue("scheduledExecutorTasks", taskList);
		scheduledExecutorFactoryBean.addPropertyValue("threadNamePrefix", id + "-reloader");
		
		parserContext.registerBeanComponent(new BeanComponentDefinition(scheduledExecutorFactoryBean.getBeanDefinition(), id + "-Scheduler"));
		
	}

}
