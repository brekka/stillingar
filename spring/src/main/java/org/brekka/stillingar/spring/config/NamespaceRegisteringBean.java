/*
 * Copyright 2013 the original author or authors.
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

package org.brekka.stillingar.spring.config;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brekka.stillingar.core.dom.NamespaceAware;

/**
 * A simple bean that registers the specified namespace uri and prefix with the NamespaceAware.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class NamespaceRegisteringBean {

    private static final Log log = LogFactory.getLog(NamespaceRegisteringBean.class);

    private final NamespaceAware namespaceAware;
    private Map<String, String> namespaceToPrefixMap;

    public NamespaceRegisteringBean(final NamespaceAware namespaceAware, final String uri, final String prefix) {
        this(namespaceAware, Collections.singletonMap(uri, prefix));
    }

    public NamespaceRegisteringBean(final NamespaceAware namespaceAware, final Map<String, String> namespaceToPrefixMap) {
        this.namespaceAware = namespaceAware;
        this.namespaceToPrefixMap = namespaceToPrefixMap == null ? Collections.<String, String>emptyMap() : namespaceToPrefixMap;
    }

    public void registerNamespaces() {
        Set<Entry<String,String>> entrySet = namespaceToPrefixMap.entrySet();
        for (Entry<String, String> entry : entrySet) {
            if (log.isInfoEnabled()) {
                log.info(String.format("Registering prefix '%s' to namespace '%s'", entry.getValue(), entry.getKey()));
            }
            namespaceAware.registerNamespace(entry.getValue(), entry.getKey());
        }
    }
}
