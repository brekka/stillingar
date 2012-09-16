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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brekka.stillingar.core.snapshot.NoSnapshotAvailableException;
import org.brekka.stillingar.core.snapshot.RejectedSnapshotLocation;
import org.springframework.core.io.Resource;

/**
 * A resource selector that will iterate a list of known directories looking for files with names determined by
 * {@link ResourceNameResolver}. The order of directories is important with the highest priority appearing first.
 * 
 * @author Andrew Taylor
 */
public class ScanningResourceSelector implements ResourceSelector {

    /**
     * Logger
     */
    private static final Log log = LogFactory.getLog(ScanningResourceSelector.class);

    /**
     * List of directories that are to be searched for the configuration resource.
     */
    private final List<BaseDirectory> baseDirectories;

    /**
     * Will be used to generate file names to combine with the base directories in order to find resources.
     */
    private final ResourceNameResolver resourceNameResolver;

    /**
     * 
     * @param baseDirectories
     * @param resourceNameResolver
     */
    public ScanningResourceSelector(List<BaseDirectory> baseDirectories, ResourceNameResolver resourceNameResolver) {
        this.baseDirectories = baseDirectories;
        this.resourceNameResolver = resourceNameResolver;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.spring.resource.ResourceSelector#getResource()
     */
    @Override
    public Resource getResource() throws NoSnapshotAvailableException {
        Set<String> names = resourceNameResolver.getNames();
        List<RejectedSnapshotLocation> rejected = new ArrayList<RejectedSnapshotLocation>();
        for (BaseDirectory locationBase : baseDirectories) {
            if (locationBase == null) {
                continue;
            }
            Resource resource = findInBaseDir(locationBase, names, rejected);
            if (resource != null) {
                return resource;
            }
        }
        throw new NoSnapshotAvailableException(names, rejected);
    }

    /**
     * Search the specified base directory for files with names matching those in <code>names</code>. If the location
     * gets rejected then it should be added to the list of rejected locations.
     * 
     * @param locationBase
     *            the location to search
     * @param names
     *            the names of files to find within the base location
     * @param rejected
     *            collects failed locations.
     * @return the resource or null if one cannot be found.
     */
    protected Resource findInBaseDir(BaseDirectory locationBase, Set<String> names,
            List<RejectedSnapshotLocation> rejected) {
        Resource dir = locationBase.getDirResource();
        if (dir instanceof UnresolvableResource) {
            UnresolvableResource res = (UnresolvableResource) dir;
            rejected.add(new Rejected(locationBase.getDisposition(), null, res.getMessage()));
        } else {
            String dirPath = null;
            try {
                URI uri = dir.getURI();
                if (uri != null) {
                    dirPath = uri.toString();
                }
            } catch (IOException e) {
                if (log.isWarnEnabled()) {
                    log.warn(format("Resource dir '%s' has a bad uri", locationBase), e);
                }
            }
            String message;
            if (dir.exists()) {
                StringBuilder messageBuilder = new StringBuilder();
                for (String name : names) {
                    try {
                        Resource location = dir.createRelative(name);
                        if (location.exists()) {
                            if (location.isReadable()) {
                                // We have found a file
                                return location;
                            }
                            if (messageBuilder.length() > 0) {
                                messageBuilder.append(" ");
                            }
                            messageBuilder.append("File '%s' exists but cannot be read.");
                        } else {
                            // Fair enough, it does not exist
                        }
                    } catch (IOException e) {
                        // Location could not be resolved, log as warning, then move on to the next one.
                        if (log.isWarnEnabled()) {
                            log.warn(format("Resource location '%s' encountered problem", locationBase), e);
                        }
                    }
                }
                if (messageBuilder.length() == 0) {
                    message = "no configuration files found";
                } else {
                    message = messageBuilder.toString();
                }
            } else {
                message = "Directory does not exist";
            }
            rejected.add(new Rejected(locationBase.getDisposition(), dirPath, message));
        }
        // No resource found
        return null;
    }

    /**
     * Internal structure for holding rejected snapshot details.
     */
    private static class Rejected implements RejectedSnapshotLocation {

        private final String disposition;
        private final String path;
        private final String message;

        /**
         * @param disposition
         * @param path
         * @param message
         */
        public Rejected(String disposition, String path, String message) {
            this.disposition = disposition;
            this.path = path;
            this.message = message;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.brekka.stillingar.core.snapshot.RejectedSnapshotLocation#getDisposition()
         */
        @Override
        public String getDisposition() {
            return disposition;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.brekka.stillingar.core.snapshot.RejectedSnapshotLocation#getPath()
         */
        @Override
        public String getPath() {
            return path;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.brekka.stillingar.core.snapshot.RejectedSnapshotLocation#getMessage()
         */
        @Override
        public String getMessage() {
            return message;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "[" + getDisposition() + " - " + getPath() + " - " + getMessage() + "]";
        }
    }
}
