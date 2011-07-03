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

package org.brekka.stillingar.spring.resource;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;

/**
 * The locations specified will be checked sequentially until the first valid location is encountered. The locations can
 * include system property and/or environment variables which will be resolved prior to checking the location is valid.
 * 
 * @author Andrew Taylor
 */
public class BaseDirResolver implements FactoryBean<Resource>, ApplicationContextAware {

    /**
     * Regex used to detect and replace system/environment properties.
     */
    private static final Pattern VAR_REPLACE_REGEX = Pattern.compile("\\$\\{(env\\.)?([\\w\\._\\-]+)\\}");

    /**
     * The list of locations
     */
    private final Collection<String> locations;

    /**
     * Environment var map
     */
    private Map<String, String> envMap = System.getenv();

    /**
     * Optional application to use to resolve resources. If not present, then all locations will be assumed to be
     * {@link FileSystemResource}.
     */
    private ApplicationContext applicationContext;

    public BaseDirResolver(Collection<String> locations) {
        this.locations = locations;
    }

    @Override
    public Class<Resource> getObjectType() {
        return Resource.class;
    }

    @Override
    public Resource getObject() throws Exception {
        Resource resource = null;
        for (String location : locations) {
            resource = resolve(location);
            if (resource != null) {
                break;
            }
        }
        return resource;
    }

    /**
     * Perform variable extraction on the location value and return the corresponding resource. If placeholder values
     * are detected which cannot be resolved, then null will be returned (ie it is not resolvable).
     * 
     * @param location the location to extrapolate placeholders for.
     * @return the resource or null if all placeholders cannot be resolved.
     */
    Resource resolve(String location) {
        Resource resource = null;
        Matcher matcher = VAR_REPLACE_REGEX.matcher(location);
        boolean allValuesReplaced = true;
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            boolean env = matcher.group(1) != null;
            String key = matcher.group(2);
            String value;
            if (env) {
                // Resolve an environment variable
                value = envMap.get(key);
            } else {
                // System property
                value = System.getProperty(key);
            }
            allValuesReplaced &= (value != null);
            if (!allValuesReplaced) {
                break;
            }
            matcher.appendReplacement(sb, value);
            matcher.appendTail(sb);
        }
        if (allValuesReplaced) {
            if (applicationContext != null) {
                resource = applicationContext.getResource(sb.toString());
            } else {
                try {
                    resource = new UrlResource(sb.toString());
                } catch (MalformedURLException e) {
                    // Ignore
                }
            }
            if (!resource.exists()) {
                resource = null;
            }
        }
        return resource;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * Override the environment variables map. Intended for testing purposes but left public in case it is useful for somebody.
     * @param envMap
     */
    public void setEnvMap(Map<String, String> envMap) {
        this.envMap = envMap;
    }

    /**
     * If set, the resolver will use the {@link ResourceLoader#getResource(String)} method of the applicationContext to resolve
     * the placeholder-replaced locations.
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
