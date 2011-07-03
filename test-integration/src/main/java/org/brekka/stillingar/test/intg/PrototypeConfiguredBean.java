package org.brekka.stillingar.test.intg;

import org.brekka.stillingar.annotations.ConfigurationListener;
import org.brekka.stillingar.annotations.Configured;

@Configured
public class PrototypeConfiguredBean {

    @Configured("/c:Configuration/c:Business/c:Frequency")
    private float frequency;
    
    private boolean configureCalled;
    
    @ConfigurationListener
    public void configure() {
        configureCalled = true;
    }
    
    
    public float getFrequency() {
        return frequency;
    }
    
    public boolean isConfigureCalled() {
        return configureCalled;
    }
}
