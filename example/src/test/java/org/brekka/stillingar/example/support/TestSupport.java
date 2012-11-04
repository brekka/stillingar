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

package org.brekka.stillingar.example.support;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.brekka.xml.stillingar.example.v1.ConfigurationDocument;

/**
 * Support for tests
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public final class TestSupport {
    
    /**
     * The config file
     */
    private static File configFile;
    
    /**
     * Write a configuration document to a temp file at a location that will be picked up by the tests.
     * @param doc the document to write
     */
    public static void write(ConfigurationDocument doc) {
        if (configFile == null) {
            // Do some preparations on the first time.
            File tempDirectory = FileUtils.getTempDirectory();
            File subTempDirectory = new File(tempDirectory, "stillingar");
            subTempDirectory.mkdirs();
            configFile = new File(subTempDirectory, "stillingar-example.xml");
            System.setProperty("stillingar.dir", subTempDirectory.getAbsolutePath());
            configFile.deleteOnExit();
        }
        try {
            File newFile = File.createTempFile(configFile.getName(), ".tmp", configFile.getParentFile());
            File discardFile = File.createTempFile(configFile.getName(), ".tmp", configFile.getParentFile());
            doc.save(newFile);
            if (configFile.exists()) {
                configFile.renameTo(discardFile);
            }
            newFile.renameTo(configFile);
            if (discardFile.exists()) {
                discardFile.delete();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
