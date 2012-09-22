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

package org.brekka.stillingar.core.delta;

/**
 * Provides a means for an external party to hook into the value creation and release mechanism. Every value created by
 * the configuration source will be passed to the {@link #created(Object)} method where the object can be manipulated
 * and then returned (or a substitute sent in its place). When values are released either as a result of a value being
 * updated or the source being shutdown, the {@link #released(Object)} method will be called.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface DeltaValueInterceptor {

    /**
     * Carry out some processing on the newly created value. The implementation should either return <code>value</code>,
     * or a different instance of the same type. This method will be invoked before the value is actually assigned
     * to its referent.
     * 
     * @param value
     *            the value to act on
     * @return the value or a substitute.
     */
    <T> T created(T value);

    /**
     * Carry out some clean up on a value that has been released. 
     * 
     * @param value the value
     */
    void released(Object value);
}
