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

package org.brekka.stillingar.spring.bpp;

import org.brekka.stillingar.annotations.ConfigurationListener;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Resolves a normal Spring managed bean from a {@link BeanFactory}. This is used by the {@link ConfigurationListener}
 * mechanism to allow the caller to request (most likely prototype) bean instances from the container. Those bean instances
 * might be standard beans (not Stillingar-aware) that are configured using the property placeholder mechanism.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
class BeanReferenceResolver implements ParameterValueResolver {
    /**
     * Bean factory in which to resolve the bean.
     */
    private final BeanFactory beanFactory;
    /**
     * Optional qualifier to single out the bean by name
     */
    private final Qualifier qualifier;
    /**
     * The expected bean value type
     */
    private final Class<?> type;

    
    public BeanReferenceResolver(BeanFactory beanFactory, Qualifier qualifier, Class<?> type) {
        this.beanFactory = beanFactory;
        this.qualifier = qualifier;
        this.type = type;
    }

    public BeanReferenceResolver(BeanFactory beanFactory, Class<?> type) {
        this(beanFactory, null, type);
    }

    /**
     * Perform the lookup of the bean via {@link BeanFactory#getBean(...)}
     */
    public Object getValue() {
        Object value;
        if (qualifier != null) {
            value = beanFactory.getBean(qualifier.value(), type);
        } else {
            value = beanFactory.getBean(type);
        }
        return value;
    }
}