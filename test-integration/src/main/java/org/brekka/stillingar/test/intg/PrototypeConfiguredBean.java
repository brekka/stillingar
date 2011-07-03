package org.brekka.stillingar.test.intg;

import java.util.Date;

import org.brekka.stillingar.annotations.ConfigurationListener;
import org.brekka.stillingar.annotations.Configured;
import org.brekka.xml.stillingar.test.intg.ConfigurationDocument.Configuration.Business;
import org.springframework.beans.factory.annotation.Qualifier;

@Configured
public class PrototypeConfiguredBean {

    @Configured("/c:Configuration/c:Business/c:Frequency")
    private float frequency;
    
    @ConfigurationListener
    public void configure() {
        System.out.printf("Prototype, Frequency: %f%n", frequency);
    }
}
