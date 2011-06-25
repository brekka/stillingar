package org.brekka.stillingar.test.intg;

import org.brekka.stillingar.test.intg.services.BusinessService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration
public class IntegrationApplicationTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	private BusinessService businessService;
	
	@Test
	public void test() throws Exception {
		businessService.doSomething();
		Thread.sleep(60000);
	}
	
}
