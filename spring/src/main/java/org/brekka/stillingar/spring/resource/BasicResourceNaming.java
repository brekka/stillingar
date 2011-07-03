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

/**
 * Simple resource naming strategy that generates 'original' and 'last good' files based on a prefix and suffix.
 * 
 * @author Andrew Taylor
 */
public class BasicResourceNaming implements ResourceNaming {
    /**
     * The default string to use for the lastgood fragment
     */
    private static final String DEFAULT_LASTGOOD = "lastgood";

    /**
     * The default suffix
     */
    private static final String DEFAULT_SUFFIX = "xml";

    /**
     * The prefix that will be applied to the resource name. This will normally be the application name.
     */
    private final String prefix;

    /**
     * The suffix to use which by default is 'xml'.
     */
    private String suffix = DEFAULT_SUFFIX;

    /**
     * Marker string to include which identifies it as a last good file.
     */
    private String lastGoodMarker = DEFAULT_LASTGOOD;

    /**
     * @param prefix
     *            The prefix that will be applied to the resource name. This will normally be the application name.
     */
    public BasicResourceNaming(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Generates the 'original' name which will simply be the prefix and suffix combined with a period separating them.
     */
    public String prepareOriginalName() {
        return format("%s.%s", prefix, suffix);
    }

    /**
     * Generates the 'last good' name which will take the format "[prefix]-[lastgood].[suffix]".
     */
    public String prepareLastGoodName() {
        return format("%s-%s.%s", prefix, lastGoodMarker, suffix);
    }

    /**
     * Retrieve the last good marker
     * 
     * @return
     */
    protected String getLastGoodMarker() {
        return lastGoodMarker;
    }

    /**
     * The suffix to use which by default is 'xml'.
     * 
     * @return
     */
    protected String getSuffix() {
        return suffix;
    }

    /**
     * The prefix that will be applied to the resource name. This will normally be the application name.
     * 
     * @return
     */
    protected String getPrefix() {
        return prefix;
    }

    /**
     * Override the 'last good' file marker
     * 
     * @param lastGoodMarker
     */
    public void setLastGoodMarker(String lastGoodMarker) {
        this.lastGoodMarker = lastGoodMarker;
    }

    /**
     * Override the default suffix
     * 
     * @param suffix
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
