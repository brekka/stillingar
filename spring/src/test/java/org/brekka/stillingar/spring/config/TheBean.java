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

package org.brekka.stillingar.spring.config;

/**
 * A basic bean used in tests.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class TheBean {

    private final String fromConstructor;
    
    private String property;

    /**
     * @param fromConstructor
     * @param property
     */
    public TheBean(String fromConstructor) {
        this.fromConstructor = fromConstructor;
    }
    
    /**
     * @param property the property to set
     */
    public void setProperty(String property) {
        this.property = property;
    }
    
    /**
     * @return the property
     */
    public String getProperty() {
        return property;
    }
    
    /**
     * @return the fromConstructor
     */
    public String getFromConstructor() {
        return fromConstructor;
    }
}
