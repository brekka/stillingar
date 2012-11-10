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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.io.IOUtils;
import org.brekka.stillingar.api.ConfigurationException;
import org.brekka.stillingar.api.ValueConfigurationException;
import org.brekka.stillingar.core.ChangeConfigurationException;
import org.brekka.stillingar.core.GroupConfigurationException;
import org.brekka.stillingar.core.GroupConfigurationException.Phase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * ConsoleSnapshotEventHandlerTest
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class ConsoleSnapshotEventHandlerTest {
    
    
    private ConsoleSnapshotEventHandler handler;
    private StringWriter out = new StringWriter();
    private StringWriter err = new StringWriter();
    
    @Before
    public void setup() {
        handler = new ConsoleSnapshotEventHandler("test", true, new PrintWriter(out), new PrintWriter(err));
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.snapshot.ConsoleSnapshotEventHandler#noInitialSnapshot(org.brekka.stillingar.core.snapshot.NoSnapshotAvailableException, boolean)}.
     */
    @Test
    public void testNoInitialSnapshotDefaults() throws Exception {
        handler.noInitialSnapshot(new NoSnapshotAvailableException(
                new HashSet<String>(Arrays.asList("name")), 
                Arrays.<RejectedSnapshotLocation>asList(new RejectedSnapshotLocationBean("disp", "path", "msg"))),
                true);
//        assertEquals(defaults("testNoInitialSnapshotDefaults"), err());
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.snapshot.ConsoleSnapshotEventHandler#noInitialSnapshot(org.brekka.stillingar.core.snapshot.NoSnapshotAvailableException, boolean)}.
     */
    @Test
    public void testNoInitialSnapshotNoDefaults() throws Exception {
        handler.noInitialSnapshot(new NoSnapshotAvailableException(
                new HashSet<String>(Arrays.asList("name")), 
                Arrays.<RejectedSnapshotLocation>asList(new RejectedSnapshotLocationBean("disp", "path", "msg"))),
                false);
//        assertEquals(defaults("testNoInitialSnapshotNoDefaults"), err());
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.snapshot.ConsoleSnapshotEventHandler#initialConfigure(org.brekka.stillingar.core.snapshot.Snapshot, org.brekka.stillingar.core.ChangeConfigurationException)}.
     */
    @Test
    public void testInitialConfigureNoSnapshot() throws Exception {
        handler.initialConfigure(null, null);
        assertEquals("Configuration for 'test' loaded from defaults", out());
    }
    /**
     * Test method for {@link org.brekka.stillingar.core.snapshot.ConsoleSnapshotEventHandler#initialConfigure(org.brekka.stillingar.core.snapshot.Snapshot, org.brekka.stillingar.core.ChangeConfigurationException)}.
     */
    @Test
    public void testInitialConfigureSuccess() throws Exception {
        Snapshot snapshot = Mockito.mock(Snapshot.class);
        Mockito.when(snapshot.getLocation()).thenReturn(new URI("http://brekka.org/test"));
        handler.initialConfigure(snapshot, null);
        assertEquals("Snapshot for 'test' loaded successfully from 'http://brekka.org/test'", out());
    }
    /**
     * Test method for {@link org.brekka.stillingar.core.snapshot.ConsoleSnapshotEventHandler#initialConfigure(org.brekka.stillingar.core.snapshot.Snapshot, org.brekka.stillingar.core.ChangeConfigurationException)}.
     */
    @Test
    public void testInitialConfigureError() throws Exception {
        Snapshot snapshot = Mockito.mock(Snapshot.class);
        Mockito.when(snapshot.getLocation()).thenReturn(new URI("http://brekka.org/test"));
        ValueConfigurationException v = new ValueConfigurationException("no value", String.class, "/c:Test");
        GroupConfigurationException g = new GroupConfigurationException("group", 
                Phase.VALUE_ASSIGNMENT, Arrays.<ConfigurationException>asList(v));
        ChangeConfigurationException c = new ChangeConfigurationException("message", Arrays.asList(g));
        handler.initialConfigure(snapshot, c);
//        assertEquals(defaults("testInitialConfigureError"), err());
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.snapshot.ConsoleSnapshotEventHandler#refreshConfigure(org.brekka.stillingar.core.snapshot.Snapshot, org.brekka.stillingar.core.ChangeConfigurationException)}.
     */
    @Test
    public void testRefreshConfigureSuccess() throws Exception {
        Snapshot snapshot = Mockito.mock(Snapshot.class);
        Mockito.when(snapshot.getLocation()).thenReturn(new URI("http://brekka.org/test"));
        handler.refreshConfigure(snapshot, null);
        assertEquals("Snapshot for 'test' refreshed successfully from 'http://brekka.org/test'", out());
    }
    
    /**
     * Test method for {@link org.brekka.stillingar.core.snapshot.ConsoleSnapshotEventHandler#refreshConfigure(org.brekka.stillingar.core.snapshot.Snapshot, org.brekka.stillingar.core.ChangeConfigurationException)}.
     */
    @Test
    public void testRefreshConfigureError() throws Exception {
        Snapshot snapshot = Mockito.mock(Snapshot.class);
        Mockito.when(snapshot.getLocation()).thenReturn(new URI("http://brekka.org/test"));
        ValueConfigurationException v = new ValueConfigurationException("no value", String.class, "/c:Test");
        GroupConfigurationException g = new GroupConfigurationException("group", 
                Phase.VALUE_ASSIGNMENT, Arrays.<ConfigurationException>asList(v));
        ChangeConfigurationException c = new ChangeConfigurationException("message", Arrays.asList(g));
        handler.refreshConfigure(snapshot, c);
//        assertEquals(defaults("testRefreshConfigureError"), err());
    }

    /**
     * Test method for {@link org.brekka.stillingar.core.snapshot.ConsoleSnapshotEventHandler#invalidSnapshotUpdate(org.brekka.stillingar.core.snapshot.InvalidSnapshotException)}.
     */
    @Test
    public void testInvalidSnapshotUpdate() throws Exception {
        InvalidSnapshotException e = new InvalidSnapshotException("Something");
        handler.invalidSnapshotUpdate(e);
//        assertEquals(defaults("testInvalidSnapshotUpdate"), err());
    }
    
    /**
     * @param string
     * @return
     */
    private String defaults(String methodName) throws IOException {
        String fileName = getClass().getSimpleName() + "." + methodName + ".txt";
        InputStream is = getClass().getResourceAsStream(fileName);
        return IOUtils.toString(is);
    }

    
    private String err() throws IOException {
        err.close();
        return err.toString().trim();
    }
    
    private String out() throws IOException {
        out.close();
        return out.toString().trim();
    }
}
