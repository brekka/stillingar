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

package org.brekka.stillingar.spring.pc.expr;

import java.util.List;
import java.util.Set;

import org.brekka.stillingar.core.ConfigurationSource;

/**
 * A fragment that is made up of one or more sub-fragments
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class CompositeFragment implements Fragment {
    private final List<Fragment> fragments;
    private final boolean evaluate;
    private final ExpressionPlaceholderHelper helper;

    public CompositeFragment(List<Fragment> fragments, boolean evaluate, ExpressionPlaceholderHelper helper) {
        this.fragments = fragments;
        this.evaluate = evaluate;
        this.helper = helper;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.stillingar.spring.pc.ExpressionPlaceholderHelper.Fragment#evaluate(java.util.Set)
     */
    @Override
    public String evaluate(ConfigurationSource configurationSource, Set<String> visitedExpressions) {
        StringBuilder sb = new StringBuilder();
        for (Fragment fragment : fragments) {
            String value = fragment.evaluate(configurationSource, visitedExpressions);
            sb.append(value);
        }
        String value = sb.toString();
        if (evaluate) {
            value = ExpressionFragment.evaluate(value, visitedExpressions, helper, configurationSource, true);
        }
        return value;
    }
    
    /**
     * @return the fragments
     */
    public List<Fragment> getFragments() {
        return fragments;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + fragments.toString();
    }
}