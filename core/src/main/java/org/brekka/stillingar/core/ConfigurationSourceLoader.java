/**
 * 
 */
package org.brekka.stillingar.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Create a configuration source from the specified stream. The format of the data within the stream should match
 * that expected by the implementation otherwise an exception will be thrown.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface ConfigurationSourceLoader {

    /**
     * Create a configuration source from the specified input stream. It is the callers responsibility to close
     * the stream when control is returned.
     * 
     * @param sourceStream the stream to read from, which must not be null.
     * @param encoding the encoding of the stream, if known (can be null).
     * @return the configuration source based on <code>sourceStream</code> - never null.
     * @throws ConfigurationException if the parser is unable to extract the content it needs from the stream.
     * @throws IOException if an IO problem occurs while reading from the stream.
     */
    ConfigurationSource parse(InputStream sourceStream, Charset encoding) throws ConfigurationException, IOException;
}
