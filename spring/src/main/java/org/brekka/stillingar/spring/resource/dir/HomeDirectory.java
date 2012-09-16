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

import org.brekka.stillingar.spring.resource.BaseDirectory;
import org.springframework.core.io.Resource;

/**
 * A base directory that is either the user's home or a directory relative to it.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class HomeDirectory implements BaseDirectory {

    /**
     * The relative path within the user home.
     */
    private final String path;

    /**
     * The home directory itself
     */
    public HomeDirectory() {
        this(null);
    }

    /**
     * A base location that is a path within the user home.
     * 
     * @param path
     *            The relative path within the user home.
     */
    public HomeDirectory(String path) {
        this.path = path;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.spring.resource.BaseDirectory#getDirResource()
     */
    @Override
    public Resource getDirResource() {
        String userHome = System.getProperty("user.home");
        return BaseDirUtils.resourceFromVariable(userHome, path);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.spring.resource.BaseDirectory#getDisposition()
     */
    @Override
    public String getDisposition() {
        return "Home Directory";
    }

    /**
     * Retrieve the sub-path within the user home.
     * 
     * @return the path
     */
    public String getPath() {
        return path;
    }
}
