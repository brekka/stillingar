package org.brekka.stillingar.test.intg;

import org.brekka.stillingar.test.intg.services.BusinessService;
import org.junit.Ignore;
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
		
		applicationContext.getBean(PrototypeConfiguredBean.class);
	}
	
}
