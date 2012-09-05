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

package org.brekka.stillingar.spring.resource.dir;

import java.io.File;

import org.brekka.stillingar.spring.resource.UnresolvableResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
final class BaseDirUtils {
    
    /**
     * Non-constructor
     */
    private BaseDirUtils() { }

    /**
     * @param envVar
     * @param object
     * @return
     */
    public static Resource resourceFromVariable(String baseFromVariable, String subPath) {
        if (baseFromVariable == null) {
            return new UnresolvableResource("Not set");
        }
        Resource resource;
        File baseDir = new File(baseFromVariable);
        resource = verifyDir(baseDir);
        File dir = baseDir;
        if (resource == null) {
            if (subPath != null) {
                dir = new File(baseDir, subPath);
                resource = verifyDir(dir);
            }
        }
        if (resource == null) {
            resource = new FileSystemResource(dir.getAbsolutePath() + "/");
        }
        return resource;
    }

    protected static Resource verifyDir(File baseDir) {
        if (!baseDir.exists()) {
            return new UnresolvableResource(String.format(
                    "Path '%s' does not exist or is inaccessible", baseDir));
        }
        if (!baseDir.isDirectory()) {
            return new UnresolvableResource(String.format(
                    "Path '%s' is not a directory", baseDir));
        }
        return null;
    }
    
    
    
}