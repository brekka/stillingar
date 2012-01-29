package org.brekka.stillingar.test.intg;

import org.brekka.stillingar.annotations.ConfigurationListener;
import org.brekka.stillingar.annotations.Configured;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;

@Configured
public class ContextBean implements InitializingBean  {
    
    
    @Configured("/c:Configuration/c:Context/beans:beans")
    private ApplicationContext applicationContext;
    
    @Configured("/c:Configuration/c:Context/beans:beans")
    public void setContext(ApplicationContext applicationContext) {
        System.out.println(applicationContext);
    }
    
    public ApplicationContext getContext() {
        return null;
    }
    
    public void afterPropertiesSet() throws Exception {
         System.out.println(applicationContext);
    }
    
    @ConfigurationListener
    public void configure(@Configured("/c:Configuration/c:Context/beans:beans") ApplicationContext applicationContext) {
        System.out.println(applicationContext);
    }
}
