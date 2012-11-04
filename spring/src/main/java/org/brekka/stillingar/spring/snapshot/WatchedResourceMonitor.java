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

import org.brekka.stillingar.api.ConfigurationException;
import org.springframework.core.io.Resource;

/**
 * TODO Description of WatchedResourceMonitor
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class WatchedResourceMonitor implements ResourceMonitor {
    
    private final long timeout;
    
    private WatchService watchService;
    
    private Path resourceFile;
    
    /**
     * 
     */
    public WatchedResourceMonitor(long timeout) {
        this.timeout = timeout;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.spring.snapshot.ResourceMonitor#initialize(org.springframework.core.io.Resource)
     */
    @Override
    public void initialize(Resource resource) {
        try {
            this.resourceFile = resource.getFile().toPath();
            Path parent = resourceFile.getParent();
            this.watchService = parent.getFileSystem().newWatchService();
            parent.register(this.watchService, StandardWatchEventKinds.ENTRY_MODIFY, 
                    StandardWatchEventKinds.ENTRY_CREATE);
        } catch (IOException e) {
            throw new ConfigurationException("", e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.spring.snapshot.ResourceMonitor#hasChanged()
     */
    @Override
    public boolean hasChanged() {
        boolean changed = false;
        try {
            WatchKey poll = watchService.poll(timeout, TimeUnit.MILLISECONDS);
            if (poll != null) {
                List<WatchEvent<?>> pollEvents = poll.pollEvents();
                for (WatchEvent<?> watchEvent : pollEvents) {
                    Path name = (Path) watchEvent.context();
                    if (resourceFile.getFileName().equals(name)) {
                        changed = true;
                        break;
                    }
                }
                poll.reset();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return changed;
    }
}
