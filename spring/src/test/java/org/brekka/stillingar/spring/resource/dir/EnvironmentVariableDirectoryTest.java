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

package org.brekka.stillingar.spring.resource.dir;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.brekka.stillingar.spring.resource.UnresolvableResource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class EnvironmentVariableDirectoryTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }
    
    @Test
    public void disposition() {
        assertEquals("Environment Variable 'TEST'", new EnvironmentVariableDirectory("TEST").getDisposition());
    }

    @Test
    public void found() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("TEST", System.getProperty("java.io.tmpdir"));
        EnvironmentVariableDirectory envVarDir = new EnvironmentVariableDirectory("TEST", map);
        Resource dirResource = envVarDir.getDirResource();
        assertEquals(System.getProperty("java.io.tmpdir"), dirResource.getFile().getAbsolutePath());
    }
    
    @Test
    public void notFound() throws Exception {
        EnvironmentVariableDirectory envVarDir = new EnvironmentVariableDirectory("TEST");
        Resource dirResource = envVarDir.getDirResource();
        assertTrue(dirResource instanceof UnresolvableResource);
    }

}
