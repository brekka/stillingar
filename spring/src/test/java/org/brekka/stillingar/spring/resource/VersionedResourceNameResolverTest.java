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

package org.brekka.stillingar.spring.resource;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.brekka.stillingar.spring.version.ApplicationVersionResolver;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class VersionedResourceNameResolverTest {



    private VersionedResourceNameResolver resolver;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        resolver = new VersionedResourceNameResolver("test", new AVR("1.2.35"));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullPrefix() {
        new VersionedResourceNameResolver(null, null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullApplicationVersionResolver() {
        new VersionedResourceNameResolver("test", null);
    }
    
    @Test
    public void testNoVersion() {
        resolver = new VersionedResourceNameResolver("test", new AVR(null));
        Set<String> expected = new LinkedHashSet<String>(Arrays.asList("test.xml"));
        assertEquals(expected, resolver.getNames());
    }

    @Test
    public void testGetNamesMultiLevel() {
        Set<String> expected = new LinkedHashSet<String>(Arrays.asList("test-1.2.35.xml", "test-1.2.xml", "test-1.xml", "test.xml"));
        resolver.setVersionPattern(Pattern.compile("(((\\d+)\\.\\d+)\\.\\d+)"));
        assertEquals(expected, resolver.getNames());
    }
    
    @Test
    public void testGetNamesNoPatternGroup() {
        Set<String> expected = new LinkedHashSet<String>(Arrays.asList("test-1.2.35.xml", "test.xml"));
        resolver.setVersionPattern(Pattern.compile("[\\d+\\.]+"));
        assertEquals(expected, resolver.getNames());
    }
    
    @Test
    public void testGetNamesDifferentFormat() {
        Set<String> expected = new LinkedHashSet<String>(Arrays.asList("test-config-1.2.35.xml", "test-base.xml"));
        resolver.setVersionNameFormat("%s-config-%s.%s");
        resolver.setNameFormat("%s-base.%s");
        assertEquals(expected, resolver.getNames());
    }
    
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullVersionFormat() {
        resolver.setVersionNameFormat(null);
        resolver.getNames();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullNameFormat() {
        resolver.setNameFormat(null);
        resolver.getNames();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullPattern() {
        resolver.setVersionPattern(null);
        resolver.getNames();
    }
    
    private static final class AVR implements ApplicationVersionResolver {
        private final String version;
        
        /**
         * @param version
         */
        public AVR(String version) {
            this.version = version;
        }

        @Override
        public String identifyVersion() {
            return version;
        }
    }

}
