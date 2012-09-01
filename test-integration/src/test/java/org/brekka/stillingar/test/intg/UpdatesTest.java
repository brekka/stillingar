/**
 * 
 */
package org.brekka.stillingar.test.intg;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.brekka.stillingar.core.ConfigurationSource;
import org.brekka.xml.stillingar.test.intg.v1.ConfigurationDocument.Configuration.CompanyY;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class UpdatesTest {

    @Test
    public void test() throws Exception {
        // Copy configuration to temp location
        File targetDir = new File("target");
        System.setProperty("target.path", targetDir.getAbsolutePath());
        targetDir.mkdirs();
        File configFile = new File(targetDir, "updates.xml");
        FileUtils.copyURLToFile(getClass().getResource("config/IntegrationTestConfig_1.xml"), configFile);
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                "updates-context.xml", UpdatesTest.class);
        
        ConfigurationSource configurationSource = applicationContext.getBean("integration-config", ConfigurationSource.class);
        CompanyY companyY = configurationSource.retrieve(CompanyY.class);
        String url = companyY.getWarehouseWebService().getURL();
        assertEquals("http://example.org/CompanyY", url);
        assertEquals("Username", companyY.getWarehouseWebService().getUsername());
        
        FileUtils.copyURLToFile(getClass().getResource("config/IntegrationTestConfig_2.xml"), configFile);
        
        Thread.sleep(1000);
        
        companyY = configurationSource.retrieve(CompanyY.class);
        
        assertEquals("Username5", companyY.getWarehouseWebService().getUsername());
    }
}
