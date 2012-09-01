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
