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

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.brekka.stillingar.api.ConfigurationException;
import org.brekka.stillingar.core.conversion.ConversionManager;
import org.brekka.stillingar.core.conversion.TemporalAdapter;
import org.brekka.stillingar.core.conversion.xml.DocumentConverter;
import org.brekka.stillingar.core.dom.DOMConfigurationSourceLoader;
import org.brekka.stillingar.core.dom.DefaultNamespaceContext;
import org.brekka.stillingar.core.properties.PropertiesConfigurationSourceLoader;
import org.brekka.stillingar.core.snapshot.SnapshotBasedConfigurationService;
import org.brekka.stillingar.spring.bpp.ConfigurationBeanPostProcessor;
import org.brekka.stillingar.spring.bpp.NamespaceBeanFactoryPostProcessor;
import org.brekka.stillingar.spring.converter.ApplicationContextConverter;
import org.brekka.stillingar.spring.expr.DefaultPlaceholderParser;
import org.brekka.stillingar.spring.pc.ConfigurationPlaceholderConfigurer;
import org.brekka.stillingar.spring.resource.BasicResourceNameResolver;
import org.brekka.stillingar.spring.resource.FixedResourceSelector;
import org.brekka.stillingar.spring.resource.ScanningResourceSelector;
import org.brekka.stillingar.spring.resource.VersionedResourceNameResolver;
import org.brekka.stillingar.spring.resource.dir.EnvironmentVariableDirectory;
import org.brekka.stillingar.spring.resource.dir.HomeDirectory;
import org.brekka.stillingar.spring.resource.dir.ResourceDirectory;
import org.brekka.stillingar.spring.resource.dir.PlatformDirectory;
import org.brekka.stillingar.spring.resource.dir.SystemPropertyDirectory;
import org.brekka.stillingar.spring.resource.dir.WebappDirectory;
import org.brekka.stillingar.spring.snapshot.ConfigurationSnapshotRefresher;
import org.brekka.stillingar.spring.snapshot.LoggingSnapshotEventHandler;
import org.brekka.stillingar.spring.snapshot.NoopResourceMonitor;
import org.brekka.stillingar.spring.snapshot.PollingResourceMonitor;
import org.brekka.stillingar.spring.snapshot.ResourceSnapshotManager;
import org.brekka.stillingar.spring.snapshot.SnapshotDeltaValueInterceptor;
import org.brekka.stillingar.spring.version.ApplicationVersionFromMaven;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.concurrent.ScheduledExecutorFactoryBean;
import org.springframework.scheduling.concurrent.ScheduledExecutorTask;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parser that converts the stil:configuration XML element into bean definitions for the Spring container. A bean
 * definition for the {@link SnapshotBasedConfigurationService} class will be prepared as the primary bean, but
 * additional bean definitions may also be added as required.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
class ConfigurationServiceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    private static final int MINIMUM_RELOAD_INTERVAL = 500;

    /**
     * Determine if Watchable is available (Java 7).
     */
    private boolean watchableAvailable = ClassUtils.isPresent("java.nio.file.Watchable",
            this.getClass().getClassLoader()) && !"true".equals(System.getProperty("stillingar.reload-watcher.disabled"));

    @Override
    protected Class<SnapshotBasedConfigurationService> getBeanClass(final Element element) {
        return SnapshotBasedConfigurationService.class;
    }

    private String namespacesId;

    @Override
    protected void doParse(final Element element, final ParserContext parserContext, final BeanDefinitionBuilder builder) {

        Engine engine = determineEngine(element);

        builder.addConstructorArgValue(prepareResourceManager(element, engine, parserContext));
        builder.addConstructorArgValue("true".equals(element.getAttribute("snapshot-required")));
        builder.addConstructorArgValue(prepareDefaultConfigurationSource(element, engine));
        prepareSnapshotEventHandler(element, builder);
        builder.addPropertyValue("deltaValueInterceptor", prepareDeltaValueInterceptor(element));
        builder.getRawBeanDefinition().setDestroyMethodName("shutdown");

        prepareNamespaceContext(element, parserContext);

        // Other identifiable context beans
        prepareNamespaces(element, parserContext);
        prepareLoader(element, parserContext, engine);
        preparePlaceholderConfigurer(element, parserContext);
        preparePostProcessor(element, parserContext);
        prepareReloadMechanism(element, parserContext);
    }


    private void prepareNamespaceContext(final Element element, final ParserContext parserContext) {
        String id = element.getAttribute("id");
        namespacesId = id + "-Namespaces";
        if (!parserContext.getRegistry().containsBeanDefinition(namespacesId)) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(DefaultNamespaceContext.class);
            builder.addConstructorArgValue(new String[0]);
            parserContext.registerBeanComponent(new BeanComponentDefinition(
                builder.getBeanDefinition(), this.namespacesId));
        }

        // Register the post processor if there is not already one
        String beanName = NamespaceBeanFactoryPostProcessor.class.getName();
        if (!parserContext.getRegistry().containsBeanDefinition(beanName)) {
            BeanDefinitionBuilder namespacePostProcessor = BeanDefinitionBuilder.genericBeanDefinition(NamespaceBeanFactoryPostProcessor.class);
            parserContext.registerBeanComponent(new BeanComponentDefinition(namespacePostProcessor.getBeanDefinition(), beanName));
        }
    }


    protected void preparePostProcessor(final Element element, final ParserContext parserContext) {
        String id = element.getAttribute("id");
        String name = getName(element);
        BeanDefinitionBuilder postProcessor = BeanDefinitionBuilder
                .genericBeanDefinition(ConfigurationBeanPostProcessor.class);
        postProcessor.addConstructorArgValue(name);
        postProcessor.addConstructorArgReference(id);
        Element annotationConfigElement = selectSingleChildElement(element, "annotation-config", true);
        if (annotationConfigElement != null) {
            String marker = annotationConfigElement.getAttribute("marker");
            if (StringUtils.hasLength(marker)) {
                Class<?> theClass = ClassUtils.resolveClassName(marker, Thread.currentThread().getContextClassLoader());
                if (!theClass.isAnnotation()) {
                    throw new ConfigurationException(String.format("The class '%s' is not an annotation", marker));
                }
                postProcessor.addPropertyValue("markerAnnotation", theClass);
            }
        }
        parserContext.registerBeanComponent(new BeanComponentDefinition(postProcessor.getBeanDefinition(), id
                + "-postProcessor"));
    }

    /**
     * @param element
     * @param parserContext
     */
    protected void preparePlaceholderConfigurer(final Element element, final ParserContext parserContext) {
        Element placeholderElement = selectSingleChildElement(element, "property-placeholder", true);
        if (placeholderElement != null) {
            String id = element.getAttribute("id");
            String name = getName(element);
            String prefix = placeholderElement.getAttribute("prefix");
            String suffix = placeholderElement.getAttribute("suffix");

            BeanDefinitionBuilder placeholderConfigurer = BeanDefinitionBuilder
                    .genericBeanDefinition(ConfigurationPlaceholderConfigurer.class);
            placeholderConfigurer.addConstructorArgReference(id);

            if (!StringUtils.hasText(prefix)) {
                prefix = "$" + name + "{";
            }
            if (!StringUtils.hasText(suffix)) {
                suffix = "}";
            }
            BeanDefinitionBuilder placeholderParser = BeanDefinitionBuilder
                    .genericBeanDefinition(DefaultPlaceholderParser.class);
            placeholderParser.addConstructorArgValue(prefix);
            placeholderParser.addConstructorArgValue(suffix);

            placeholderConfigurer.addPropertyValue("placeholderParser", placeholderParser.getBeanDefinition());

            parserContext.registerBeanComponent(new BeanComponentDefinition(placeholderConfigurer.getBeanDefinition(),
                    id + "-placeholderConfigurer"));
        }
    }


    /**
     * @param element
     * @param parserContext
     */
    protected void prepareLoader(final Element element, final ParserContext parserContext, final Engine engine) {
        String loaderReference = getLoaderReference(element);
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(engine.getLoaderClassName());

        switch (engine) {
            case XMLBEANS:
                prepareXmlBeans(element, parserContext, builder);
                break;
            case DOM:
                prepareDOM(element, parserContext, builder);
                break;
            case JAXB:
                prepareJAXB(element, parserContext, builder);
                break;
            case JSON:
                prepareJson(element, builder);
                break;
            case PROPS:
                // No extra handling for properties
                break;
            default:
                // No special requirements
                break;
        }
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        parserContext.registerBeanComponent(new BeanComponentDefinition(beanDefinition, loaderReference));
    }


    /**
     * @param element
     * @param parserContext
     * @param builder
     */
    protected void prepareXmlBeans(final Element element, final ParserContext parserContext, final BeanDefinitionBuilder builder) {
        // ConversionManager
        builder.addConstructorArgValue(prepareXmlBeansConversionManager());

        // Namespaces
        builder.addConstructorArgReference(this.namespacesId);
    }

    /**
     * @param element
     * @param parserContext
     * @param builder
     */
    protected void prepareDOM(final Element element, final ParserContext parserContext, final BeanDefinitionBuilder builder) {
        // ConversionManager
        builder.addConstructorArgValue(prepareDOMConversionManager());

        // Namespaces
        builder.addConstructorArgReference(this.namespacesId);
    }

    /**
     * @param element
     * @param builder
     */
    protected void prepareJAXB(final Element element, final ParserContext parserContext, final BeanDefinitionBuilder builder) {
        Element jaxbElement = selectSingleChildElement(element, "jaxb", false);

        // Path
        String contextPath = jaxbElement.getAttribute("context-path");
        builder.addConstructorArgValue(contextPath);

        // Schemas
        List<Element> schemaElementList = selectChildElements(jaxbElement, "schema");
        ManagedList<URL> schemaUrlList = new ManagedList<URL>(schemaElementList.size());
        for (Element schemaElement : schemaElementList) {
            String schemaPath = schemaElement.getTextContent();
            try {
                Resource resource = parserContext.getReaderContext().getResourceLoader().getResource(schemaPath);
                schemaUrlList.add(resource.getURL());
            } catch (IOException e) {
                throw new ConfigurationException(String.format(
                        "Failed to parse schema location '%s'", schemaPath), e);
            }
        }
        builder.addConstructorArgValue(schemaUrlList);

        // Namespaces
        builder.addConstructorArgReference(this.namespacesId);

        // ConversionManager
        builder.addConstructorArgValue(prepareJAXBConversionManager());
    }

    protected void prepareJson(final Element element, final BeanDefinitionBuilder builder) {
        Element jsonElement = selectSingleChildElement(element, "json", false);

        // Object mapper
        if (jsonElement.hasAttribute("object-mapper-ref")) {
            String objectMapperRef = jsonElement.getAttribute("object-mapper-ref");
            builder.addConstructorArgReference(objectMapperRef);
        } else {
            builder.addConstructorArgValue(prepareObjectMapper());
        }

        // RootNode class
        String rootNodeClass = jsonElement.getAttribute("root-node-class");
        builder.addConstructorArgValue(rootNodeClass);


        // ConversionManager
        builder.addConstructorArgValue(prepareJsonConversionManager());
    }


    /**
     * @param element
     * @return
     */
    protected void prepareNamespaces(final Element element, final ParserContext parserContext) {
        List<Element> namespaceElements = selectChildElements(element, "namespace");
        ManagedMap<String, String> map = new ManagedMap<String, String>();
        for (Element namespaceElement : namespaceElements) {
            map.put(namespaceElement.getAttribute("uri"), namespaceElement.getAttribute("prefix"));
        }
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(NamespaceRegisteringBean.class);
        builder.addConstructorArgReference(namespacesId);
        builder.addConstructorArgValue(map);
        parserContext.registerBeanComponent(new BeanComponentDefinition(builder.getBeanDefinition(), element.getAttribute("id") + "-ns"));
    }

    protected void prepareReloadMechanism(final Element element, final ParserContext parserContext) {
        String id = element.getAttribute("id");
        String reloadIntervalStr = element.getAttribute("reload-interval");
        if (StringUtils.hasLength(reloadIntervalStr)) {
            int reloadInterval = 0;
            try {
                reloadInterval = Integer.valueOf(reloadIntervalStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("The attribute reload-interval is invalid", e);
            }
            if (reloadInterval >= MINIMUM_RELOAD_INTERVAL) {
                // Update task
                BeanDefinitionBuilder updateTask = BeanDefinitionBuilder
                        .genericBeanDefinition(ConfigurationSnapshotRefresher.class);
                updateTask.addConstructorArgReference(id);

                // Scheduled executor
                BeanDefinitionBuilder scheduledExecutorTask = BeanDefinitionBuilder
                        .genericBeanDefinition(ScheduledExecutorTask.class);
                scheduledExecutorTask.addConstructorArgValue(updateTask.getBeanDefinition());
                if (watchableAvailable) {
                    /*
                     * The WatchedResourceMonitor is blocking with a timeout of reloadInterval. Must set a period, choose
                     * an interval of 1s.
                     */
                    scheduledExecutorTask.addPropertyValue("period", 1000);
                    scheduledExecutorTask.addPropertyValue("delay", 1000);
                } else {
                    scheduledExecutorTask.addPropertyValue("period", reloadInterval);
                    scheduledExecutorTask.addPropertyValue("delay", reloadInterval);
                }

                ManagedList<Object> taskList = new ManagedList<Object>();
                taskList.add(scheduledExecutorTask.getBeanDefinition());

                // Scheduler factory bean
                BeanDefinitionBuilder scheduledExecutorFactoryBean = BeanDefinitionBuilder
                        .genericBeanDefinition(ScheduledExecutorFactoryBean.class);
                scheduledExecutorFactoryBean.addPropertyValue("scheduledExecutorTasks", taskList);
                scheduledExecutorFactoryBean.addPropertyValue("threadNamePrefix", id + "-reloader");
                scheduledExecutorFactoryBean.addPropertyValue("daemon", Boolean.TRUE);
                parserContext.registerBeanComponent(new BeanComponentDefinition(scheduledExecutorFactoryBean
                        .getBeanDefinition(), id + "-Scheduler"));
            }
        }
    }

    /**
     * @param element
     * @return
     */
    protected AbstractBeanDefinition prepareResourceManager(final Element element, final Engine engine, final ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ResourceSnapshotManager.class);
        builder.addConstructorArgValue(prepareResourceSelector(element, engine, parserContext));
        builder.addConstructorArgReference(getLoaderReference(element));
        builder.addConstructorArgValue(prepareResourceMonitor(element));
        Element handlers = selectSingleChildElement(element, "handlers", true);
        if (handlers != null) {
            String rejectedRef = handlers.getAttribute("rejected-ref");
            if (StringUtils.hasLength(rejectedRef)) {
                builder.addPropertyReference("rejectedResourceHandler", rejectedRef);
            }
        }
        return builder.getBeanDefinition();
    }

    /**
     * @param element
     * @return
     */
    protected AbstractBeanDefinition prepareResourceMonitor(final Element element) {
        BeanDefinitionBuilder builder = null;
        String reloadIntervalStr = element.getAttribute("reload-interval");
        if (StringUtils.hasLength(reloadIntervalStr)) {
            if (watchableAvailable) { // Must have a reload-interval to use watched.
                builder = BeanDefinitionBuilder.genericBeanDefinition("org.brekka.stillingar.spring.snapshot.WatchedResourceMonitor");
                builder.addConstructorArgValue(Integer.valueOf(reloadIntervalStr));
            } else {
                builder = BeanDefinitionBuilder.genericBeanDefinition(PollingResourceMonitor.class);
            }
        } else {
            builder = BeanDefinitionBuilder.genericBeanDefinition(NoopResourceMonitor.class);
        }
        return builder.getBeanDefinition();
    }

    /**
     * @param element
     * @return
     */
    protected AbstractBeanDefinition prepareResourceSelector(final Element element, final Engine engine, final ParserContext parserContext) {
        String path = element.getAttribute("path");
        BeanDefinitionBuilder builder;
        if (path != null && !path.isEmpty()) {
            builder = BeanDefinitionBuilder.genericBeanDefinition(FixedResourceSelector.class);
            builder.addConstructorArgValue(parserContext.getReaderContext().getResourceLoader().getResource(path));
        } else {
            builder = BeanDefinitionBuilder.genericBeanDefinition(ScanningResourceSelector.class);
            builder.addConstructorArgValue(prepareBaseDirectoryList(element));
            builder.addConstructorArgValue(prepareResourceNameResolver(element, engine));
        }
        return builder.getBeanDefinition();
    }

    /**
     * @param element
     * @return
     */
    protected AbstractBeanDefinition prepareResourceNameResolver(final Element element, final Engine engine) {
        Element selectorElement = selectSingleChildElement(element, "selector", true);
        BeanDefinitionBuilder builder = null;
        String prefix = getName(element);
        String extension = engine.getDefaultExtension();
        if (selectorElement != null) {
            Element name = selectSingleChildElement(selectorElement, "name", true);
            if (name != null) {
                prefix = attribute(name, "prefix", prefix);
                extension = attribute(name, "extension", extension);

                Element version = selectSingleChildElement(name, "version", true);
                if (version != null) {
                    String pattern = version.getAttribute("pattern");
                    Element versionMavenElement = selectSingleChildElement(selectorElement, "maven", true);
                    builder = buildMavenVersionedResourceNameResolver(versionMavenElement, pattern, prefix, extension);
                }
            }
        }

        // Failsafe
        if (builder == null) {
            builder = buildBasicResourceNameResolver(prefix, extension);
        }
        return builder.getBeanDefinition();
    }

    /**
     * @param versionMavenElement
     * @param prefix
     * @param extension
     * @return
     */
    protected BeanDefinitionBuilder buildMavenVersionedResourceNameResolver(final Element versionMavenElement, final String pattern, final String prefix,
            final String extension) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(VersionedResourceNameResolver.class);
        builder.addConstructorArgValue(prefix);
        builder.addConstructorArgValue(prepareApplicationVersionFromMaven(versionMavenElement));
        builder.addPropertyValue("extension", extension);
        if (pattern != null) {
            builder.addPropertyValue("versionPattern", pattern);
        }
        return builder;
    }

    /**
     * @param versionMavenElement
     * @return
     */
    protected AbstractBeanDefinition prepareApplicationVersionFromMaven(final Element versionMavenElement) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ApplicationVersionFromMaven.class);
        builder.addConstructorArgValue(versionMavenElement.getAttribute("groupId"));
        builder.addConstructorArgValue(versionMavenElement.getAttribute("artifactId"));
        builder.addConstructorArgValue(Thread.currentThread().getContextClassLoader());
        return builder.getBeanDefinition();
    }

    /**
     * @param prefix
     * @param extension
     * @return
     */
    protected BeanDefinitionBuilder buildBasicResourceNameResolver(final String prefix, final Object extension) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(BasicResourceNameResolver.class);
        builder.addConstructorArgValue(prefix);
        builder.addPropertyValue("extension", extension);
        return builder;
    }

    /**
     * @param element
     * @return
     */
    protected ManagedList<Object> prepareBaseDirectoryList(final Element element) {
        ManagedList<Object> list = new ManagedList<Object>();
        Element locationElement = selectSingleChildElement(element, "location", true);
        if (locationElement != null) {
            List<Element> selectChildElements = selectChildElements(locationElement, "*");
            for (Element location : selectChildElements) {
                String tag = location.getLocalName();
                if ("environment-variable".equals(tag)) {
                    list.add(prepareLocation(location.getTextContent(), EnvironmentVariableDirectory.class));
                } else if ("system-property".equals(tag)) {
                    list.add(prepareLocation(location.getTextContent(), SystemPropertyDirectory.class));
                } else if ("home".equals(tag)) {
                    list.add(prepareHomeLocation(element, location.getAttribute("path")));
                } else if ("platform".equals(tag)) {
                    list.add(preparePlatformLocation(location.getTextContent()));
                } else if ("webapp".equals(tag)) {
                    list.add(prepareWebappLocation(element, location.getAttribute("path")));
                } else if ("resource".equals(tag)) {
                    list.add(prepareResourceLocation(element, location.getAttribute("location")));
                } else {
                    throw new IllegalArgumentException(String.format("Unknown location type '%s'", tag));
                }
            }
        } else {
            list.add(prepareHomeLocation(element, null));
            PlatformDirectory[] values = PlatformDirectory.values();
            for (PlatformDirectory platformDirectory : values) {
                list.add(platformDirectory);
            }
            // home, webapp (if available) and platforms
            if (ClassUtils.isPresent("org.springframework.web.context.WebApplicationContext", this.getClass().getClassLoader())) {
                list.add(prepareWebappLocation(element, null));
            }
        }
        return list;
    }

    /**
     * @param textContent
     * @return
     */
    protected PlatformDirectory preparePlatformLocation(final String type) {
        return PlatformDirectory.valueOf(type);
    }

    /**
     * @param attribute
     * @param class1
     * @return
     */
    protected AbstractBeanDefinition prepareHomeLocation(final Element element, final String path) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(HomeDirectory.class);
        String homePath = path;
        if (path == null) {
            homePath = ".config/" + getName(element);
        }
        builder.addConstructorArgValue(homePath);
        return builder.getBeanDefinition();
    }

    /**
     * @param element
     * @param path
     * @return
     */
    protected AbstractBeanDefinition prepareWebappLocation(final Element element, final String path) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(WebappDirectory.class);
        builder.addConstructorArgValue(path);
        return builder.getBeanDefinition();
    }

    /**
     * @param element
     * @param location
     * @return
     */
    protected AbstractBeanDefinition prepareResourceLocation(final Element element, final String location) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ResourceDirectory.class);
        builder.addConstructorArgValue(location);
        return builder.getBeanDefinition();
    }

    /**
     * @param value
     * @param baseDirectoryClass
     * @return
     */
    protected AbstractBeanDefinition prepareLocation(final String value, final Class<?> baseDirectoryClass) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(baseDirectoryClass);
        builder.addConstructorArgValue(value);
        return builder.getBeanDefinition();
    }

    /**
     * @param element
     * @return
     */
    protected void prepareSnapshotEventHandler(final Element element, final BeanDefinitionBuilder serviceBuilder) {
        Element handlers = selectSingleChildElement(element, "handlers", true);
        if (handlers != null) {
            String eventRef = handlers.getAttribute("event-ref");
            if (StringUtils.hasLength(eventRef)) {
                serviceBuilder.addConstructorArgReference(eventRef);
                return;
            }
        }
        BeanDefinitionBuilder eventBuilder = BeanDefinitionBuilder.genericBeanDefinition(LoggingSnapshotEventHandler.class);
        eventBuilder.addConstructorArgValue(getName(element));
        serviceBuilder.addConstructorArgValue(eventBuilder.getBeanDefinition());
    }

    /**
     * @param element
     * @return
     */
    protected Object prepareDeltaValueInterceptor(final Element element) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(SnapshotDeltaValueInterceptor.class);
        return builder.getBeanDefinition();
    }

    /**
     * @param element
     * @return
     */
    protected AbstractBeanDefinition prepareDefaultConfigurationSource(final Element element, final Engine engine) {
        Element defaultsElement = selectSingleChildElement(element, "defaults", true);
        String defaultsPath = null;
        String encoding = null;
        if (defaultsElement != null) {
            defaultsPath = defaultsElement.getAttribute("path");
            encoding = defaultsElement.getAttribute("encoding");
        }
        if (defaultsPath == null) {
            String guessPath = String.format("stillingar/%s.%s", getName(element), engine.getDefaultExtension());
            URL resource = Thread.currentThread().getContextClassLoader().getResource(guessPath);
            if (resource != null) {
                defaultsPath = guessPath;
            }
        }
        if (defaultsPath != null) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder
                    .genericBeanDefinition(DefaultConfigurationSourceFactoryBean.class);

            builder.addConstructorArgValue(prepareClassPathResource(defaultsPath));
            builder.addConstructorArgReference(getLoaderReference(element));
            builder.addConstructorArgValue(encoding == null ? null : Charset.forName(encoding));
            return builder.getBeanDefinition();
        }
        return null;
    }

    /**
     * @param defaultsPath
     * @return
     */
    protected AbstractBeanDefinition prepareClassPathResource(final String path) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ClassPathResource.class);
        builder.addConstructorArgValue(path);
        return builder.getBeanDefinition();
    }

    protected AbstractBeanDefinition prepareFileSystemResource(final String path) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(FileSystemResource.class);
        builder.addConstructorArgValue(path);
        return builder.getBeanDefinition();
    }

    protected AbstractBeanDefinition prepareXmlBeansConversionManager() {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ConversionManager.class);
        List<String> converterShortNames = Arrays.asList("BigDecimalConverter", "BigIntegerConverter",
                "BooleanConverter", "ByteConverter", "ByteArrayConverter", "UUIDConverter", "EnumConverter",
                "DoubleConverter", "ElementConverter", "FloatConverter", "IntegerConverter", "LongConverter",
                "ShortConverter", "StringConverter", "URIConverter", "DocumentConverter", "LocaleConverter"
        );
        ManagedList<AbstractBeanDefinition> converters = toManagedConverterList(converterShortNames, "org.brekka.stillingar.xmlbeans.conversion");
        converters.addAll(prepareTemporalConverters("org.brekka.stillingar.xmlbeans.conversion", "XmlBeansTemporalAdapter"));

        BeanDefinitionBuilder appCxtBeanDefBuilder = BeanDefinitionBuilder.genericBeanDefinition(ApplicationContextConverter.class);
        appCxtBeanDefBuilder.addConstructorArgValue(
                BeanDefinitionBuilder.genericBeanDefinition("org.brekka.stillingar.xmlbeans.conversion.DocumentConverter").getBeanDefinition()
        );
        converters.add(appCxtBeanDefBuilder.getBeanDefinition());
        builder.addConstructorArgValue(converters);
        return builder.getBeanDefinition();
    }

    protected AbstractBeanDefinition prepareDOMConversionManager() {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(ConversionManager.class);
        ManagedList<AbstractBeanDefinition> converters = prepareCoreConverters();
        converters.addAll(prepareTemporalConverters(TemporalAdapter.class.getPackage().getName(), TemporalAdapter.class.getSimpleName()));

        BeanDefinitionBuilder appCxtBeanDefBuilder = BeanDefinitionBuilder.genericBeanDefinition(ApplicationContextConverter.class);
        appCxtBeanDefBuilder.addConstructorArgValue(
                BeanDefinitionBuilder.genericBeanDefinition(DocumentConverter.class).getBeanDefinition()
        );
        converters.add(appCxtBeanDefBuilder.getBeanDefinition());
        builder.addConstructorArgValue(converters);
        return builder.getBeanDefinition();
    }

    protected AbstractBeanDefinition prepareJAXBConversionManager() {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(ConversionManager.class);
        ManagedList<AbstractBeanDefinition> converters = prepareCoreConverters();
        converters.addAll(prepareTemporalConverters("org.brekka.stillingar.jaxb.conversion", "JAXBTemporalAdapter"));

        BeanDefinitionBuilder appCxtBeanDefBuilder = BeanDefinitionBuilder.genericBeanDefinition(ApplicationContextConverter.class);
        appCxtBeanDefBuilder.addConstructorArgValue(
                BeanDefinitionBuilder.genericBeanDefinition(DocumentConverter.class).getBeanDefinition()
        );
        converters.add(appCxtBeanDefBuilder.getBeanDefinition());
        builder.addConstructorArgValue(converters);
        return builder.getBeanDefinition();
    }

    /**
     * @return
     */
    protected AbstractBeanDefinition prepareJsonConversionManager() {
        // TODO for now just reuse the DOM manager.
        return prepareDOMConversionManager();
    }

    protected Object prepareObjectMapper() {
        Object objectMapper;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", cl)) {
            try {
                objectMapper = ClassUtils.forName("org.brekka.stillingar.jackson.support.ObjectMapperFactory", cl)
                        .getMethod("getInstance").invoke(null);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to instantiate ObjectMapper for JSON support", e);
            }
        } else {
            throw new IllegalStateException("Unable to prepare ObjectMapper for JSON handling. "
                    + "Please ensure that the Jackson2 libraries are on the classpath.");
        }
        return objectMapper;
    }

    protected ManagedList<AbstractBeanDefinition> prepareCoreConverters() {
        List<String> coreConverterShortNames = Arrays.asList("BigDecimalConverter", "BigIntegerConverter",
                "BooleanConverter", "ByteConverter", "ByteArrayConverter",
                "DoubleConverter", "xml.ElementConverter", "FloatConverter", "IntegerConverter", "LongConverter",
                "ShortConverter", "StringConverter", "URIConverter", "xml.DocumentConverter", "LocaleConverter",
                "UUIDConverter", "EnumConverter");
        return toManagedConverterList(coreConverterShortNames, "org.brekka.stillingar.core.conversion");
    }

    /**
     * @param string
     * @return
     */
    protected Collection<? extends AbstractBeanDefinition> prepareTemporalConverters(final String customClassPackage,
            final String temporalAdapterClassShortName) {
        ManagedList<AbstractBeanDefinition> converters = new ManagedList<AbstractBeanDefinition>();
        BeanDefinitionBuilder temporalBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(
                customClassPackage + "." + temporalAdapterClassShortName);
        AbstractBeanDefinition temporalBeanDef = temporalBeanBuilder.getBeanDefinition();

        List<String> temporalConverterShortNames = Arrays.asList("DateConverter", "CalendarConverter");
        if (ClassUtils.isPresent("org.joda.time.ReadableInstant", Thread.currentThread().getContextClassLoader())) {
            // JodaTime is present, add the support classes
            temporalConverterShortNames = new ArrayList<String>(temporalConverterShortNames);
            temporalConverterShortNames.addAll(Arrays.asList("DateTimeConverter", "LocalDateConverter", "LocalTimeConverter"));

            if (customClassPackage != null) {
                BeanDefinitionBuilder converterBldr = BeanDefinitionBuilder
                        .genericBeanDefinition(customClassPackage + ".PeriodConverter");
                converters.add(converterBldr.getBeanDefinition());
            }
        }
        for (String shortName : temporalConverterShortNames) {
            BeanDefinitionBuilder converterBldr = BeanDefinitionBuilder
                    .genericBeanDefinition("org.brekka.stillingar.core.conversion." + shortName);
            converterBldr.addConstructorArgValue(temporalBeanDef);
            converters.add(converterBldr.getBeanDefinition());
        }
        return converters;
    }


    /**
     * @param element
     * @return
     */
    private static Engine determineEngine(final Element element) {
        String engine = element.getAttribute("engine");
        engine = engine.toUpperCase();
        return Engine.valueOf(engine);
    }

    /**
     * @param naming
     * @param string
     * @param prefix
     * @return
     */
    protected static String attribute(final Element elem, final String attributeName, final String defaultValue) {
        String value;
        if (elem.hasAttribute(attributeName)) {
            value = elem.getAttribute(attributeName);
        } else {
            value = defaultValue;
        }
        return value;
    }

    /**
     * @param element
     * @return
     */
    protected static String getLoaderReference(final Element element) {
        String id = element.getAttribute("id");
        return id + "-loader";
    }

    protected static String getName(final Element element) {
        // Optional application name, will use the id if not specified.
        String name = element.getAttribute("name");
        if (name == null || name.isEmpty()) {
            name = element.getAttribute("id");
        }
        return name;
    }

    /**
     * @param element
     * @param string
     * @return
     */
    protected static Element selectSingleChildElement(final Element element, final String tagName, final boolean optional) {
        Element singleChild = null;
        NodeList children = element.getElementsByTagNameNS("*", tagName);
        if (children.getLength() == 1) {
            Node node = children.item(0);
            if (node instanceof Element) {
                singleChild = (Element) node;
            } else {
                throw new IllegalArgumentException(String.format(
                        "Expected child node '%s' of element '%s' to be itself an instance of Element, "
                                + "it is instead '%s'", tagName, element.getTagName(), node.getClass().getName()));
            }
        } else if (children.getLength() == 0) {
            if (!optional) {
                throw new IllegalArgumentException(String.format(
                        "Failed to find a single child element named '%s' for parent element '%s'", tagName,
                        element.getTagName()));
            }
        } else {
            throw new IllegalArgumentException(String.format(
                    "Expected element '%s' to have a single child element named '%s', found however %d elements",
                    element.getTagName(), tagName, children.getLength()));
        }
        return singleChild;
    }

    protected static List<Element> selectChildElements(final Element element, final String tagName) {
        NodeList children = element.getElementsByTagNameNS("*", tagName);
        List<Element> elementList = new ArrayList<Element>(children.getLength());
        for (int i = 0; i < children.getLength(); i++) {
            Node item = children.item(i);
            if (item instanceof Element) {
                elementList.add((Element) item);
            } else {
                throw new IllegalArgumentException(String.format(
                        "The child node '%s' of element '%s' at index %d is not an instance of Element, "
                                + "it is instead '%s'", tagName, element.getTagName(), i, item.getClass().getName()));
            }

        }
        return elementList;
    }

    /**
     * @param converters
     * @param converterShortNames
     */
    private static ManagedList<AbstractBeanDefinition> toManagedConverterList(final List<String> converterShortNames, final String packagePrefix) {
        ManagedList<AbstractBeanDefinition> converters = new ManagedList<AbstractBeanDefinition>();
        for (String shortName : converterShortNames) {
            BeanDefinitionBuilder converterBldr = BeanDefinitionBuilder
                    .genericBeanDefinition(packagePrefix + "." + shortName);
            converters.add(converterBldr.getBeanDefinition());
        }
        return converters;
    }

    enum Engine {
        PROPS(PropertiesConfigurationSourceLoader.class.getName(), "properties"),

        DOM(DOMConfigurationSourceLoader.class.getName(), "xml"),

        XMLBEANS("org.brekka.stillingar.xmlbeans.XmlBeansConfigurationSourceLoader", "xml"),

        JAXB("org.brekka.stillingar.jaxb.JAXBConfigurationSourceLoader", "xml"),

        JSON("org.brekka.stillingar.jackson.JacksonConfigurationSourceLoader", "json"),

        ;

        private final String loaderClassName;
        private final String defaultExtension;

        private Engine(final String loaderClassName, final String defaultExtension) {
            this.loaderClassName = loaderClassName;
            this.defaultExtension = defaultExtension;
        }

        /**
         * @return the defaultExtension
         */
        public String getDefaultExtension() {
            return defaultExtension;
        }

        /**
         * @return the loaderClassName
         */
        public String getLoaderClassName() {
            return loaderClassName;
        }
    }
}
