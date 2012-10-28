/*
 * Copyright 2012 the original author or authors.
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

package org.brekka.stillingar.spring.snapshot;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.brekka.stillingar.core.snapshot.SnapshotBasedConfigurationService;

/**
 * Simple adapter class that invokes the {@link SnapshotBasedConfigurationService#refresh()} method via
 * the {@link #run()}, allowing it to be called by scheduling frameworks.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class ConfigurationSnapshotRefresher implements Runnable {
    
    /**
     * Logger
     */
    private static final Log log = LogFactory.getLog(ConfigurationSnapshotRefresher.class);

    /**
     * The configuration source to refresh
     */
    private final SnapshotBasedConfigurationService snapshotBasedConfigurationSource;
    
    
    /**
     * @param snapshotBasedConfigurationSource
     */
    public ConfigurationSnapshotRefresher(SnapshotBasedConfigurationService snapshotBasedConfigurationSource) {
        this.snapshotBasedConfigurationSource = snapshotBasedConfigurationSource;
    }


    /**
     * Invokes {@link SnapshotBasedConfigurationService#refresh()}. Error handling should be taken care of by the
     * configuration source, but log out just in case.
     */
    @Override
    public void run() {
        try {
            snapshotBasedConfigurationSource.refresh();
        } catch (RuntimeException e) {
            // Log just in case, can always disable WARN on this class.
            if (log.isWarnEnabled()) {
                log.warn("Snapshot configuration refresh problem", e);
            }
            throw e;
        }
    }
}
