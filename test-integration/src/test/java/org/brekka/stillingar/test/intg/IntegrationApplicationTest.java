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

package org.brekka.stillingar.test.intg;

import org.brekka.stillingar.test.intg.services.BusinessService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration
public class IntegrationApplicationTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	private BusinessService businessService;
	
	@Qualifier("something")
	@Autowired
	private Float something;
	
	@Qualifier("simpleBeanStatic")
	@Autowired
	private SimpleBean simpleBeanStatic;
	
	@Test
	public void test() throws Exception {
		businessService.doSomething();
		
		PrototypeConfiguredBean bean = applicationContext.getBean(PrototypeConfiguredBean.class);
		bean.getFrequency();
	}
	
}
