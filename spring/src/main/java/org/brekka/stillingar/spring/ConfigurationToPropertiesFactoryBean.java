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

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.brekka.stillingar.core.ConfigurationSource;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * TODO
 * 
 * @author Andrew Taylor
 */
public class ConfigurationToPropertiesFactoryBean implements FactoryBean<Properties>, InitializingBean {

	private Map<String, String> keyToExpressionMap = Collections.emptyMap();
	
	private ConfigurationSource configurationSource;
	

	public void afterPropertiesSet() throws Exception {
		if (this.configurationSource == null) {
			throw new IllegalArgumentException("'configurationSource' is required");
		}
	}
	
	public Properties getObject() throws Exception {
		Properties properties = new Properties();
		Set<Entry<String,String>> entrySet = keyToExpressionMap.entrySet();
		for (Entry<String, String> entry : entrySet) {
			String key = entry.getKey();
			String expression = entry.getValue();
			
			String value = configurationSource.retrieve(String.class, expression);
			properties.put(key, value);
		}
		return properties;
	}

	public Class<Properties> getObjectType() {
		return Properties.class;
	}

	public boolean isSingleton() {
		return true;
	}

	
	public void setConfigurationSource(ConfigurationSource configurationSource) {
		this.configurationSource = configurationSource;
	}
	
	public void setKeyToExpressionMap(Map<String, String> keyToExpressionMap) {
		this.keyToExpressionMap = keyToExpressionMap;
	}
}
