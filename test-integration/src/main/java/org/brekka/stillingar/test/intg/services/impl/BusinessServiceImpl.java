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
	
	private int configureCount = 0;
	
	private Date configureDate;
	
	private Business configureBusiness;

	// Business method
	public void doSomething() {
	}
	
	@ConfigurationListener
	public void configure(@Qualifier("dynamicBean") 
	                      Date theDate, 
	                      @Configured 
	                      Business business) {
	    configureCount++;
	    this.configureDate = theDate;
	    this.configureBusiness = business;
	}
	
	public int getConfigureCount() {
        return configureCount;
    }
	
	public Business getConfigureBusiness() {
        return configureBusiness;
    }
	
	public Date getConfigureDate() {
        return configureDate;
    }
	
	public float getFrequency() {
        return frequency;
    }
}
