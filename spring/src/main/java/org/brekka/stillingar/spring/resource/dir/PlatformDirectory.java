/**
 * 
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
