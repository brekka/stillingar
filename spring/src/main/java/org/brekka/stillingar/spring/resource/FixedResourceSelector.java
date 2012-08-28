/**
 * 
 */
package org.brekka.stillingar.spring.resource;

import org.springframework.core.io.Resource;

/**
 * Retrieve a resource from a fixed path. Not very flexible, but some situations might call for it.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class FixedResourceSelector implements ResourceSelector {

    /**
     * The resource to return
     */
    private final Resource resource;
    
    
    public FixedResourceSelector(Resource resource) {
        this.resource = resource;
    }


    /* (non-Javadoc)
     * @see org.brekka.stillingar.spring.resource.ResourceSelector#locateResources()
     */
    @Override
    public Resource getResource() {
        return resource;
    }

}
