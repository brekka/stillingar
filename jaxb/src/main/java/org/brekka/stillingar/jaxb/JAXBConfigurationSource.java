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

import java.util.List;

import org.brekka.stillingar.core.ConfigurationSource;

/**
 * TODO Description of JAXBConfigurationSource
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class JAXBConfigurationSource implements ConfigurationSource {

    private final Object object;
    
    /**
     * @param object
     */
    public JAXBConfigurationSource(Object object) {
        this.object = object;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ConfigurationSource#isAvailable(java.lang.String)
     */
    @Override
    public boolean isAvailable(String expression) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ConfigurationSource#isAvailable(java.lang.Class)
     */
    @Override
    public boolean isAvailable(Class<?> valueType) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieve(java.lang.String, java.lang.Class)
     */
    @Override
    public <T> T retrieve(String expression, Class<T> valueType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieve(java.lang.Class)
     */
    @Override
    public <T> T retrieve(Class<T> valueType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieveList(java.lang.String, java.lang.Class)
     */
    @Override
    public <T> List<T> retrieveList(String expression, Class<T> valueType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ConfigurationSource#retrieveList(java.lang.Class)
     */
    @Override
    public <T> List<T> retrieveList(Class<T> valueType) {
        // TODO Auto-generated method stub
        return null;
    }

}
