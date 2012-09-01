/**
 * 
 */
package org.brekka.stillingar.core.snapshot;

import java.util.List;

import org.brekka.stillingar.core.GroupConfigurationException;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 * 
 */
public interface SnapshotEventHandler {

    /**
     * Called on initialisation if the {@link SnapshotManager} could not locate any applicable snapshot. Provided as a
     * way to feedback to the developer/administrator the locations that were searched for valid configuration. If
     * <code>defaultsAvailable</code> is true then the situation is not necessarily fatal, the system could continue to
     * load on defaults only.
     * 
     * However, if <code>initialSnapshotRequired</code> is true and there is no snapshot, then a fatal error will occur.
     * This flag will normally be set when the application being configured must have custom settings defined such as
     * ldap/database configuration (for which there are no possible defaults).
     * 
     * @param e
     *            details about the locations searched and why they were not valid snapshots (missing, invalid, etc).
     * @param defaultsAvailable
     *            are there defaults available that the application could potentially use.
     */
    void noInitialSnapshot(NoSnapshotAvailableException e, boolean defaultsAvailable);

    /**
     * Called after the initial snapshot has been processed. If <code>errors</code> is empty then the snapshot loaded
     * without error.
     * 
     * @param snapshot
     *            the snapshot that was loaded.
     * @param errors
     *            any errors encountered.
     */
    void initialConfigure(Snapshot snapshot, List<GroupConfigurationException> errors);

    /**
     * When an updated configuration snapshot becomes available, this method will be called after the snapshot is used
     * to refresh the configuration and all listeners. If <code>errors</code> is empty then the snapshot loaded without
     * error.
     * 
     * @param snapshot
     *            the snapshot that was loaded.
     * @param errors
     *            any errors encountered.
     */
    void refreshConfigure(Snapshot snapshot, List<GroupConfigurationException> errors);

    /**
     * For when a snapshot change is detected, but that change is not valid. This is non fatal as the system will
     * continue with the existing configuration. Provided as a means to feedback to the developer/administrator 
     * that whatever change they made is not valid.
     * 
     * @param e the exception containing details about why the snapshot was invalid.
     */
    void invalidSnapshotUpdate(InvalidSnapshotException e);

}
