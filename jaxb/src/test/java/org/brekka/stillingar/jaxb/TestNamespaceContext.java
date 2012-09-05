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

package org.brekka.stillingar.jaxb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;

/**
 * Since Java does not provide a simple implementation of {@link NamespaceContext}, here is one.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class TestNamespaceContext implements NamespaceContext {

    private final Map<String, String> prefixToNamespace;
    
    private final Map<String, List<String>> namespaceToPrefix;
    
    public TestNamespaceContext(Map<String, String> nsMap) {
        this.prefixToNamespace = new HashMap<String, String>(nsMap);
        this.namespaceToPrefix = new HashMap<String, List<String>>();
        Set<Entry<String,String>> entrySet = nsMap.entrySet();
        for (Entry<String, String> entry : entrySet) {
            if (!this.namespaceToPrefix.containsKey(entry.getValue())) {
                this.namespaceToPrefix.put(entry.getValue(), new ArrayList<String>());
            }
            this.namespaceToPrefix.get(entry.getValue()).add(entry.getKey());
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

}
