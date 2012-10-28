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

import java.util.List;
import java.util.Set;

import org.brekka.stillingar.api.ConfigurationSource;

/**
 * A fragment that is made up of one or more sub-fragments
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class CompositeFragment implements Fragment {
    /**
     * The list of sub-fragments
     */
    private final List<Fragment> fragments;
    
    /**
     * Does this fragment represent an expression that needs to be evaluated, or is it just a literal value?
     */
    private final boolean evaluate;
    
    /**
     * The parser that will be used to resolve values from the looked-up values.
     */
    private final DefaultPlaceholderParser parser;

    
    /**
     * @param fragments The list of sub-fragments
     * @param evaluate Does this fragment repesent an expression that needs to be evaluated, or is it just a literal value?
     * @param parser The parser that will be used to resolve values from the looked-up values.
     */
    public CompositeFragment(List<Fragment> fragments, boolean evaluate, DefaultPlaceholderParser parser) {
        this.fragments = fragments;
        this.evaluate = evaluate;
        this.parser = parser;
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
            value = ExpressionFragment.evaluate(value, visitedExpressions, parser, configurationSource, true);
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