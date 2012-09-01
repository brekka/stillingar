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
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public enum PlatformDirectory implements BaseDirectory {
    
    Tomcat {
        @Override
        public Resource getDirResource() {
            String prop = System.getProperty("catalina.base");
            return BaseDirUtils.resourceFromVariable(prop, "conf");
        }
    },
    
    Glassfish {
        @Override
        public Resource getDirResource() {
            String prop = System.getProperty("com.sun.aas.instanceRoot");
            return BaseDirUtils.resourceFromVariable(prop, "config");
        }
    },
    
    JBoss {
        @Override
        public Resource getDirResource() {
            String prop = System.getProperty("jboss.server.home.dir");
            return BaseDirUtils.resourceFromVariable(prop, "conf");
        }
    },
    
    Weblogic {
        @Override
        public Resource getDirResource() {
            String env = System.getenv("DOMAIN_HOME");
            return BaseDirUtils.resourceFromVariable(env, "config");
        }
    },
    
    ;
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.spring.resource.BaseDirectory#getDisposition()
     */
    @Override
    public String getDisposition() {
        return "Platform - " + name();
    }
}
