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

import static java.lang.String.format;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.brekka.stillingar.api.ConfigurationException;
import org.brekka.stillingar.api.ConfigurationSource;
import org.brekka.stillingar.api.ConfigurationSourceLoader;
import org.brekka.stillingar.core.snapshot.InvalidSnapshotException;
import org.brekka.stillingar.core.snapshot.NoSnapshotAvailableException;
import org.brekka.stillingar.core.snapshot.Snapshot;
import org.brekka.stillingar.core.snapshot.SnapshotManager;
import org.brekka.stillingar.spring.resource.RejectedResourceHandler;
import org.brekka.stillingar.spring.resource.ResourceSelector;
import org.springframework.core.io.Resource;

/**
 * Snapshot manager based around resources.
 * 
 * @author Andrew Taylor
 */
public class ResourceSnapshotManager implements SnapshotManager {

	/**
	 * Will actually load the configuration sources
	 */
	private final ConfigurationSourceLoader configurationSourceLoader;

	/**
	 * Determines what resource will be used to load snapshots from.
	 */
	private final ResourceSelector resourceSelector;
	
	/**
	 * Monitors activity on the resource.
	 */
	private final ResourceMonitor resourceMonitor;
	
	/**
	 * Can listen for when a resource is rejected.
	 */
	private RejectedResourceHandler rejectedResourceHandler; 
	
	/**
	 * The configuration resource identified by the {@link ResourceSelector}.
	 */
	private Resource configurationResource;
	
	/**
	 * @param resourceSelector Determines where the resources that the snapshots will be based on will be loaded from.
	 * @param configurationSourceLoader Will actually load the snapshots
	 */
	public ResourceSnapshotManager(
			ResourceSelector resourceSelector,
			ConfigurationSourceLoader configurationSourceLoader,
			ResourceMonitor resourceMonitor) {
		this.resourceSelector = resourceSelector;
		this.configurationSourceLoader = configurationSourceLoader;
		this.resourceMonitor = resourceMonitor;
	}
	
	
	/* (non-Javadoc)
	 * @see org.brekka.stillingar.core.snapshot.SnapshotManager#retrieveInitial()
	 */
	@Override
	public Snapshot retrieveInitial() throws NoSnapshotAvailableException {
	    Resource configurationResource = resourceSelector.getResource();
	    this.configurationResource = configurationResource;
	    Snapshot snapshot = performLoad(configurationResource);
	    this.resourceMonitor.initialise(configurationResource);
	    return snapshot;
	}


    /* (non-Javadoc)
	 * @see org.brekka.stillingar.core.snapshot.SnapshotManager#retrieveUpdated()
	 */
	@Override
	public Snapshot retrieveUpdated() throws InvalidSnapshotException {
	    if (configurationResource == null) {
	        // No initial configuration yet, just return null
	        return null;
	    }
        if (resourceMonitor.hasChanged()) {
            Snapshot snapshot = performLoad(configurationResource);
            snapshot = performLoad(configurationResource);
            return snapshot;
        }
        return null;
	}
	
	/* (non-Javadoc)
     * @see org.brekka.stillingar.core.snapshot.SnapshotManager#reject(org.brekka.stillingar.core.snapshot.Snapshot)
     */
    @Override
    public void reject(Snapshot rejectedSnapshot) {
        if (rejectedSnapshot instanceof ResourceSnapshot) {
            ResourceSnapshot resourceSnapshot = (ResourceSnapshot) rejectedSnapshot;
            Resource resource = resourceSnapshot.getResource();
            if (rejectedResourceHandler != null) {
                rejectedResourceHandler.rejected(resource);
            }
        }
    }
	   
    /**
     * Perform the load operation that will convert a resource into a snapshot.
     * @param resourceToLoad the resouce to load into a snapshot
     * @return the snapshot loaded from the specified resource
     * @throws ConfigurationException if something goes wrong such as an IO error.
     */
    protected Snapshot performLoad(Resource resourceToLoad) {
        Snapshot snapshot = null;
        if (resourceToLoad != null && resourceToLoad.exists()
                && resourceToLoad.isReadable()) {
            InputStream sourceStream = null;
            try {
                sourceStream = resourceToLoad.getInputStream();
                long timestamp = resourceToLoad.lastModified();
                ConfigurationSource configurationSource = configurationSourceLoader.parse(sourceStream, null);
                snapshot = new ResourceSnapshot(configurationSource, new Date(timestamp), resourceToLoad);
            } catch (IOException e) {
                throw new ConfigurationException(format("Resouce '%s'", resourceToLoad), e);
            } catch (RuntimeException e) {
                // Wrap to include location details
                throw new ConfigurationException(format("Resouce '%s' processing problem", resourceToLoad), e);
            } finally {
                closeQuietly(sourceStream);
            }
        }
        return snapshot;
    }
    
    /**
     * @param rejectedResourceHandler the rejectedResourceHandler to set
     */
    public void setRejectedResourceHandler(RejectedResourceHandler rejectedResourceHandler) {
        this.rejectedResourceHandler = rejectedResourceHandler;
    }
	
	/**
	 * Close the steams
	 */
    private static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // Ignore
        }
    }

}
