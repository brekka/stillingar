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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.brekka.stillingar.core.ConfigurationSource;
import org.brekka.stillingar.core.ValueDefinition;
import org.springframework.util.PropertyPlaceholderHelper;

/**
 * TODO Description of ExpressionPlaceholderHelper
 * 
 * Based partly on {@link PropertyPlaceholderHelper} by Juergen Hoeller and Rob Harrop.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class ExpressionPlaceholderHelper {
    private static final Map<String, String> wellKnownSimplePrefixes = new HashMap<String, String>(4);

    static {
        wellKnownSimplePrefixes.put("}", "{");
        wellKnownSimplePrefixes.put("]", "[");
        wellKnownSimplePrefixes.put(")", "(");
    }

    private final String placeholderPrefix;

    private final String placeholderSuffix;

    private final String nestedPrefix;
    
    private final String prefixCharacter;

    /**
     * @param placeholderPrefix
     * @param placeholderSuffix
     */
    public ExpressionPlaceholderHelper(String placeholderPrefix, String placeholderSuffix) {
        this.placeholderPrefix = placeholderPrefix;
        this.placeholderSuffix = placeholderSuffix;
        String simplePrefixForSuffix = wellKnownSimplePrefixes.get(this.placeholderSuffix);
        if (simplePrefixForSuffix != null && this.placeholderPrefix.endsWith(simplePrefixForSuffix)) {
            this.prefixCharacter = simplePrefixForSuffix;
        } else {
            this.prefixCharacter = this.placeholderPrefix;
        }
        if (placeholderPrefix.length() > 1) {
            this.nestedPrefix = placeholderPrefix.substring(0, 1) + prefixCharacter;
        } else {
            this.nestedPrefix = prefixCharacter;
        }
    }
    
    public Fragment parse(String strVal) {
        if (strVal.contains(placeholderPrefix)) {
            return parse(strVal, 0, false);
        }
        return new StringFragment(strVal);
    }

    /**
     * @param fragment
     * @return
     */
    public List<ValueDefinition<?>> toValueDefinitions(Fragment fragment) {
        List<ValueDefinition<?>> valueDefs = new ArrayList<ValueDefinition<?>>();
        collectValueDefinitions(fragment, valueDefs);
        return valueDefs;
    }
    
    private void collectValueDefinitions(Fragment fragment, List<ValueDefinition<?>> values) {
        if (fragment instanceof CompositeFragment) {
            CompositeFragment composite = (CompositeFragment) fragment;
            List<Fragment> fragments = composite.getFragments();
            for (Fragment f : fragments) {
                collectValueDefinitions(f, values);
            }
        } else if (fragment instanceof ExpressionFragment) {
            ExpressionFragment expressionFragment = (ExpressionFragment) fragment;
            values.add(expressionFragment.toValueDefinition());
        }
    }

    
    Fragment parse(String strVal, int depth, boolean inExpression) {
        List<Fragment> fragments = new ArrayList<Fragment>();
        
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < strVal.length();) {
            String sub = strVal.substring(i, strVal.length());
            int prefixSkip = -1;
            
            if (depth == 0
                    && sub.startsWith(placeholderPrefix)) {
                prefixSkip = placeholderPrefix.length();
            } else if (sub.startsWith(nestedPrefix)) {
                prefixSkip = nestedPrefix.length();
            }
            
            if (prefixSkip != -1) {
                if (buf.length() > 0) {
                    // We have regular string data
                    fragments.add(new StringFragment(buf.toString()));
                    buf = new StringBuilder();
                }
                
                int closingIndex = findClosing(sub.substring(prefixSkip)) + prefixSkip;
                String internal = sub.substring(prefixSkip, closingIndex);
                Fragment parsed = parse(internal, depth + 1, true);
                fragments.add(parsed);
                i = i + closingIndex + 1;
            } else {
                // Add regular text character.
                buf.append(strVal.substring(i, i + 1));
                i++;
            }
        }
        
        if (fragments.isEmpty()) {
            if (inExpression) {
                return new ExpressionFragment(buf.toString(), this);
            }
            return new StringFragment(buf.toString());
        }
        if (buf.length() > 0) {
            fragments.add(new StringFragment(buf.toString()));
        }
        return new CompositeFragment(fragments, inExpression, this);
    }
    
    /**
     * @param sub
     * @return
     */
    private int findClosing(String sub) {
        int depth = 1;
        for (int i = 0; i < sub.length(); i++) {
            String character = sub.substring(i, i + 1);
            if (character.equals(prefixCharacter)) {
                depth++;
            } else if (character.equals(placeholderSuffix)) {
                depth--;
            }
            if (depth == 0) {
                return i;
            }
        }
        // TODO better message
        throw new IllegalStateException("No closing token found");
    }

    /**
     * @param fragment
     * @param configurationSource
     * @return
     */
    public static String evaluate(Fragment fragment, ConfigurationSource configurationSource) {
        String value;
        if (fragment instanceof CompositeFragment) {
            CompositeFragment composite = (CompositeFragment) fragment;
            List<Fragment> fragments = composite.getFragments();
            // Each top level fragment needs its own 'visitedExpressions' set.
            StringBuilder sb = new StringBuilder();
            for (Fragment f : fragments) {
                sb.append(f.evaluate(configurationSource, new HashSet<String>()));
            }
            value = sb.toString();
        } else if (fragment instanceof ExpressionFragment) {
            ExpressionFragment expressionFragment = (ExpressionFragment) fragment;
            value = expressionFragment.evaluate(configurationSource, new HashSet<String>());
        } else {
            throw new IllegalStateException(String.format(
                    "Unable to handle change to fragment type '%s'", fragment.getClass().getName()));
        }
        return value;
    }
}
