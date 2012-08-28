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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.brekka.stillingar.core.properties.PropertiesConfigurationSourceLoader;
import org.brekka.stillingar.core.snapshot.SnapshotBasedConfigurationSource;
import org.brekka.stillingar.spring.ConfigurationBeanPostProcessor;
import org.brekka.stillingar.spring.ConfigurationPlaceholderConfigurer;
import org.brekka.stillingar.spring.LoggingSnapshotEventHandler;
import org.brekka.stillingar.spring.resource.BaseDirResolver;
import org.brekka.stillingar.spring.resource.BaseInHomeDirResolver;
import org.brekka.stillingar.spring.resource.ScanningResourceSelector;
import org.brekka.stillingar.spring.snapshot.ResourceSnapshotManager;
import org.brekka.stillingar.spring.xmlbeans.ApplicationContextConverter;
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

    private static final String LOADER_TYPE_PROPS = "props";
    private static final String LOADER_TYPE_XMLBEANS = "xmlbeans";

    /**
     * Default locations to scan, in case they are not defined.
     */
    private static final List<String> DEFAULT_LOCATION_LIST = Arrays.asList(
        /*
         * Tomcat
         */
        "file:${catalina.base}/conf/",
        /*
         * Glassfish
         */
        "file:${com.sun.aas.instanceRoot}/config/",
        /*
         * JBoss
         */
        "file:${jboss.server.home.dir}/conf/",
        /*
         * Weblogic
         */
        "file:${env.DOMAIN_HOME}/config/"
    );

	private static final int MINIMUM_RELOAD_INTERVAL = 1000;

	private static final Map<String, String> TYPE_ALIASES = new HashMap<String, String>();
	static {
	    TYPE_ALIASES.put(LOADER_TYPE_PROPS, PropertiesConfigurationSourceLoader.class.getName());
	    TYPE_ALIASES.put(LOADER_TYPE_XMLBEANS, "org.brekka.stillingar.xmlbeans.XmlBeansSnapshotLoader");
	}

    @Override
	protected Class<SnapshotBasedConfigurationSource> getBeanClass(Element element) {
		return SnapshotBasedConfigurationSource.class;
	}
	
	
	@Override
	protected void doParse(Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder) {
	    // ID used for the ConfigurationSource 
		String id = element.getAttribute("id");
		
		// Optional application name, will use the id if not specified.
		String applicationName = element.getAttribute("application-name");
		if (applicationName == null) {
		    applicationName = id;
		}
		ManagedList<Object> locations = new ManagedList<Object>();
		BeanDefinitionBuilder configBaseResolver = BeanDefinitionBuilder.genericBeanDefinition(BaseDirResolver.class);
		configBaseResolver.addConstructorArgValue(DEFAULT_LOCATION_LIST);
		BeanDefinitionBuilder homeConfigBaseResolver = BeanDefinitionBuilder.genericBeanDefinition(BaseInHomeDirResolver.class);
		homeConfigBaseResolver.addConstructorArgValue("." + applicationName);
		
		locations.add(configBaseResolver.getBeanDefinition());
		locations.add(homeConfigBaseResolver.getBeanDefinition());
		
		
		BeanDefinitionBuilder resourceNameResolver = BeanDefinitionBuilder.genericBeanDefinition(org.brekka.stillingar.spring.resource.BasicResourceNameResolver.class);
		resourceNameResolver.addConstructorArgValue(id);
		
		BeanDefinitionBuilder classpathBaseResolver = BeanDefinitionBuilder.genericBeanDefinition(BaseDirResolver.class);
        classpathBaseResolver.addConstructorArgValue(Arrays.asList("classpath:/stillingar/"));
        locations.add(classpathBaseResolver.getBeanDefinition());
		
		BeanDefinitionBuilder configSource = BeanDefinitionBuilder.genericBeanDefinition(ScanningResourceSelector.class);
		configSource.addConstructorArgValue(locations);
		configSource.addConstructorArgValue(locations);
		configSource.addConstructorArgValue(resourceNameResolver.getBeanDefinition());
		
		
		String typeKey = element.getAttribute("type");
		String type = LOADER_TYPE_PROPS;
		if (TYPE_ALIASES.containsKey(typeKey)) {
		    type = TYPE_ALIASES.get(typeKey);
		}
		
		BeanDefinitionBuilder snapshotLoader = BeanDefinitionBuilder.genericBeanDefinition(type);
		if (typeKey.equals(LOADER_TYPE_XMLBEANS)) {
		    BeanDefinitionBuilder conversionManager = BeanDefinitionBuilder.genericBeanDefinition("org.brekka.stillingar.xmlbeans.conversion.ConversionManager");
		    
		    ManagedList<Object> converters = prepareDefaultConverters();
		    BeanDefinitionBuilder applicationContextConverter = BeanDefinitionBuilder.genericBeanDefinition(ApplicationContextConverter.class);
		    converters.add(applicationContextConverter.getBeanDefinition());
		    
		    conversionManager.addConstructorArgValue(converters);
		    snapshotLoader.addConstructorArgValue(conversionManager.getBeanDefinition());
		}
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
		
		
		BeanDefinitionBuilder manager = BeanDefinitionBuilder.genericBeanDefinition(ResourceSnapshotManager.class);
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
                BeanDefinitionBuilder updateTask = BeanDefinitionBuilder.genericBeanDefinition(LoggingSnapshotEventHandler.class);
                updateTask.addConstructorArgReference(id);
                
                // Scheduled executor
                BeanDefinitionBuilder scheduledExecutorTask = BeanDefinitionBuilder.genericBeanDefinition(ScheduledExecutorTask.class);
                scheduledExecutorTask.addConstructorArgValue(updateTask.getBeanDefinition());
                scheduledExecutorTask.addPropertyValue("period", reloadInterval);
                scheduledExecutorTask.addPropertyValue("delay", reloadInterval);
                
                
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


    private ManagedList<Object> prepareDefaultConverters() {
        ManagedList<Object> converters = new ManagedList<Object>();
        List<String> converterShortNames = Arrays.asList("BigDecimalConverter", "BigIntegerConverter",
                "BooleanConverter", "ByteConverter", "ByteArrayConverter", "CalendarConverter", "DateConverter",
                "DoubleConverter", "ElementConverter", "FloatConverter", "IntegerConverter", "LongConverter",
                "ShortConverter", "StringConverter", "URIConverter", "DocumentConverter");
        for (String shortName : converterShortNames) {
            BeanDefinitionBuilder converterBldr = BeanDefinitionBuilder.genericBeanDefinition(
                    "org.brekka.stillingar.xmlbeans.conversion." + shortName);
            converters.add(converterBldr.getBeanDefinition());
        }
        return converters;
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
