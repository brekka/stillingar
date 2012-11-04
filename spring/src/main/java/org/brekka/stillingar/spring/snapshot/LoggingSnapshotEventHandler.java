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

package org.brekka.stillingar.spring.snapshot;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brekka.stillingar.core.ChangeConfigurationException;
import org.brekka.stillingar.core.GroupConfigurationException;
import org.brekka.stillingar.core.snapshot.ConsoleSnapshotEventHandler;
import org.brekka.stillingar.core.snapshot.InvalidSnapshotException;
import org.brekka.stillingar.core.snapshot.NoSnapshotAvailableException;
import org.brekka.stillingar.core.snapshot.Snapshot;
import org.brekka.stillingar.core.snapshot.SnapshotEventHandler;

/**
 * Enhances the standard {@link SnapshotEventHandler} to use commons logging as the output mechanism (giving 
 * control of output to a standard logging framework).
 * 
 * @author Andrew Taylor
 */
public class LoggingSnapshotEventHandler implements SnapshotEventHandler {

    /**
     * The logger to use to report errors
     */
    private static final Log log = LogFactory.getLog(LoggingSnapshotEventHandler.class);
    
    private final String applicationName;
    
    /**
     * @param applicationName
     */
    public LoggingSnapshotEventHandler(String applicationName) {
        this.applicationName = applicationName;
    }



    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.snapshot.SnapshotEventHandler#noInitialSnapshot(org.brekka.stillingar.core.snapshot.NoSnapshotAvailableException, boolean, boolean)
     */
    @Override
    public void noInitialSnapshot(NoSnapshotAvailableException e, boolean defaultsAvailable) {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        ConsoleSnapshotEventHandler.writeNoInitialSnapshotSummary(out, defaultsAvailable, applicationName, e);
        
        if (defaultsAvailable) {
            log.warn(sw.toString());
        } else {
            log.error(sw.toString());
        }
    }



    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.snapshot.SnapshotEventHandler#initialConfigure(org.brekka.stillingar.core.snapshot.Snapshot, java.util.List)
     */
    @Override
    public void initialConfigure(Snapshot snapshot, ChangeConfigurationException e) {
        if (snapshot == null) {
            if (log.isInfoEnabled()) {
                log.info("Loaded from defaults");
            }
        } else if (e == null) {
            if (log.isInfoEnabled()) {
                log.info(String.format("Snapshot loaded successfully from '%s'", snapshot.getLocation()));
            }
        } else {
            log.error(String.format("Problem loading snapshot '%s'", 
                    snapshot.getLocation()), e);
            int cnt = 1;
            for (GroupConfigurationException groupConfigurationException : e.getGroupErrors()) {
                log.error(String.format("Group error %d", cnt++), groupConfigurationException);
            }
        }
    }



    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.snapshot.SnapshotEventHandler#refreshConfigure(org.brekka.stillingar.core.snapshot.Snapshot, java.util.List)
     */
    @Override
    public void refreshConfigure(Snapshot snapshot, ChangeConfigurationException e) {
        if (e == null) {
            if (log.isInfoEnabled()) {
                log.info(String.format("Snapshot refreshed successfully from '%s'", snapshot.getLocation()));
            }
        } else {
            log.error(String.format("Problem refreshing from snapshot '%s'", 
                    snapshot.getLocation()), e);
            int cnt = 1;
            for (GroupConfigurationException groupConfigurationException : e.getGroupErrors()) {
                log.error(String.format("Group error %d", cnt++), groupConfigurationException);
            }
        }
    }



    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.snapshot.SnapshotEventHandler#invalidSnapshotUpdate(org.brekka.stillingar.core.snapshot.InvalidSnapshotException)
     */
    @Override
    public void invalidSnapshotUpdate(InvalidSnapshotException e) {
        log.error(String.format("Failed to update configuration for '%s' from snapshot", applicationName), e);
    }
}
