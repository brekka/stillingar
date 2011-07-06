package org.brekka.stillingar.spring.resource;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.springframework.core.io.Resource;

public class UnresolvableResource implements Resource {

    private final String message;
    
    public UnresolvableResource(String message, Object... args) {
        this.message = format(message, args);
    }
    
    public final String getMessage() {
        return message;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException("Resource unresolvable: " + getMessage());
    }

    @Override
    public boolean exists() {
        throw fail();
    }

    @Override
    public boolean isReadable() {
        throw fail();
    }

    @Override
    public boolean isOpen() {
        throw fail();
    }

    @Override
    public URL getURL() throws IOException {
        throw fail();
    }

    @Override
    public URI getURI() throws IOException {
        throw fail();
    }

    @Override
    public File getFile() throws IOException {
        throw fail();
    }

    @Override
    public long contentLength() throws IOException {
        throw fail();
    }

    @Override
    public long lastModified() throws IOException {
        throw fail();
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        throw fail();
    }

    @Override
    public String getFilename() {
        throw fail();
    }

    @Override
    public String getDescription() {
        throw fail();
    }
    
    private UnsupportedOperationException fail() {
        return new UnsupportedOperationException("Resource unresolvable: " + getMessage());
    }
    
    @Override
    public String toString() {
        return getMessage();
    }

}
