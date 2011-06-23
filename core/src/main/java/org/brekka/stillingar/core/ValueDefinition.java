/*
 * Copyright 2011 the original author or authors.
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

package org.brekka.stillingar.core;

public final class ValueDefinition<T> {
	
	private final Class<T> type;
	
	private final String expression;
	
	private final ValueChangeListener<T> listener;
	
	private final boolean list;

	public ValueDefinition(Class<T> type, String expression, ValueChangeListener<T> listener, boolean list) {
		this.type = type;
		if (expression != null 
				&& expression.isEmpty()) {
			expression = null;
		}
		this.expression = expression;
		this.listener = listener;
		this.list = list;
	}

	public ValueDefinition(Class<T> type, ValueChangeListener<T> listener, boolean list) {
		this(type, null, listener, list);
	}

	public Class<T> getType() {
		return type;
	}

	public String getExpression() {
		return expression;
	}

	public ValueChangeListener<T> getListener() {
		return listener;
	}
	
	public boolean isList() {
		return list;
	}
}
