/**
 * 
 */
package org.brekka.stillingar.spring.version;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brekka.stillingar.spring.resource.VersionedResourceNameResolver;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class ApplicationVersionFromMaven implements ApplicationVersionResolver {
    /**
     * The key used to resolve the version within a Maven 'pom.properties' file.
     */
    private static final String POM_PROPS_VERSION = "version";

    /**
     * Location of the 'pom.properties' which can be found on the classpath by specifying the group and artifactId ids
     * to this string.
     */
    private static final String POM_CLASSPATH_FORMAT = "META-INF/maven/%s/%s/pom.properties";

    /**
     * Logger
     */
    private static final Log log = LogFactory.getLog(VersionedResourceNameResolver.class);
    
    
    private final String groupId;
    private final String artifactId;
    private final ClassLoader resolveClassloader;

    /**
     * @param groupId
     * @param artifactId
     * @param resolveClassloader
     */
    public ApplicationVersionFromMaven(String groupId, String artifactId, ClassLoader resolveClassloader) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.resolveClassloader = resolveClassloader;
    }
    
    /**
     * Identify the version. Should only need to be invoked once. 
     * 
     * @return
     */
    public String identifyVersion() {
        String version = null;
        String path = format(POM_CLASSPATH_FORMAT, groupId, artifactId);
        InputStream is = resolveClassloader.getResourceAsStream(path);
        if (is != null) {
            Properties props = new Properties();
            try {
                props.load(is);
                version = props.getProperty(POM_PROPS_VERSION);
            } catch (IOException e) {
                if (log.isWarnEnabled()) {
                    log.warn(format("Failed to load 'pom.properties' from classpath for group '%s', artifact '%s'. " +
                            "No version will be included in the resource file names.", groupId, artifactId));
                }
            } finally {
                closeQuietly(is);
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn(format("Unable to locate 'pom.properties' for group '%s', artifact '%s'. " +
                        "No version will be included in the resource file names.", groupId, artifactId));
            }
        }
        return version;
    }

    /**
     * @param is
     */
    private static void closeQuietly(InputStream is) {
        try {
            is.close();
        } catch (IOException e) { }
    }
}
