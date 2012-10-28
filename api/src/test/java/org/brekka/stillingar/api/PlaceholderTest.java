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

package org.brekka.stillingar.api;

import static org.junit.Assert.*;

import org.brekka.stillingar.api.Placeholder;
import org.junit.Before;
import org.junit.Test;

/**
 * Check that the {@link Placeholder} class throws the correct exception when any method of its proxies instance is
 * called.
 * 
 * @author Andrew Taylor
 */
public class PlaceholderTest {

    private PlaceholderTestBean bean;

    @Before
    public void setUp() throws Exception {
        bean = new PlaceholderTestBean();
    }

    @Test
    public void testOf() {
        try {
            bean.process1();
            fail();
        } catch (IllegalStateException e) {
            assertEquals("Instance variable <java.lang.Runnable> at line 23 of class "
                    + "'org.brekka.stillingar.api.PlaceholderTestBean' has not yet been set", e.getMessage());
        }
    }

    @Test
    public void testOfWithLabel() {
        try {
            bean.process2();
            fail();
        } catch (IllegalStateException e) {
            assertEquals("Instance variable 'placedWithLabel' <java.lang.Runnable> at line 25 of class "
                    + "'org.brekka.stillingar.api.PlaceholderTestBean' has not yet been set", e.getMessage());
        }
    }

    @Test
    public void testIsPlaceholderValid() {
        Runnable val = Placeholder.of(Runnable.class);
        assertTrue(Placeholder.isPlaceholder(val));
    }

    @Test
    public void testIsPlaceholderNotValid() {
        assertFalse(Placeholder.isPlaceholder("Not a placeholder"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNullValueType() {
        Placeholder.of(null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNotInterfaceValueType() {
        Placeholder.of(String.class);
    }
    
    @Test(expected=IllegalStateException.class)
    public void testInvocation() {
        Runnable val = Placeholder.of(Runnable.class);
        val.run();
    }
    
    @Test
    public void testInvocationCached() {
        Runnable val = Placeholder.of(Runnable.class);
        IllegalStateException error = null;
        try {
            val.run();
        } catch (IllegalStateException e) {
            error = e;
        }
        IllegalStateException error2 = null;
        try {
            val.run();
        } catch (IllegalStateException e) {
            error2 = e;
        }
        assertSame(error, error2);
    }
}
