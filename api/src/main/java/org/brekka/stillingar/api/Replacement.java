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

package org.brekka.stillingar.api;

import java.io.Serializable;

import org.brekka.stillingar.api.annotations.ConfigurationListener;

/**
 * Wraps a value being replaced with the value replacing it, in a single bean. Intended to be used a parameter in
 * {@link ConfigurationListener} to expose the previous value with the @Configure'd value.
 *
 * @author Andrew Taylor
 */
public final class Replacement<T> implements Serializable {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 4223557643361286033L;

    /**
     * The latest value
     */
    private final T latest;

    /**
     * The previous value, if any.
     */
    private final T previous;

    /**
     * @param latest
     * @param previous
     */
    public Replacement(T latest) {
        this(latest, null);
    }

    /**
     * @param latest
     * @param previous
     */
    public Replacement(T latest, T previous) {
        this.latest = latest;
        this.previous = previous;
    }

    /**
     * The latest value. Can be null if the value is set to null.
     * 
     * @return the latest value
     */
    public T getLatest() {
        return latest;
    }

    /**
     * The previous value. 
     * 
     * @return the previous value
     */
    public T getPrevious() {
        return previous;
    }
}
