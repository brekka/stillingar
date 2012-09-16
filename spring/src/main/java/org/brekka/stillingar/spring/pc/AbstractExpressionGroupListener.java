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

package org.brekka.stillingar.spring.pc;

import java.util.HashSet;

import org.brekka.stillingar.core.ConfigurationSource;
import org.brekka.stillingar.core.GroupChangeListener;
import org.brekka.stillingar.spring.expr.Fragment;

/**
 * Evaluates the fragment and passes the result to the onChange method.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public abstract class AbstractExpressionGroupListener implements GroupChangeListener {

    /**
     * The fragment that will be evaluated
     */
    private final Fragment fragment;
    
    /**
     * @param fragment The fragment that will be evaluated
     * @param helper The helper that will be used to evaluate the fragment.
     */
    public AbstractExpressionGroupListener(Fragment fragment) {
        if (fragment == null) {
            throw new IllegalArgumentException("A fragment must be specified");
        }
        this.fragment = fragment;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.GroupChangeListener#onChange(org.brekka.stillingar.core.ConfigurationSource)
     */
    @Override
    public void onChange(ConfigurationSource configurationSource) {
        String value = fragment.evaluate(configurationSource, new HashSet<String>());
        onChange(value);
    }

    protected abstract void onChange(String newValue);
}
