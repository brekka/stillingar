/**
 * 
 */
package org.brekka.stillingar.test.intg;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.brekka.stillingar.core.ConfigurationSource;
import org.brekka.xml.stillingar.test.v1.ConfigurationDocument.Configuration.CompanyY;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
