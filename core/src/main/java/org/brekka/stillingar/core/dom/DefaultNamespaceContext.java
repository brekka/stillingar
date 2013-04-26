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

package org.brekka.stillingar.core.dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

/**
 * Since Java does not provide a simple implementation of {@link NamespaceContext}, here is one.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class DefaultNamespaceContext implements NamespaceContext, NamespaceAware {

    private final Map<String, String> prefixToNamespace;
    
    private final Map<String, List<String>> namespaceToPrefix;
    
    public DefaultNamespaceContext(String... prefixToNamespacePairs) {
        this.prefixToNamespace = new HashMap<String, String>();
        this.namespaceToPrefix = new HashMap<String, List<String>>();
        for (int i = 0; i < prefixToNamespacePairs.length; i+= 2) {
            String prefix = prefixToNamespacePairs[i];
            String namespace = prefixToNamespacePairs[i + 1];
            if (!this.namespaceToPrefix.containsKey(namespace)) {
                this.namespaceToPrefix.put(namespace, new ArrayList<String>());
            }
            this.namespaceToPrefix.get(namespace).add(prefix);
            this.prefixToNamespace.put(prefix, namespace);
        }
    }
    
    /* (non-Javadoc)
     * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
     */
    @Override
    public String getNamespaceURI(String prefix) {
        return prefixToNamespace.get(prefix);
    }

    /* (non-Javadoc)
     * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
     */
    @Override
    public String getPrefix(String namespaceURI) {
        List<String> list = namespaceToPrefix.get(namespaceURI);
        if (list == null 
                || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.dom.NamespaceAware#registerNamespace(java.lang.String, java.lang.String)
     */
    @Override
    public synchronized void registerNamespace(String prefix, String uri) {
        if (prefix == null) {
            throw new IllegalArgumentException("A prefix must be provided");
        }
        if (uri == null) {
            throw new IllegalArgumentException("A uri must be set");
        }
        String existingNamespaceForPrefix = this.prefixToNamespace.get(prefix);
        if (existingNamespaceForPrefix != null) {
            if (existingNamespaceForPrefix.equals(uri)) {
                // Already registered
                return;
            }
            throw new IllegalArgumentException(String.format("Cannot assign namespace URI '%s' to prefix '%s', " +
            		"that prefix is already assigned to '%s'", uri, prefix, existingNamespaceForPrefix));
        }
        // Not yet assigned
        this.prefixToNamespace.put(prefix, uri);
        if (!this.namespaceToPrefix.containsKey(uri)) {
            this.namespaceToPrefix.put(uri, new ArrayList<String>());
        }
        this.namespaceToPrefix.get(uri).add(prefix);
    }

    /* (non-Javadoc)
     * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Iterator getPrefixes(String namespaceURI) {
        List<String> list = namespaceToPrefix.get(namespaceURI);
        if (list == null 
                || list.isEmpty()) {
            return null;
        }
        return list.iterator();
    }
    
    /**
     * Retrieve the available prefixes
     * @return
     */
    public Iterable<String> getPrefixes() {
        return prefixToNamespace.keySet();
    }
    
    /**
     * Does this context currently contain any namespaces.
     * @return true if there are more than one namespaces registered.
     */
    public boolean hasNamespaces() {
        return !prefixToNamespace.isEmpty();
    }
}
