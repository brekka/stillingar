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

package org.brekka.stillingar.core.snapshot;

import org.brekka.stillingar.core.ChangeConfigurationException;

/**
 * Provide a failsafe mechanism for conveying critical configuration events back to the developer/administrator via the
 * standard output.
 * 
 * Output from this class can be suppressed by setting the "stillingar.quiet" system property to "true" either in the
 * main method of the utility using this, or as a system property argument to the process itself (ie
 * "-Dstillingar.quiet=true").
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class ConsoleSnapshotEventHandler implements SnapshotEventHandler {

    /**
     * The system property that if set to 'true' will disable console logging of configuration loading.
     */
    private static final String SYSPROP_STILLINGAR_QUIET = "stillingar.quiet";

    /**
     * Determines whether events will be actually written to the console
     */
    private final boolean enabled;

    /**
     * 
     */
    public ConsoleSnapshotEventHandler() {
        this(!isDisabledBySystemProperty());
    }

    /**
     * 
     */
    public ConsoleSnapshotEventHandler(boolean enabled) {
        this.enabled = enabled;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.brekka.stillingar.core.snapshot.SnapshotEventHandler#noInitialSnapshot(org.brekka.stillingar.core.snapshot
     * .NoSnapshotAvailableException, boolean)
     */
    public void noInitialSnapshot(NoSnapshotAvailableException e, boolean defaultsAvailable) {
        if (!enabled) {
            return;
        }
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.brekka.stillingar.core.snapshot.SnapshotEventHandler#initialConfigure(org.brekka.stillingar.core.snapshot
     * .Snapshot, java.util.List)
     */
    public void initialConfigure(Snapshot snapshot, ChangeConfigurationException errors) {
        if (!enabled) {
            return;
        }
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.brekka.stillingar.core.snapshot.SnapshotEventHandler#refreshConfigure(org.brekka.stillingar.core.snapshot
     * .Snapshot, org.brekka.stillingar.core.RefreshConfigurationException)
     */
    public void refreshConfigure(Snapshot snapshot, ChangeConfigurationException refreshError) {
        if (!enabled) {
            return;
        }
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.brekka.stillingar.core.snapshot.SnapshotEventHandler#invalidSnapshotUpdate(org.brekka.stillingar.core.snapshot
     * .InvalidSnapshotException)
     */
    public void invalidSnapshotUpdate(InvalidSnapshotException e) {
        if (!enabled) {
            return;
        }
        // TODO Auto-generated method stub

    }

    /**
     * Look for a special system property that can be used to disable console output.
     * 
     * @return true if console output should be suppressed.
     */
    private static boolean isDisabledBySystemProperty() {
        String enabled = System.getProperty(SYSPROP_STILLINGAR_QUIET);
        return enabled == null || Boolean.parseBoolean(enabled);
    }
}
