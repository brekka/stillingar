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

/**
 * TODO
 * 
 * @author Andrew Taylor
 */
public class ValueConfigurationException extends ConfigurationException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 4645869513144296360L;

	/**
	 * The value type
	 */
	private final Class<?> type;
	
	/**
	 * Expression used to resolve the value (optional)
	 */
	private final String expression;
	
	/**
	 * The reason for the error
	 */
	private final String reason;
	
	public ValueConfigurationException(String reason, ValueDefinition<?> definition, Throwable cause) {
		super(cause);
		this.type = definition.getType();
		this.expression = definition.getExpression();
		this.reason = reason;
	}
	
	public ValueConfigurationException(String reason, ValueDefinition<?> definition) {
		super();
		this.type = definition.getType();
		this.expression = definition.getExpression();
		this.reason = reason;
	}
	
	public ValueConfigurationException(String reason, Class<?> type, String expression, Throwable cause) {
		super(cause);
		this.type = type;
		this.expression = expression;
		this.reason = reason;
	}

	public ValueConfigurationException(String reason, Class<?> type, String expression) {
		super();
		this.type = type;
		this.expression = expression;
		this.reason = reason;
	}
	
	@Override
	public String getMessage() {
		// TODO Combine instance vars into a message.
		return super.getMessage();
	}

	public Class<?> getType() {
		return type;
	}

	public String getExpression() {
		return expression;
	}

	public String getReason() {
		return reason;
	}
}
