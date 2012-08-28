/**
 * 
 */
package org.brekka.stillingar.spring.resource;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
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
        resolver = new VersionedResourceNameResolver("test", new ApplicationVersionResolver() {
            @Override
            public String identifyVersion() {
                return "1.2.35";
            }
        });
    }

    /**
     * Test method for {@link org.brekka.stillingar.spring.resource.VersionedResourceNameResolver#getNames()}.
     */
    @Test
    public void testGetNames() {
        List<String> expected = Arrays.asList("test-1.2.35.xml", "test-1.2.xml", "test-1.xml", "test.xml");
        resolver.setVersionPattern(Pattern.compile("(((\\d+)\\.\\d+)\\.\\d+)"));
        assertEquals(expected, resolver.getNames());
    }

}
