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

package org.brekka.stillingar.spring.bpp;

import static org.junit.Assert.*;

import org.brekka.stillingar.core.ReferentUpdateException;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO Description of InvocationChangeListenerSupportTest
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class InvocationChangeListenerSupportTest {


    /**
     * Test method for {@link org.brekka.stillingar.spring.bpp.InvocationChangeListenerSupport#throwError(java.lang.String, java.lang.Object, java.lang.Throwable)}.
     */
    @Test
    public void testThrowError() {
        TestInvocationChangeListenerSupport testInvocationChangeListenerSupport = new TestInvocationChangeListenerSupport(
                "bean", String.class, false, "Method");
        IllegalAccessException iae = new IllegalAccessException();
        try {
            testInvocationChangeListenerSupport.throwError("test", "value", iae);
        } catch (ReferentUpdateException e) {
            assertEquals("Method 'test' of java.lang.String (Single): expected 'java.lang.String', actual 'java.lang.String'", e.getMessage());
            assertSame(iae, e.getCause());
        }
    }

    private class TestInvocationChangeListenerSupport extends InvocationChangeListenerSupport<String> {
        
        /**
         * @param target
         * @param expectedValueType
         * @param list
         * @param referentTypeLabel
         */
        public TestInvocationChangeListenerSupport(Object target, Class<?> expectedValueType, boolean list,
                String referentTypeLabel) {
            super(target, expectedValueType, list, referentTypeLabel);
        }

        /* (non-Javadoc)
         * @see org.brekka.stillingar.spring.bpp.PrototypeValueChangeListener#onChange(java.lang.Object, java.lang.Object, java.lang.Object)
         */
        @Override
        public void onChange(String newValue, String oldValue, Object target) {
            
        }
    }
}
