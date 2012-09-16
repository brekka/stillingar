/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.brekka.stillingar.spring.resource;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.springframework.core.io.Resource;

/**
 * Marker instance for when a resource cannot be resolved. Allows detail about why the resource to be passed down rather
 * than simply null being returned.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class UnresolvableResource implements Resource {

    /**
     * A message detailing why the resource could not resolved.
     */
    private final String message;

    /**
     * @param message
     *            A message detailing why the resource could not resolved.
     * @param args
     *            will be formatted into the message using {@link String#format(String, Object...)}.
     */
    public UnresolvableResource(String message, Object... args) {
        this.message = format(message, args);
    }

    /**
     * Retrieve the detailed message about why this resource was rejected.
     * 
     * @return the message
     */
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
