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

package org.brekka.stillingar.example;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.brekka.stillingar.api.annotations.Configured;
import org.brekka.xml.stillingar.example.v1.ConfigurationDocument;
import org.brekka.xml.stillingar.example.v1.ConfigurationDocument.Configuration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration
@Configured
public class PollingReloadTest extends AbstractJUnit4SpringContextTests {

    @Configured("//c:MOTD")
    private String messageOfTheDay;
    
    private static File configFile;
    
    static {
        File tempDirectory = FileUtils.getTempDirectory();
        File subTempDirectory = new File(tempDirectory, "stillingar");
        subTempDirectory.mkdirs();
        configFile = new File(subTempDirectory, "stillingar-example.xml");
        writeConfig("Reload check");
        System.setProperty("stillingar.dir", subTempDirectory.getAbsolutePath());
        // Simulate JDK < 7
        System.setProperty("stillingar.reload-watcher.disabled", "true");
    }
    
    @AfterClass
    public static void done() {
        configFile.delete();
    }
    
	@Test
	public void test() throws Exception {
	    assertEquals("Reload check", messageOfTheDay);
	    Thread.sleep(2000);
	    for (int i = 0; i < 3; i++) {
	        String msg = "Message has been updated " + i;
	        writeConfig(msg);
	        Thread.sleep(3000);
	        assertEquals(msg, messageOfTheDay);
        }
	}
	
	private static void writeConfig(String message) {
	    ConfigurationDocument doc = ConfigurationDocument.Factory.newInstance();
        Configuration newConfiguration = doc.addNewConfiguration();
        newConfiguration.setMOTD(message);
        try {
            File newFile = File.createTempFile(configFile.getName(), ".tmp", configFile.getParentFile());
            File discardFile = File.createTempFile(configFile.getName(), ".tmp", configFile.getParentFile());
            doc.save(newFile);
            configFile.renameTo(discardFile);
            newFile.renameTo(configFile);
            discardFile.delete();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
	}
}
