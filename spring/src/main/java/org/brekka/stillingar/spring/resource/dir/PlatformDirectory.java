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
        /* (non-Javadoc)
         * @see org.brekka.stillingar.spring.resource.BaseDirectory#getDirResource()
         */
        @Override
        public Resource getDirResource() {
            // TODO Auto-generated method stub
            return null;
        }
    },
    
    Glassfish {
        /* (non-Javadoc)
         * @see org.brekka.stillingar.spring.resource.BaseDirectory#getDirResource()
         */
        @Override
        public Resource getDirResource() {
            // TODO Auto-generated method stub
            return null;
        }
    },
    
    JBoss {
        /* (non-Javadoc)
         * @see org.brekka.stillingar.spring.resource.BaseDirectory#getDirResource()
         */
        @Override
        public Resource getDirResource() {
            // TODO Auto-generated method stub
            return null;
        }
    },
    
    Weblogic {
        /* (non-Javadoc)
         * @see org.brekka.stillingar.spring.resource.BaseDirectory#getDirResource()
         */
        @Override
        public Resource getDirResource() {
            // TODO Auto-generated method stub
            return null;
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
