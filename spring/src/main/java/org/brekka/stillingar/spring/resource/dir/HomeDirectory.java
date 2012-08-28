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
public class HomeDirectory implements BaseDirectory {

    private String path;
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.spring.resource.BaseDirectory#getDirResource()
     */
    @Override
    public Resource getDirResource() {
        String userHome = System.getProperty("user.home");
        File home = new File(userHome);
        if (path != null) {
            home = new File(home, path);
        }
        if (!home.exists()) {
            return new UnresolvableResource("Path '%s' does not exist or is inaccessible", home.getAbsolutePath());
        }
        return new FileSystemResource(home);
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.spring.resource.BaseDirectory#getDisposition()
     */
    @Override
    public String getDisposition() {
        return "Home Directory";
    }
    
    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }
}
