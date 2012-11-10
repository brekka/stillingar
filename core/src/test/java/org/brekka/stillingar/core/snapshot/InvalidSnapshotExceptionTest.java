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

import org.junit.Test;

/**
 * InvalidSnapshotExceptionTest
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class InvalidSnapshotExceptionTest {

    /**
     * Test method for {@link org.brekka.stillingar.core.snapshot.InvalidSnapshotException#InvalidSnapshotException(java.lang.String, java.lang.Throwable)}.
     */
    @Test()
    public void testInvalidSnapshotExceptionStringThrowable() {
        InvalidSnapshotException e = new InvalidSnapshotException("Error");
        assertEquals("Error", e.getMessage());
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.snapshot.InvalidSnapshotException#InvalidSnapshotException(java.lang.String)}.
     */
    @Test
    public void testInvalidSnapshotExceptionString() {
        RuntimeException re = new RuntimeException();
        InvalidSnapshotException e = new InvalidSnapshotException("Error", re);
        assertEquals("Error", e.getMessage());
        assertSame(re, e.getCause());
    }

}
