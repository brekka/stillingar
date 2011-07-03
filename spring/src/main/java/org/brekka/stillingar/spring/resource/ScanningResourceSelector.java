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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brekka.stillingar.core.ConfigurationException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * Selector that identifies a configuration base directory based on a list of resources of which the first valid
 * location will be chosen. A valid location is deemed to be a directory which contains a resource whose name matches
 * the 'original' name returned by the {@link ResourceNaming} instance.
 * 
 * It is important to note that scanning is performed only on initialisation. Once that location has been determined, it
 * is locked in until the application is restarted.
 * 
 * Should no valid location be found, and exception will be thrown.
 * 
 * @author Andrew Taylor
 */
public class ScanningResourceSelector implements ResourceSelector {

    /**
     * Logger
     */
    private static final Log log = LogFactory.getLog(ScanningResourceSelector.class);
    
    /**
     * The 'original' resource
     */
    private final Resource original;

    /**
     * 'Last good' resource
     */
    private final Resource lastGood;

    /**
     * Scan the list of locations for the original name specified by {@link ResourceNaming}.
     * 
     * @param locations the list of resources that should identify directories
     * @param resourceNaming the strategy to use for naming configuration resources.
     */
    public ScanningResourceSelector(List<Resource> locations, ResourceNaming resourceNaming) {
        Resource original = null;
        Resource lastGood = null;

        String originalName = resourceNaming.prepareOriginalName();
        String lastGoodName = resourceNaming.prepareLastGoodName();

        for (Resource locationBase : locations) {
            try {
                if (locationBase != null && locationBase.exists()) {
                    Resource locationOriginal = locationBase.createRelative(originalName);
                    if (locationOriginal.exists() && locationOriginal.isReadable()) {
                        original = locationOriginal;
                        try {
                            File base = new File(locationBase.getURI());
                            File lastGoodFile = new File(base, lastGoodName);
                            lastGood = new FileSystemResource(lastGoodFile);
                        } catch (IllegalArgumentException e) {
                            // Not a file, just ignore.
                        }
                        // We have found what we are looking for
                        break;
                    }
                }
            } catch (IOException e) {
                // Log as warning
                if (log.isWarnEnabled()) {
                    log.warn(format("Resource location '%s' encountered problem", locationBase), e);
                }
            }
        }
        if (original == null) {
            throw new ConfigurationException(format(
                    "Could not find configuration resource '%s' in any of the following: %s", originalName, locations));
        }
        this.original = original;
        this.lastGood = lastGood;
    }

    public final Resource getOriginal() {
        return original;
    }

    public final Resource getLastGood() {
        return lastGood;
    }
}
