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

package org.brekka.stillingar.spring.snapshot;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brekka.stillingar.api.ConfigurationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;

/**
 * Monitors for changes to a resource using the JDK 7 "watch" capability. This results in the {@link #hasChanged()}
 * method becoming blocking, waiting for a change to occur. To avoid blocking indefinately, a timeout can be specified
 * after which {@link #hasChanged()} will give up and return false.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class WatchedResourceMonitor implements ResourceMonitor, DisposableBean {
    /**
     * Logger
     */
    private static final Log log = LogFactory.getLog(WatchedResourceMonitor.class);

    /**
     * The timeout for the {@link #hasChanged()} method.
     */
    private final long timeout;

    /**
     * The service doing the watching of the resource directory.
     */
    private WatchService watchService;

    /**
     * The actual resource as a path
     */
    private Path resourceFile;

    /**
     * Watch key
     */
    private WatchKey watchKey;

    /**
     * Watch with no timeout
     */
    public WatchedResourceMonitor() {
        this(0);
    }

    /**
     * Watch with a timeout. If no change is detected within this time, the call to {@link #hasChanged()} will return
     * null.
     * 
     * @param timeout
     *            the timeout in milliseconds.
     */
    public WatchedResourceMonitor(long timeout) {
        this.timeout = timeout;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.spring.snapshot.ResourceMonitor#initialize(org.springframework.core.io.Resource)
     */
    @Override
    public void initialise(Resource resource) {
        try {
            this.resourceFile = resource.getFile().toPath();
            Path parent = resourceFile.getParent();
            this.watchService = parent.getFileSystem().newWatchService();
            this.watchKey = parent.register(this.watchService, StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_CREATE);
        } catch (IOException e) {
            throw new ConfigurationException(String.format(
                    "Failed to initialize watcher for resource '%s'", resource.toString()), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.spring.snapshot.ResourceMonitor#hasChanged()
     */
    @Override
    public boolean hasChanged() {
        if (!watchKey.isValid()) {
            return false;
        }
        boolean changed = false;
        try {
            WatchKey wKey;
            if (timeout > 0) {
                wKey = watchService.poll(timeout, TimeUnit.MILLISECONDS);
            } else {
                // Indefinite blocking
                wKey = watchService.take();
            }
            if (wKey != null) {
                if (wKey != this.watchKey) {
                    throw new IllegalStateException("WatchKey does not match that registered with the service");
                }
                List<WatchEvent<?>> pollEvents = wKey.pollEvents();
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Found %d events", pollEvents.size()));
                }
                for (WatchEvent<?> watchEvent : pollEvents) {
                    Path name = (Path) watchEvent.context();
                    if (resourceFile.getFileName().equals(name)) {
                        changed = true;
                        if (log.isInfoEnabled()) {
                            log.info(String.format("Found change to file '%s'", name));
                        }
                        break;
                    }
                }
                wKey.reset();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return changed;
    }

    @Override
    public void destroy() throws Exception {
        if (log.isInfoEnabled()) {
            log.info(String.format("Shutdown watch on '%s'", resourceFile));
        }
        if (watchKey != null) {
            watchKey.cancel();
        }
        if (watchService != null) {
            watchService.close();
        }
    }

    @Override
    public boolean canMonitor(Resource resource) {
        try {
            return resource.getURI().getScheme().startsWith("file");
        } catch (IOException e) {
            throw new ConfigurationException(String.format(
                "Failed to test watcher for ability to monitor resource '%s'", resource.toString()), e);
        }
    }
    
}
