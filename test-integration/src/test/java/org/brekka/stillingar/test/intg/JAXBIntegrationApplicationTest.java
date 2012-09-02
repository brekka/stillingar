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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.brekka.stillingar.core.ConfigurationSource;
import org.brekka.stillingar.jaxb.test.intg.Configuration.CompanyX;
import org.brekka.stillingar.jaxb.test.intg.Configuration.CompanyY;
import org.brekka.stillingar.jaxb.test.intg.Configuration.Services.Rules;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration
public class JAXBIntegrationApplicationTest extends AbstractJUnit4SpringContextTests {

	@Test
	public void test() throws Exception {
		
		ConfigurationSource configurationSource = applicationContext.getBean("integration-config", ConfigurationSource.class);
		
		CompanyY companyY = configurationSource.retrieve("/c:Configuration/c:CompanyY", CompanyY.class);
		assertEquals("Username", companyY.getWarehouseWebService().getUsername());
		
		assertTrue(configurationSource.isAvailable("//c:Rules/c:Transaction/c:MaxAmount"));
		
		BigDecimal maxAmount = configurationSource.retrieve("//c:Rules/c:Transaction/c:MaxAmount", BigDecimal.class);
		assertEquals(new BigDecimal("50000.73"), maxAmount);
		
		List<String> keywords = configurationSource.retrieveList("//c:Rules/c:Fraud/c:Keyword", String.class);
		assertEquals(Arrays.asList("KeywordA", "KeywordB", "KeywordC"), keywords);
		
		CompanyX companyX = configurationSource.retrieve(CompanyX.class);
		assertNotNull(companyX);
		
		assertTrue(configurationSource.isAvailable(CompanyY.class));
		
		assertTrue(configurationSource.isAvailable(Rules.class));
		
		List<String> strings = configurationSource.retrieveList(String.class);
		assertTrue(strings.size() > 0);
		
		String keyword = configurationSource.retrieve("//c:Rules/c:Fraud/c:Keyword[2]", String.class);
		assertEquals("KeywordB", keyword);
	}
	
}
