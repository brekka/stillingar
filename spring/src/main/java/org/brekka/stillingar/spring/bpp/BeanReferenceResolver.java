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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * TODO Description of BeanReferenceResolver
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
class BeanReferenceResolver implements ValueResolver {
    private final BeanFactory beanFactory;
	private final Qualifier qualifier;
	private final Class<?> type;
	
	public BeanReferenceResolver(BeanFactory beanFactory, Qualifier qualifier, Class<?> type) {
	    this.beanFactory = beanFactory;
		this.qualifier = qualifier;
		this.type = type;
	}

	public BeanReferenceResolver(BeanFactory beanFactory, Class<?> type) {
		this(beanFactory, null, type);
	}

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