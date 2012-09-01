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
public class SystemPropertyDirectory implements BaseDirectory {

    private final String property;
    
    
    /**
     * @param property
     */
    public SystemPropertyDirectory(String property) {
        this.property = property;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.spring.resource.BaseDirectory#getDirResource()
     */
    @Override
    public Resource getDirResource() {
        String val = System.getProperty(property);
        return BaseDirUtils.resourceFromVariable(val, null);
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.spring.resource.BaseDirectory#getDisposition()
     */
    @Override
    public String getDisposition() {
        return String.format("System Property '%s'", property);
    }

}
