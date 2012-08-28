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
public class EnvironmentVariableDirectory implements BaseDirectory {
    
    private final String var;
    
    /**
     * @param var
     */
    public EnvironmentVariableDirectory(String var) {
        this.var = var;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.spring.resource.BaseDirectory#getDirResource()
     */
    @Override
    public Resource getDirResource() {
        String envVar = System.getenv(var);
        if (envVar == null) {
            return new UnresolvableResource("Not set");
        }
        File dir = new File(envVar);
        // TODO verify dir
        return new FileSystemResource(dir);
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.spring.resource.BaseDirectory#getDisposition()
     */
    @Override
    public String getDisposition() {
        return String.format("Environment Variable '%s'", var);
    }
}
