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

package org.brekka.stillingar.core.snapshot;

/**
 * RejectedSnapshotLocationBean
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
class RejectedSnapshotLocationBean implements RejectedSnapshotLocation {

    private final String disposition;
    private final String path;
    private final String message;

    /**
     * @param disposition
     * @param path
     * @param message
     */
    public RejectedSnapshotLocationBean(String disposition, String path, String message) {
        this.disposition = disposition;
        this.path = path;
        this.message = message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.snapshot.RejectedSnapshotLocation#getDisposition()
     */
    @Override
    public String getDisposition() {
        return disposition;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.snapshot.RejectedSnapshotLocation#getPath()
     */
    @Override
    public String getPath() {
        return path;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.snapshot.RejectedSnapshotLocation#getMessage()
     */
    @Override
    public String getMessage() {
        return message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "[" + getDisposition() + " - " + getPath() + " - " + getMessage() + "]";
    }
}
