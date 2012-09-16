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

import java.util.Map;

import org.brekka.stillingar.spring.resource.BaseDirectory;
import org.springframework.core.io.Resource;

/**
 * Base directory location that will be taken from an environment variable.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class EnvironmentVariableDirectory implements BaseDirectory {
    
    /**
     * The environment variable key
     */
    private final String var;
    
    /**
     * The map of environment variables.
     */
    private final Map<String, String> envMap;
    
    /**
     * @param var the name of the environment variable to load the location from
     */
    public EnvironmentVariableDirectory(String var) {
        this(var, System.getenv());
    }
    
    /**
     * Allow the map to resolve environment variables from to be set. Pretty much useful
     * only for unit testing.
     * 
     * @param var the name of the environment variable to load the location from
     * @param envMap the map of variables to load the location value from.
     */
    EnvironmentVariableDirectory(String var, Map<String, String> envMap) {
        this.var = var;
        this.envMap = envMap;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.spring.resource.BaseDirectory#getDirResource()
     */
    @Override
    public Resource getDirResource() {
        String envVar = envMap.get(var);
        return BaseDirUtils.resourceFromVariable(envVar, null);
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.spring.resource.BaseDirectory#getDisposition()
     */
    @Override
    public String getDisposition() {
        return String.format("Environment Variable '%s'", var);
    }
}
