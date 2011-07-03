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

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Enhancement to the {@link BasicResourceNaming} implementation that will include the version number in the name based
 * on that extracted from Apache Maven metadata.
 * 
 * @author Andrew Taylor
 */
public class MavenVersionedResourceNaming extends BasicResourceNaming implements InitializingBean {
    /**
     * The key used to resolve the version within a Maven 'pom.properties' file.
     */
    private static final String POM_PROPS_VERSION = "version";

    /**
     * Location of the 'pom.properties' which can be found on the classpath by specifying the group and artifactId ids
     * to this string.
     */
    private static final String POM_CLASSPATH_FORMAT = "META-INF/maven/%s/%s/pom.properties";

    /**
     * Default version pattern.
     */
    private static final Pattern DEFAULT_VERSION_PATTERN = Pattern.compile("^(\\d+).*$");
    
    /**
     * Logger
     */
    private static final Log log = LogFactory.getLog(MavenVersionedResourceNaming.class);

    /**
     * The project group ID
     */
    private String groupId;

    /**
     * Project artifact ID.
     */
    private String artifactId;

    /**
     * Pattern to use to extract the portion of the version number to include in the resource name.
     */
    private Pattern versionPattern = DEFAULT_VERSION_PATTERN;

    /**
     * The version that will be extracted by the call to {@link #afterPropertiesSet()}.
     */
    private String version;

    /**
     * Class loader to use to resolve the version number. By default uses the same class loader that loaded this class.
     */
    private ClassLoader resolveClassloader = this.getClass().getClassLoader();

    /**
     * @param prefix
     *            The prefix that will be applied to the resource name. This will normally be the application name.
     */
    public MavenVersionedResourceNaming(String prefix) {
        super(prefix);
    }

    /**
     * Extract the version number from Maven. If the project version number cannot be found, then it will be silently
     * ignored (apart from the warn log message).
     */
    public void afterPropertiesSet() {
        String version = null;
        String path = format(POM_CLASSPATH_FORMAT, groupId, artifactId);
        InputStream is = resolveClassloader.getResourceAsStream(path);
        if (is != null) {
            Properties props = new Properties();
            try {
                props.load(is);
                String projectVersion = props.getProperty(POM_PROPS_VERSION);
                Matcher matcher = versionPattern.matcher(projectVersion);
                if (matcher.matches()) {
                    version = matcher.group(1);
                } else {
                    if (log.isWarnEnabled()) {
                        log.warn(format("Version string '%s' for group '%s', artifact '%s' does not match the pattern '%s'." +
                                "No version will be included in the resource file names.", 
                                projectVersion, groupId, artifactId, versionPattern.pattern()));
                    }
                }
            } catch (IOException e) {
                if (log.isWarnEnabled()) {
                    log.warn(format("Failed to load 'pom.properties' from classpath for group '%s', artifact '%s'. " +
                            "No version will be included in the resource file names.", groupId, artifactId));
                }
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn(format("Unable to locate 'pom.properties' for group '%s', artifact '%s'. " +
                		"No version will be included in the resource file names.", groupId, artifactId));
            }
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
        return format("%s-%s-%s.%s", getPrefix(), version, getLastGoodMarker(), getSuffix());
    }

    /**
     * Set the project artifact id
     * @param artifactId
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * Set the project group id
     * @param groupId
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Override the default version pattern.
     * @param versionPattern
     */
    public void setVersionPattern(Pattern versionPattern) {
        this.versionPattern = versionPattern;
    }

    /**
     * Set the classloader in which to resolve the POM project version details.
     * @param resolveClassloader
     */
    public void setResolveClassloader(ClassLoader resolveClassloader) {
        this.resolveClassloader = resolveClassloader;
    }

    /**
     * Use the classloader of the specified class to resolve the POM details.
     * @param resolveClass
     */
    public void setResolveClass(Class<?> resolveClass) {
        if (resolveClass != null) {
            setResolveClassloader(resolveClass.getClassLoader());
        }
    }
}
