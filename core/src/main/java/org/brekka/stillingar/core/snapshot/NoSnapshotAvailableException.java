/**
 * 
 */
package org.brekka.stillingar.core.snapshot;

import java.util.List;
import java.util.Set;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class NoSnapshotAvailableException extends Exception {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 2289329974658207789L;

    private final Set<String> snapshotResourceNames;
    
    private final List<RejectedSnapshotLocation> locations;

    public NoSnapshotAvailableException(Set<String> snapshotResourceNames, List<RejectedSnapshotLocation> locations) {
        this.snapshotResourceNames = snapshotResourceNames;
        this.locations = locations;
    }

    public Set<String> getSnapshotResourceNames() {
        return snapshotResourceNames;
    }

    public List<RejectedSnapshotLocation> getLocations() {
        return locations;
    }
    
}
