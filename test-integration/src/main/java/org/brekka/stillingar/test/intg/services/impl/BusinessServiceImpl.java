package org.brekka.stillingar.test.intg.services.impl;

import java.util.Date;

import org.brekka.stillingar.annotations.ConfigurationListener;
import org.brekka.stillingar.annotations.Configured;
import org.brekka.stillingar.test.intg.services.BusinessService;
import org.brekka.xml.stillingar.test.intg.ConfigurationDocument.Configuration.Business;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Configured
@Service
public class BusinessServiceImpl implements BusinessService {

	@Configured("/c:Configuration/c:Business/c:Frequency")
	private float frequency;
	
	public void doSomething() {
		System.out.println("Done something");
		
	}
	
	@ConfigurationListener
	public void configure(@Qualifier("dynamicBean") 
	                      Date theDate, 
	                      @Configured 
	                      Business business) {
		System.out.printf("Configured, Frequence: %f, date: %s, business: %s%n", frequency, theDate, business);
	}
}
