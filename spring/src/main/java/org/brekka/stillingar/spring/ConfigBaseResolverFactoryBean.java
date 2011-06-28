package org.brekka.stillingar.spring;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/**
 * Attempt to locate the configuration base directories of popular Java web/application servers. The locations specified will
 * be checked sequentially until the first valid location is encountered. The locations can include system property and/or
 * environment variables which will be resolved prior to checking the location is valid.
 * 
 * @author Andrew Taylor
 */
public class ConfigBaseResolverFactoryBean implements FactoryBean<Resource>, ApplicationContextAware {
	
	/**
	 * Regex used to detect and replace system/environment properties.
	 */
	private static final Pattern VAR_REPLACE_REGEX = Pattern.compile("\\$\\{(env\\.)?([\\w\\._\\-]+)\\}");

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
	
	/**
	 * The list of locations
	 */
	private Set<String> locations;
	
	/**
	 * Enviroment var map
	 */
	private Map<String, String> envMap = System.getenv();
	
	/**
	 * Optional application to use to resolve resources. If not present, then all locations
	 * will be assumed to be {@link FileSystemResource}.
	 */
	private ApplicationContext applicationContext;
	
	public ConfigBaseResolverFactoryBean() {
		this(new HashSet<String>(DEFAULT_LOCATION_LIST));
	}
	
	public ConfigBaseResolverFactoryBean(Set<String> locations) {
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
	
	public void setEnvMap(Map<String, String> envMap) {
		this.envMap = envMap;
	}
	
	public void setLocations(Set<String> locations) {
		this.locations = locations;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
}
