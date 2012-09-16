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
 * A series of well-defined configuration directories for various platforms.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public enum PlatformDirectory implements BaseDirectory {

    /**
     * Resolves the 'conf' directory within Apache Tomcat, identified via the 'catalina.base' system property.
     */
    Tomcat {
        @Override
        public Resource getDirResource() {
            String prop = System.getProperty("catalina.base");
            return BaseDirUtils.resourceFromVariable(prop, "conf");
        }
    },

    /**
     * Resolves the 'config' directory within a Glassfish domain, where the domain is identified via the
     * 'com.sun.aas.instanceRoot' system property.
     */
    Glassfish {
        @Override
        public Resource getDirResource() {
            String prop = System.getProperty("com.sun.aas.instanceRoot");
            return BaseDirUtils.resourceFromVariable(prop, "config");
        }
    },

    /**
     * Resolves the 'conf' directory within a JBoss server, where the server is identified via the
     * 'jboss.server.home.dir' system property.
     */
    JBoss {
        @Override
        public Resource getDirResource() {
            String prop = System.getProperty("jboss.server.home.dir");
            return BaseDirUtils.resourceFromVariable(prop, "conf");
        }
    },

    /**
     * Resolves the 'config' directory within a WebLogic domain, where the domain is identified via the
     * 'DOMAIN_HOME' environment variable.
     */
    Weblogic {
        @Override
        public Resource getDirResource() {
            String env = System.getenv("DOMAIN_HOME");
            return BaseDirUtils.resourceFromVariable(env, "config");
        }
    },

    ;

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.spring.resource.BaseDirectory#getDisposition()
     */
    @Override
    public String getDisposition() {
        return "Platform - " + name();
    }
}
