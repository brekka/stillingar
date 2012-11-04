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

package org.brekka.stillingar.example;

/**
 * A plain bean 
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class StandardBean {

	private String value;
	
	private Long max;

	public void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
        return value;
    }

    /**
     * @return the max
     */
    public Long getMax() {
        return max;
    }

    /**
     * @param max the max to set
     */
    public void setMax(Long max) {
        this.max = max;
    }
}
