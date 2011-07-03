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

import static java.lang.String.format;

/**
 * Indicates an error while updating a field/method reference to its new value.
 * 
 * @author Andrew Taylor
 */
public class ReferentUpdateException extends ConfigurationException {

	/**
	 * SerialUID
	 */
	private static final long serialVersionUID = -6253150293370045665L;

	/**
	 * The type of reference, e.g. field or setter method.
	 */
	private final String referentType;
	
	/**
	 * The name of the field/method
	 */
	private final String name;
	
	/**
	 * The type of value being set
	 */
	private final Class<?> valueType;
	
	/**
	 * The type the field/method is expecting
	 */
	private final Class<?> expectedType;
	
	/**
	 * Is this a list or not
	 */
	private final boolean list;
    
	/**
	 * The type of the bean on which the method/field is being set.
	 */
	private final Class<?> targetType;
	
	/**
	 * 
	 * @param referentType The type of reference, e.g. field or setter method.
	 * @param name The name of the field/method
	 * @param valueType The type of value being set
	 * @param expectedType The type the field/method is expecting
	 * @param list Is this a list or not
	 * @param targetType The type of the bean on which the method/field is being set.
	 * @param cause the reason for this exception being thrown.
	 */
	public ReferentUpdateException(String referentType, String name, Class<?> valueType, Class<?> expectedType,
			boolean list, Class<?> targetType, Throwable cause) {
		super(cause);
		this.referentType = referentType;
		this.name = name;
		this.valueType = valueType;
		this.expectedType = expectedType;
		this.list = list;
		this.targetType = targetType;
	}
	
	@Override
	public String getMessage() {
	    String multiplicity = "Single";
	    if (list) {
	        multiplicity = "List";
	    }
	    String message = format("%s '%s' of %s (%s): expected '%s', actual '%s'", 
	            referentType, name, targetType.getName(), multiplicity, expectedType.getName(), valueType.getName());
	    return message;
	}

	/**
	 * The name of the field/method
	 * @return
	 */
    public String getName() {
        return name;
    }

    /**
     * The type of value being set
     * @return
     */
    public Class<?> getValueType() {
        return valueType;
    }

    /**
     * The type the field/method is expecting
     * @return
     */
    public Class<?> getExpectedType() {
        return expectedType;
    }

    /**
     * Is this a list or not
     * @return
     */
    public boolean isList() {
        return list;
    }

    /**
     * The type of the bean on which the method/field is being set.
     * @return
     */
    public Class<?> getTargetType() {
        return targetType;
    }
}
