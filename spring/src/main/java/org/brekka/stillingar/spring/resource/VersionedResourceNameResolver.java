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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.brekka.stillingar.spring.version.ApplicationVersionResolver;

/**
 * Enhancement to the {@link BasicResourceNameResolver} implementation that will include the version number in the name based
 * on that extracted from Apache Maven metadata.
 * 
 * @author Andrew Taylor
 */
public class VersionedResourceNameResolver extends BasicResourceNameResolver {
    /**
     * Default version pattern.
     */
    private static final Pattern DEFAULT_VERSION_PATTERN = Pattern.compile("^([\\d\\.]+).*$");
    
    /**
     * Default output name format that include version
     */
    private static final String DEFAULT_VERSION_NAME_FORMAT = "%s-%s.%s";

    /**
     * Where to obtain the version number.
     */
    protected final ApplicationVersionResolver applicationVersionResolver;

    /**
     * The version pattern to apply to the version number.
     */
    private Pattern versionPattern = DEFAULT_VERSION_PATTERN;
    
    /**
     * Output formatter to create the full configuration file name.
     */
    private String versionNameFormat = DEFAULT_VERSION_NAME_FORMAT;

    /**
     * @param prefix
     *            The prefix that will be applied to the resource name. This will normally be the application name.
     * @param applicationVersionResolver
     *            Identifies the version string.
     */
    public VersionedResourceNameResolver(String prefix, ApplicationVersionResolver applicationVersionResolver) {
        super(prefix);
        if (applicationVersionResolver == null) {
            throw new IllegalArgumentException("Application version resolver must be set");
        }
        this.applicationVersionResolver = applicationVersionResolver;
    }
    
    
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.spring.resource.ResourceNameResolver#getNames()
     */
    @Override
    public Set<String> getNames() {
        Set<String> names = new LinkedHashSet<String>();
        names.addAll(prepareVersionedNames());
        names.add(prepareBasicName());
        return names;
    }
    
    /**
     * Generate a list of candidate version names.
     * @return the list of names
     */
    protected List<String> prepareVersionedNames() {
        List<String> names = Collections.emptyList();
        String version = applicationVersionResolver.identifyVersion();
        if (version != null) {
            Matcher matcher = versionPattern.matcher(version);
            if (matcher.matches()) {
                names = new ArrayList<String>();
                int groupCount = matcher.groupCount();
                if (groupCount == 0) {
                    // No groups, just use the whole string
                    version = matcher.group(0);
                    names.add(formatWithVersion(version));
                } else {
                    for (int i = 1; i <= groupCount; i++) {
                        String groupVersion = matcher.group(i);
                        names.add(formatWithVersion(groupVersion));
                    }
                }
            }
        }
        return names;
    }

    /**
     * @param version
     * @return
     */
    protected String formatWithVersion(String version) {
        return String.format(getVersionNameFormat(), getPrefix(), version, getExtension());
    }

    /**
     * @param nameFormat the nameFormat to set
     */
    public final void setVersionNameFormat(String versionNameFormat) {
        if (versionNameFormat == null) {
            throw new IllegalArgumentException("Output version name format cannot be null");
        }
        this.versionNameFormat = versionNameFormat;
    }
    
    /**
     * @param versionPattern the versionPattern to set
     */
    public final void setVersionPattern(Pattern versionPattern) {
        if (versionPattern == null) {
            throw new IllegalArgumentException("Pattern cannot be null");
        }
        this.versionPattern = versionPattern;
    }
    
    /**
     * @return the nameFormat
     */
    public final String getVersionNameFormat() {
        return versionNameFormat;
    }
    
    /**
     * @return the versionPattern
     */
    public final Pattern getVersionPattern() {
        return versionPattern;
    }

}
