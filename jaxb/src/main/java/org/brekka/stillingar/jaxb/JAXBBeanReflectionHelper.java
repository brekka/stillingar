/*
 * Copyright 2014 the original author or authors.
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

import java.lang.reflect.Field;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.brekka.stillingar.core.support.BeanReflectionHelper;

/**
 * TODO Description of JAXBBeanReflectionHelper
 *
 * @author Andrew Taylor
 */
public class JAXBBeanReflectionHelper extends BeanReflectionHelper {

    /**
     * @param bean
     */
    public JAXBBeanReflectionHelper(Object bean) {
        super(bean);
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.support.BeanReflectionHelper#acceptClass(java.lang.Class)
     */
    @Override
    protected boolean acceptClass(Class<?> clazz) {
        return clazz.getAnnotation(XmlType.class) != null;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.support.BeanReflectionHelper#acceptField(java.lang.reflect.Field)
     */
    @Override
    protected boolean acceptField(Field field) {
        return field.getAnnotation(XmlElement.class) != null;
    }
}
