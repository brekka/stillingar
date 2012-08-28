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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Simple resource naming strategy that generates 'original' and 'last good' files based on a prefix and extension.
 * 
 * @author Andrew Taylor
 */
public class BasicResourceNameResolver implements ResourceNameResolver {

    /**
     * The default extension
     */
    private static final String DEFAULT_EXTENSION = "xml";

    /**
     * The prefix that will be applied to the resource name. This will normally be the application name.
     */
    private final String prefix;

    /**
     * The extension to use which by default is 'xml'.
     */
    private String extension = DEFAULT_EXTENSION;

    /**
     * @param prefix
     *            The prefix that will be applied to the resource name. This will normally be the application name.
     */
    public BasicResourceNameResolver(String prefix) {
        this.prefix = prefix;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.spring.resource.ResourceNameResolver#getNames()
     */
    @Override
    public Set<String> getNames() {
        return new LinkedHashSet<String>(Arrays.asList(prepareBasicName()));
    }
    
    protected String prepareBasicName() {
        return format("%s.%s", prefix, extension);
    }
    
    /**
     * @return the prefix
     */
    public final String getPrefix() {
        return prefix;
    }
    
    /**
     * @return the extension
     */
    public final String getExtension() {
        return extension;
    }
    
    /**
     * @param extension the extension to set
     */
    public final void setExtension(String extension) {
        this.extension = extension;
    }
}
