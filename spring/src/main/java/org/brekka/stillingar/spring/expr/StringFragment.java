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

package org.brekka.stillingar.spring.expr;

import java.util.Set;

import org.brekka.stillingar.api.ConfigurationSource;

/**
 * A simple string based fragment.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class StringFragment implements Fragment {
    /**
     * The string literal.
     */
    private final String value;

    /**
     * @param value The string literal.
     */
    public StringFragment(String value) {
        this.value = value;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.spring.pc.ExpressionPlaceholderHelper.Fragment#evaluate(java.util.Set)
     */
    @Override
    public String evaluate(ConfigurationSource configurationSource, Set<String> visitedExpressions) {
        return value;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + value + "]";
    }
}
