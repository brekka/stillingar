/**
 * 
 */
package org.brekka.stillingar.spring.resource.dir;

import java.io.File;

import org.brekka.stillingar.spring.resource.BaseDirectory;
import org.brekka.stillingar.spring.resource.UnresolvableResource;
import org.springframework.core.io.FileSystemResource;
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
        if (val == null) {
            return new UnresolvableResource("Not set");
        }
        File dir = new File(val);
        // TODO verify dir
        return new FileSystemResource(dir);
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.spring.resource.BaseDirectory#getDisposition()
     */
    @Override
    public String getDisposition() {
        return String.format("System Property '%s'", property);
    }

}
