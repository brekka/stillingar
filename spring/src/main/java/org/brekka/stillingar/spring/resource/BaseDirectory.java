/**
 * 
 */
package org.brekka.stillingar.spring.resource;

import org.springframework.core.io.Resource;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface BaseDirectory {
    Resource getDirResource();
    
    String getDisposition();
}
