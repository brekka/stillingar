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
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brekka.stillingar.core.ChangeConfigurationException;
import org.brekka.stillingar.core.GroupConfigurationException;
import org.brekka.stillingar.core.snapshot.InvalidSnapshotException;
import org.brekka.stillingar.core.snapshot.NoSnapshotAvailableException;
import org.brekka.stillingar.core.snapshot.RejectedSnapshotLocation;
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
        out.printf("%nApplication '%s' - No initial configuration snapshot could be found.%n", applicationName);
        out.println("   Looked for files with the names:");
        Set<String> snapshotResourceNames = e.getSnapshotResourceNames();
        for (String name : snapshotResourceNames) {
            out.printf( "     - %s%n", name);
        }
        List<RejectedSnapshotLocation> locations = e.getLocations();
        out.printf( "   in the following locations:%n", e.getSnapshotResourceNames());
        for (RejectedSnapshotLocation r : locations) {
            String path = "";
            if (r.getPath() != null) {
                path = " (" + r.getPath() + ")";
            }
            out.printf( "     - %s - %s%s%n", r.getDisposition(), r.getMessage(), path);
        }
        if (defaultsAvailable) {
            out.println("Application will now be configured using defaults from classpath.");
        } else {
            out.println("There are no defaults available, so this application will fail to start.");
        }
        
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
        if (e == null) {
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
        // TODO more detail
        log.error("Failed to update configuration from snapshot", e);
    }
}
