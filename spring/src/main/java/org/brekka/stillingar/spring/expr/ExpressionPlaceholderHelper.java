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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.brekka.stillingar.core.ConfigurationException;
import org.springframework.util.PropertyPlaceholderHelper;

/**
 * Helper class that parses placeholders that can contain expressions and other placeholders. Can also resolve
 * placeholders that are the result of evaluating existing placeholders.
 * 
 * Based partly on {@link PropertyPlaceholderHelper} by Juergen Hoeller and Rob Harrop.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class ExpressionPlaceholderHelper {
    /**
     * A mapping of different types of opening bracket based on their corresponding end-bracket.
     */
    private static final Map<String, String> WELL_KNOWN_SIMPLE_PREFIXES = new HashMap<String, String>(4);

    static {
        WELL_KNOWN_SIMPLE_PREFIXES.put("}", "{");
        WELL_KNOWN_SIMPLE_PREFIXES.put("]", "[");
        WELL_KNOWN_SIMPLE_PREFIXES.put(")", "(");
    }

    /**
     * The string prefix that identifies a placeholder.
     */
    private final String placeholderPrefix;

    /**
     * The string suffix the identifies the end of a placeholder.
     */
    private final String placeholderSuffix;

    /**
     * The string prefix that identifies a nested placeholder.
     */
    private final String nestedPrefix;

    /**
     * The opening bracket symbol that will be used to identify and skip nested placeholders and well-balanced
     * expressions using the same symbol notation.
     */
    private final String placeholderPrefixSymbol;

    /**
     * @param placeholderPrefix
     *            The string prefix that identifies a placeholder.
     * @param placeholderSuffix
     *            The string suffix the identifies the end of a placeholder.
     */
    public ExpressionPlaceholderHelper(String placeholderPrefix, String placeholderSuffix) {
        this.placeholderPrefix = placeholderPrefix;
        this.placeholderSuffix = placeholderSuffix;
        String simplePrefixForSuffix = WELL_KNOWN_SIMPLE_PREFIXES.get(this.placeholderSuffix);
        if (simplePrefixForSuffix != null && this.placeholderPrefix.endsWith(simplePrefixForSuffix)) {
            this.placeholderPrefixSymbol = simplePrefixForSuffix;
        } else {
            this.placeholderPrefixSymbol = this.placeholderPrefix;
        }
        if (placeholderPrefix.length() > 1) {
            this.nestedPrefix = placeholderPrefix.substring(0, 1) + placeholderPrefixSymbol;
        } else {
            this.nestedPrefix = placeholderPrefixSymbol;
        }
    }

    /**
     * Parse the specified string into a {@link Fragment} that can be subsequently evaluated to produce a value.
     * 
     * @param strVal
     *            the value to parse which can consist of multiple concatenated placeholders that can themselves contain
     *            nested placeholders.
     * @return the fragment
     */
    public Fragment parse(String strVal) {
        if (strVal.contains(placeholderPrefix)) {
            return parse(strVal, 0, false);
        }
        return new StringFragment(strVal);
    }

    /**
     * The actual algorithm for extracting placeholders and nested placeholders. It is based on substring logic rather
     * than iterating the underlying char array, so should be safe to use with UTF-8 characters consisting of more than
     * two bytes.
     * 
     * @param strVal
     *            the string value to parse
     * @param depth
     *            the current nested placeholder depth (0 = top). Used to determine whether we should be looking for the
     *            outermost placeholder prefix or the nested placeholder prefix.
     * @param inExpression
     *            are we currently within an expression?
     * @return the {@link Fragment} 'tree' that represent the input string.
     */
    Fragment parse(String strVal, int depth, boolean inExpression) {
        List<Fragment> fragments = new ArrayList<Fragment>();

        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < strVal.length();) {
            String sub = strVal.substring(i, strVal.length());
            int prefixSkip = -1;

            if (depth == 0 && sub.startsWith(placeholderPrefix)) {
                prefixSkip = placeholderPrefix.length();
            } else if (depth > 0 && sub.startsWith(nestedPrefix)) {
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
     * Find the index of the closing bracket with the sub string. If nested brackets are encountered, then they are
     * counted in order to find the correct closing bracket.
     * 
     * @param sub
     *            the substring to search.
     * @return the index of the closing bracket
     */
    private int findClosing(String sub) {
        int depth = 1;
        for (int i = 0; i < sub.length(); i++) {
            String character = sub.substring(i, i + 1);
            if (character.equals(placeholderPrefixSymbol)) {
                depth++;
            } else if (character.equals(placeholderSuffix)) {
                depth--;
            }
            if (depth == 0) {
                return i;
            }
        }
        throw new ConfigurationException(String.format("Failed to locate closing bracket '%s' in expression fragment "
                + "'%s' (reached depth %d)", placeholderSuffix, sub, depth));
    }
    

    /**
     * Extract all {@link ExpressionFragment}s from the given fragment and return them
     * 
     * @param fragment
     *            the fragment to extract {@link ExpressionFragment}s from.
     * @return the list of expression fragments (never null).
     */
    public static List<ExpressionFragment> findExpressionFragments(Fragment fragment) {
        List<ExpressionFragment> valueDefs = new ArrayList<ExpressionFragment>();
        collectExpressionFragments(fragment, valueDefs);
        return valueDefs;
    }

    private static void collectExpressionFragments(Fragment fragment, List<ExpressionFragment> values) {
        if (fragment instanceof CompositeFragment) {
            CompositeFragment composite = (CompositeFragment) fragment;
            List<Fragment> fragments = composite.getFragments();
            for (Fragment f : fragments) {
                collectExpressionFragments(f, values);
            }
        } else if (fragment instanceof ExpressionFragment) {
            ExpressionFragment expressionFragment = (ExpressionFragment) fragment;
            values.add(expressionFragment);
        }
    }

}
