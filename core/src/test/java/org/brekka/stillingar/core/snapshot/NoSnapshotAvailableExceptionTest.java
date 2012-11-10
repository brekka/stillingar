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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * TODO Description of NoSnapshotAvailableExceptionTest
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class NoSnapshotAvailableExceptionTest {

    /**
     * Test method for {@link org.brekka.stillingar.core.snapshot.NoSnapshotAvailableException#NoSnapshotAvailableException(java.util.Set, java.util.List)}.
     */
    @Test
    public void testNoSnapshotAvailableException() {
        Set<String> names = new HashSet<String>(Arrays.asList("file-1.1.xml", "file-1.xml", "file.xml"));
        List<RejectedSnapshotLocation> rejected = new ArrayList<RejectedSnapshotLocation>(Arrays.asList(
                new RejectedSnapshotLocationBean("cookie", "/path", "a message")));
        NoSnapshotAvailableException e = new NoSnapshotAvailableException(names, rejected);
        assertSame(names, e.getSnapshotResourceNames());
        assertSame(rejected, e.getLocations());
        assertEquals("Unable to find configuration with any of the names " +
        		"[file-1.xml, file-1.1.xml, file.xml] in the locations: " +
        		"[[cookie - /path - a message]]", e.getLocalizedMessage());
    }


}
