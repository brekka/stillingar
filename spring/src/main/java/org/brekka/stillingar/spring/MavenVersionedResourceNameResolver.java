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

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.InitializingBean;

public class MavenVersionedResourceNameResolver extends
		BasicResourceNameResolver implements InitializingBean {

	private static final Pattern DEFAULT_VERSION_PATTERN = Pattern
			.compile("^(\\d+).*$");

	private String groupId;
	private String artifactId;
	private Pattern versionPattern = DEFAULT_VERSION_PATTERN;
	
	private String version;
	
	private ClassLoader resolveClassloader = this.getClass().getClassLoader();

	public MavenVersionedResourceNameResolver(String prefix) {
		super(prefix);
	}
	
	public void afterPropertiesSet() {
		String version = null;
		String path = format("META-INF/maven/%s/%s/pom.properties", groupId, artifactId);
		InputStream is = resolveClassloader.getResourceAsStream(path);
		if (is != null) {
			Properties props = new Properties();
			try {
				props.load(is);
				String projectVersion = props.getProperty("version");
				Matcher matcher = versionPattern.matcher(projectVersion);
				if (matcher.matches()) {
					version = matcher.group(1);
				}
			} catch (IOException e) { 
				// TODO log warning
			}
		} else {
			// log warning
		}
		this.version = version;
	}


	@Override
	public String prepareOriginalName() {
		if (version == null) {
			return super.prepareOriginalName();
		}
		return format("%s-%s.%s", getPrefix(), version, getSuffix());
	}

	@Override
	public String prepareLastGoodName() {
		if (version == null) {
			return super.prepareLastGoodName();
		}
		return format("%s-%s-%s.%s", getPrefix(), version, getLastGoodMarker(),
				getSuffix());
	}
	
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}
	
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	public void setVersionPattern(Pattern versionPattern) {
		this.versionPattern = versionPattern;
	}
	
	public void setResolveClassloader(ClassLoader resolveClassloader) {
		this.resolveClassloader = resolveClassloader;
	}
	
	public void setResolveClass(Class<?> resolveClass) {
		if (resolveClass != null) {
			setResolveClassloader(resolveClass.getClassLoader());
		}
	}
}
