/**
 * 
 */
package org.brekka.stillingar.spring.snapshot;

import org.brekka.stillingar.core.snapshot.SnapshotBasedConfigurationSource;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class ConfigurationSnapshotRefresher implements Runnable {

    private final SnapshotBasedConfigurationSource snapshotBasedConfigurationSource;
    
    
    /**
     * @param snapshotBasedConfigurationSource
     */
    public ConfigurationSnapshotRefresher(SnapshotBasedConfigurationSource snapshotBasedConfigurationSource) {
        this.snapshotBasedConfigurationSource = snapshotBasedConfigurationSource;
    }


    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        snapshotBasedConfigurationSource.refresh();
    }

}
