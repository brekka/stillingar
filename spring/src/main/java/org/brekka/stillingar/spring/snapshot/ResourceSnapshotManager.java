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

import org.brekka.stillingar.core.ConfigurationException;
import org.brekka.stillingar.core.ConfigurationSource;
import org.brekka.stillingar.core.ConfigurationSourceLoader;
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
	 * Can listen for when a resource is rejected.
	 */
	private RejectedResourceHandler rejectedResourceHandler; 
	
	/**
	 * The configuration resource identified by the {@link ResourceSelector}.
	 */
	private Resource configurationResource;
	
	/**
	 * The lastModified of the original at the time we last tried to read it. If it throws an exception
	 * then we should not attempt to parse again until the resource last modified is greater than this
	 * value.
	 */
	private long lastAttempt;
	
	/**
	 * 
	 * @param resourceSelector Determines where the resources that the snapshots will be based on will be loaded from.
	 * @param snapshotLoader Will actually load the snapshots
	 */
	public ResourceSnapshotManager(
			ResourceSelector resourceSelector,
			ConfigurationSourceLoader configurationSourceLoader) {
		this.resourceSelector = resourceSelector;
		this.configurationSourceLoader = configurationSourceLoader;
	}
	

	
	/* (non-Javadoc)
	 * @see org.brekka.stillingar.core.snapshot.SnapshotManager#retrieveInitial()
	 */
	@Override
	public Snapshot retrieveInitial() throws NoSnapshotAvailableException {
	    Snapshot snapshot = null;
	    Resource configurationResource = resourceSelector.getResource();
	    this.configurationResource = configurationResource;
	    return snapshot;
	}


    /* (non-Javadoc)
	 * @see org.brekka.stillingar.core.snapshot.SnapshotManager#retrieveUpdated()
	 */
	@Override
	public Snapshot retrieveUpdated() throws InvalidSnapshotException {
	    if (configurationResource == null) {
	        throw new ConfigurationException("Cannot call 'retrieveUpdated' until 'retrieveInitial' has been called" +
	        		" for the first time");
	    }
	    long resourceLastModified;
        try {
            resourceLastModified = configurationResource.lastModified();
        } catch (IOException e) {
            throw new ConfigurationException(String.format(
                    "Unable to determine the last modified for the resource '%s'", configurationResource));
        }
	    if (resourceLastModified <= lastAttempt) {
	        // Resource has not changed
	        return null;
	    }
	    
	    try {
	        Snapshot snapshot = performLoad(configurationResource);
            snapshot = performLoad(configurationResource);
            validate(snapshot);
            this.lastAttempt = resourceLastModified;
            return snapshot;
        } catch (ConfigurationException e) {
            throw new InvalidSnapshotException(String.format(
                    "Error extracting snapshot from resource '%s'", configurationResource), e);
        }
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
     * @param snapshot
     */
	protected void validate(Snapshot snapshot) {
        // TODO Auto-generated method stub
        
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
