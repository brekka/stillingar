/**
 * 
 */
package org.brekka.stillingar.core.snapshot;

import java.net.URI;
import java.util.Date;

import org.brekka.stillingar.core.ConfigurationSource;

/**
 * Immutable implementation of {@link Snapshot}.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public final class DefaultSnapshot implements Snapshot {

    private final ConfigurationSource configurationSource;
    
    private final Date timestamp;
    
    private final URI location;
    
    
    public DefaultSnapshot(ConfigurationSource configurationSource, Date timestamp, URI location) {
        this.configurationSource = configurationSource;
        this.timestamp = timestamp;
        this.location = location;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.snapshot.Snapshot#getSource()
     */
    public ConfigurationSource getSource() {
        return configurationSource;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.snapshot.Snapshot#getTimestamp()
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.snapshot.Snapshot#getLocation()
     */
    public URI getLocation() {
        return location;
    }

}
