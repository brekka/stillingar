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

import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import org.brekka.stillingar.core.ChangeConfigurationException;
import org.brekka.stillingar.core.GroupConfigurationException;

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
     * Name of this application
     */
    private final String applicationName;
    
    /**
     * Standard output
     */
    private final PrintWriter out;
    
    /**
     * Standard error
     */
    private final PrintWriter err;

    /**
     * Default console handler
     */
    public ConsoleSnapshotEventHandler() {
        this("?");
    }
    
    /**
     * Include the specified application name in error messages (recommended).
     * @param applicationName the name of the application being configured
     */
    public ConsoleSnapshotEventHandler(String applicationName) {
        this(applicationName, !isDisabledBySystemProperty());
    }

    /**
     * Specify both the application name and determine whether output of messages should be enabled
     * @param applicationName the name of the application being configured
     * @param enabled when set to false, this instance effectively becomes a noop.
     */
    public ConsoleSnapshotEventHandler(String applicationName, boolean enabled) {
        this.enabled = enabled;
        this.applicationName = applicationName;
        this.out = new PrintWriter(System.out);
        this.err = new PrintWriter(System.err);
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
        writeNoInitialSnapshotSummary(this.err, defaultsAvailable, applicationName, e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.brekka.stillingar.core.snapshot.SnapshotEventHandler#initialConfigure(org.brekka.stillingar.core.snapshot
     * .Snapshot, java.util.List)
     */
    public void initialConfigure(Snapshot snapshot, ChangeConfigurationException e) {
        if (!enabled) {
            return;
        }
        if (snapshot == null) {
            this.out.printf("Configuration for '%s' loaded from defaults%n", applicationName);
        } else if (e == null) {
            this.out.printf("Snapshot for '%s' loaded successfully from '%s'%n", applicationName, snapshot.getLocation());
        } else {
            this.err.printf("Problem for '%s' loading snapshot '%s'%n", applicationName, snapshot.getLocation());
            int cnt = 1;
            for (GroupConfigurationException groupConfigurationException : e.getGroupErrors()) {
                this.err.printf("Group error %d%n:", cnt++);
                groupConfigurationException.printStackTrace(this.err);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.brekka.stillingar.core.snapshot.SnapshotEventHandler#refreshConfigure(org.brekka.stillingar.core.snapshot
     * .Snapshot, org.brekka.stillingar.core.RefreshConfigurationException)
     */
    public void refreshConfigure(Snapshot snapshot, ChangeConfigurationException e) {
        if (!enabled) {
            return;
        }
        if (e == null) {
            this.out.printf("Snapshot for '%s' refreshed successfully from '%s'%n", applicationName, snapshot.getLocation());
        } else {
            this.err.printf("Problem refreshing for '%s' snapshot '%s'%n", applicationName, snapshot.getLocation());
            int cnt = 1;
            for (GroupConfigurationException groupConfigurationException : e.getGroupErrors()) {
                this.err.printf("Group error %d%n:", cnt++);
                groupConfigurationException.printStackTrace(this.err);
            }
        }
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
        this.err.printf("Failed to update configuration for '%s' from snapshot%n", applicationName);
    }
    
    /**
     * Write a summary of the information contained within NoSnapshotAvailableException, to the specified print writer.
     * 
     * @param out
     *            the writer to write to
     * @param defaultsAvailable
     *            were defaults available?
     * @param applicationName
     *            the name of the application.
     * @param e
     *            the exception thrown.
     */
    public static void writeNoInitialSnapshotSummary(PrintWriter out, boolean defaultsAvailable, 
            String applicationName, NoSnapshotAvailableException e) {
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
