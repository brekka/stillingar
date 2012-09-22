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

package org.brekka.stillingar.core;

import java.lang.ref.WeakReference;

/**
 * Used to make change listeners that contain {@link WeakReference}s that can be checked to see whether the target of
 * the listener has been garbage collected or not. This is necessary because the configuration system needs a hard
 * reference to the listener (otherwise it would be quickly GC'd). Thus it becomes the responsibility of the listener
 * implementation to hold only weak references to its target objects.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface Expirable {

    /**
     * Has this instance 'expired' and can be cleared from whatever is referencing it.
     * 
     * @return true if the instance can be cleared.
     */
    boolean isExpired();
}
