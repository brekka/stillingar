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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brekka.stillingar.core.ChangeAwareConfigurationSource;
import org.brekka.stillingar.core.GroupConfigurationException;
import org.brekka.stillingar.core.snapshot.InvalidSnapshotException;
import org.brekka.stillingar.core.snapshot.NoSnapshotAvailableException;
import org.brekka.stillingar.core.snapshot.Snapshot;
import org.brekka.stillingar.core.snapshot.SnapshotEventHandler;

/**
 * A simple runnable that can be used perform updates on an {@link ChangeAwareConfigurationSource}, logging out any
 * resulting report and its errors.
 * 
 * @author Andrew Taylor
 */
public class LoggingSnapshotEventHandler implements SnapshotEventHandler {

    /**
     * The logger to use to report errors
     */
    private static final Log log = LogFactory.getLog(LoggingSnapshotEventHandler.class);

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.snapshot.SnapshotEventHandler#noInitialSnapshot(org.brekka.stillingar.core.snapshot.NoSnapshotAvailableException, boolean, boolean)
     */
    @Override
    public void noInitialSnapshot(NoSnapshotAvailableException e, boolean defaultsAvailable,
            boolean initialSnapshotRequired) {
        // TODO more detail
        log.error("No initial snapshot could be found", e);
    }



    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.snapshot.SnapshotEventHandler#initialConfigure(org.brekka.stillingar.core.snapshot.Snapshot, java.util.List)
     */
    @Override
    public void initialConfigure(Snapshot snapshot, List<GroupConfigurationException> errors) {
        if (errors.isEmpty()) {
            if (log.isInfoEnabled()) {
                log.info(String.format("Snapshot loaded successfully from '%s'", snapshot.getLocation()));
            }
        } else {
            log.error(String.format("Encountered %d errors while loading snapshot '%s'", 
                    errors.size(), snapshot.getLocation()));
            int cnt = 1;
            for (GroupConfigurationException groupConfigurationException : errors) {
                log.error(String.format("Group error %d", cnt), groupConfigurationException);
            }
        }
    }



    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.snapshot.SnapshotEventHandler#refreshConfigure(org.brekka.stillingar.core.snapshot.Snapshot, java.util.List)
     */
    @Override
    public void refreshConfigure(Snapshot snapshot, List<GroupConfigurationException> errors) {
        if (errors.isEmpty()) {
            if (log.isInfoEnabled()) {
                log.info(String.format("Snapshot refreshed successfully from '%s'", snapshot.getLocation()));
            }
        } else {
            log.error(String.format("Encountered %d errors while refreshing snapshot '%s'", 
                    errors.size(), snapshot.getLocation()));
            int cnt = 1;
            for (GroupConfigurationException groupConfigurationException : errors) {
                log.error(String.format("Group error %d", cnt), groupConfigurationException);
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
