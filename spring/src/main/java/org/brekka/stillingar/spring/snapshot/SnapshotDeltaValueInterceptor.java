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

package org.brekka.stillingar.spring.snapshot;

import org.brekka.stillingar.core.delta.DeltaValueInterceptor;
import org.springframework.context.Lifecycle;

/**
 * Delta value interceptor that looks for value beans implementing {@link Lifecycle}, calling start/stop if it
 * encounters such beans.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class SnapshotDeltaValueInterceptor implements DeltaValueInterceptor {

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.delta.DeltaValueInterceptor#created(java.lang.Object)
     */
    @Override
    public <T> T created(T value) {
        if (value instanceof Lifecycle) {
            Lifecycle lifecycle = (Lifecycle) value;
            System.out.println("Starting: " + value);
            lifecycle.start();
        }
        return value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.delta.DeltaValueInterceptor#released(java.lang.Object)
     */
    @Override
    public void released(Object value) {
        if (value instanceof Lifecycle) {
            Lifecycle lifecycle = (Lifecycle) value;
            lifecycle.stop();
            System.out.println("Stopped: " + value);
        }
    }
}
