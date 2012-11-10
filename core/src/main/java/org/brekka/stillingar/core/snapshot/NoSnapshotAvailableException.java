/**
 * 
 */
package org.brekka.stillingar.core.snapshot;

import java.util.List;
import java.util.Set;

/**
 * Thrown when no configuration snapshot can be found. Provides details of the locations that were searched to assist
 * with resolving the problem.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class NoSnapshotAvailableException extends Exception {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 2289329974658207789L;

    /**
     * The resource names (ie file names) that were searched for in each location
     */
    private final Set<String> snapshotResourceNames;

    /**
     * The list of locations that were searched for resources with the names above.
     */
    private final List<RejectedSnapshotLocation> locations;

    /**
     * @param snapshotResourceNames The resource names (ie file names) that were searched for in each location
     * @param locations The list of locations that were searched for resources with the names above.
     */
    public NoSnapshotAvailableException(Set<String> snapshotResourceNames, List<RejectedSnapshotLocation> locations) {
        this.snapshotResourceNames = snapshotResourceNames;
        this.locations = locations;
    }

    /**
     * The resource names (ie file names) that were searched for in each location
     * @return
     */
    public Set<String> getSnapshotResourceNames() {
        return snapshotResourceNames;
    }

    /**
     * The list of locations that were searched for resources with the names above.
     * @return
     */
    public List<RejectedSnapshotLocation> getLocations() {
        return locations;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Throwable#getLocalizedMessage()
     */
    @Override
    public String getMessage() {
        return String.format("Unable to find configuration with any of the names %s in the locations: %s",
                snapshotResourceNames, locations);
    }
}
