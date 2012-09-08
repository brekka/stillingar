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

import static java.lang.String.format;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.brekka.stillingar.core.ConfigurationException;
import org.brekka.stillingar.core.ConfigurationSource;
import org.brekka.stillingar.core.GroupChangeListener;

/**
 * 
 * TODO Description of PostUpdateChangeListener
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
class PostUpdateChangeListener implements GroupChangeListener {
	private final Object target;
	final Method method;
	private final List<ValueResolver> argValues;
	
	
	public PostUpdateChangeListener(Object target, Method method, List<ValueResolver> argValues) {
		this.target = target;
		this.method = method;
		this.argValues = argValues;
	}
	
	public void onChange(ConfigurationSource configurationSource) {
        onChange(configurationSource, target);
    }
	
	public void onChange(ConfigurationSource configurationSource, Object target) {
		Object[] args = new Object[argValues.size()];
		for (int i = 0; i < argValues.size(); i++) {
			ValueResolver arg = argValues.get(i);
			args[i] = arg.getValue();
		}
		try {
			if (!method.isAccessible()) {
				method.setAccessible(true);
			}
			method.invoke(target, args);
		} catch (IllegalAccessException e) {
			throwError(args, e);
		} catch (InvocationTargetException e) {
			throwError(args, e);
		}
	}
	protected void throwError(Object[] args, Throwable cause) {
		throw new ConfigurationException(format("Listener method '%s' of type '%s' with arguments %s", 
				method.getName(), method.getDeclaringClass().getName(), Arrays.toString(args)), cause);
	}
}