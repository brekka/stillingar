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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brekka.stillingar.core.ConfigurationException;
import org.brekka.stillingar.core.snapshot.Snapshot;
import org.brekka.stillingar.core.snapshot.SnapshotLoader;
import org.brekka.stillingar.core.snapshot.SnapshotManager;
import org.brekka.stillingar.spring.resource.ResourceSelector;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * Snapshot manager based around resources.
 * 
 * @author Andrew Taylor
 */
public class ResourceSnapshotManager implements SnapshotManager {

	private static final Log log = LogFactory
			.getLog(ResourceSnapshotManager.class);

	/**
	 * 
	 */
	private final SnapshotLoader snapshotLoader;

	/**
	 * 
	 */
	private final ResourceSelector selectedConfigurationSource;

	/**
	 * The last snapshot returned by {@link #retrieveLatest()}
	 */
	private Snapshot latestSnapshot;

	public ResourceSnapshotManager(
			ResourceSelector selectedConfigurationSource,
			SnapshotLoader snapshotLoader) {
		this.selectedConfigurationSource = selectedConfigurationSource;
		this.snapshotLoader = snapshotLoader;

	}

	public Snapshot retrieveLatest() {
		Snapshot snapshot = null;
		Resource original = selectedConfigurationSource.getOriginal();
		try {
			long lastModifiedMillis = 0;
			if (latestSnapshot != null) {
				lastModifiedMillis = latestSnapshot.getTimestamp();
			}
			if (original.lastModified() > lastModifiedMillis) {
				snapshot = retrieve(original);
				latestSnapshot = snapshot;
			}
		} catch (IOException e) {
			if (log.isWarnEnabled()) {
				log.warn(format("Failed to determine last modified for resource '%s'",
								original), e);
			}
		}
		return snapshot;
	}

	public Snapshot retrieveLastGood() {
		Resource lastGood = selectedConfigurationSource.getLastGood();
		return retrieve(lastGood);
	}

	protected Snapshot retrieve(Resource resourceToLoad) {
		Snapshot snapshot = null;
		if (resourceToLoad != null && resourceToLoad.exists()
				&& resourceToLoad.isReadable()) {
			try {
				URL url = resourceToLoad.getURL();
				long timestamp = resourceToLoad.lastModified();
				snapshot = snapshotLoader.load(url, timestamp);
			} catch (IOException e) {
				throw new ConfigurationException(format("Resouce '%s'", resourceToLoad), e);
			} catch (RuntimeException e) {
				// Wrap to include location details
				throw new ConfigurationException(format("Resouce '%s' processing problem", resourceToLoad), e);
			}
		}
		return snapshot;
	}

	public void acceptLatest() {
		Resource original = selectedConfigurationSource.getOriginal();
		Resource lastGood = selectedConfigurationSource.getLastGood();
		if (lastGood instanceof FileSystemResource) {
			FileSystemResource fsResource = (FileSystemResource) lastGood;
			File file = fsResource.getFile();
			InputStream is = null;
			OutputStream os = null;
			try {
				is = original.getInputStream();
				os = new FileOutputStream(file);
				copy(is, os);
			} catch (IOException e) {
				if (log.isWarnEnabled()) {
					// TODO more detail
					log.warn("Failed to copy original to lastGood", e);
				}
			} finally {
				closeQuietly(is);
				closeQuietly(os);
			}
		}
	}

    /**
     * Method content based on 'copyLarge' from Apache Commons IO.
     */
	private static long copy(InputStream input, OutputStream output)
			throws IOException {
		byte[] buffer = new byte[4096];
		long count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
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
