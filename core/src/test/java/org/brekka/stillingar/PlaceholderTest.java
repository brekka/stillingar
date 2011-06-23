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

package org.brekka.stillingar;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Check that the {@link Placeholder} class throws the correct exception when any method of its proxies instance is called.
 * @author Andrew Taylor
 */
public class PlaceholderTest {

	private PlaceholderTestBean bean;
	
	@Before
	public void setUp() throws Exception {
		bean = new PlaceholderTestBean();
	}

	@Test(expected=IllegalStateException.class)
	public void testOf() {
		try {
			bean.process1();
		} catch (IllegalStateException e) {
			assertEquals("Instance variable <java.lang.Runnable> at line 5 of class " +
					"'org.brekka.stillingar.PlaceholderTestBean' has not yet been set",
					e.getMessage());
			throw e;
		}
	}

	@Test(expected=IllegalStateException.class)
	public void testOfWithLabel() {
		try {
			bean.process2();
		} catch (IllegalStateException e) {
			assertEquals("Instance variable 'placedWithLabel' <java.lang.Runnable> at line 7 of class " +
					"'org.brekka.stillingar.PlaceholderTestBean' has not yet been set", 
					e.getMessage());
			throw e;
		}
	}
}


