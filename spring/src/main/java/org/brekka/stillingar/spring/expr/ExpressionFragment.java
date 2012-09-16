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

import org.brekka.stillingar.core.ConfigurationSource;
import org.brekka.stillingar.core.ValueChangeListener;

/**
 * An expression based fragment that may receive value changes from an external agent calling {@link #setValue(String)},
 * or by evaluating the expression directly in the {@link #evaluate(ConfigurationSource, Set)} method.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class ExpressionFragment implements Fragment {
    /**
     * The expression that will be used to listen for changes.
     */
    private final String expression;

    /**
     * The parser that will be used to resolve values from the looked-up values.
     */
    private final DefaultPlaceholderParser parser;

    /**
     * The value captured when the {@link ValueChangeListener#onChange(Object)} method is called
     */
    private String value;

    /**
     * Determines whether the {@link ValueChangeListener#onChange(Object)} method has been called yet.
     */
    private boolean changed;

    /**
     * @param expression
     *            The expression that will be used to listen for changes.
     * @param parser
     *            The parser that will be used to resolve values from the looked-up values.
     */
    public ExpressionFragment(String expression, DefaultPlaceholderParser parser) {
        this.expression = expression;
        this.parser = parser;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(String value) {
        this.value = value;
        this.changed = true;
    }

    /**
     * @return the expression
     */
    public String getExpression() {
        return expression;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.spring.pc.ExpressionPlaceholderHelper.Fragment#evaluate(java.util.Set)
     */
    @Override
    public String evaluate(ConfigurationSource configurationSource, Set<String> visitedExpressions) {
        String theValue = value;
        if (!this.changed) {
            theValue = configurationSource.retrieve(expression, String.class);
        }
        return evaluate(theValue, visitedExpressions, parser, configurationSource, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + expression + "]";
    }

    /**
     * Helper method to evaluate the expression and any resolved expression.
     * 
     * @param value
     * @param visitedExpressions
     * @param helper
     * @param configurationSource
     * @param inExpression
     * @return
     */
    static String evaluate(String value, Set<String> visitedExpressions, DefaultPlaceholderParser helper,
            ConfigurationSource configurationSource, boolean inExpression) {
        String retVal;
        if (visitedExpressions.add(value)) {
            Fragment valueFragment = helper.parse(value, 1, inExpression);
            retVal = valueFragment.evaluate(configurationSource, visitedExpressions);
            visitedExpressions.remove(value);
        } else {
            throw new IllegalArgumentException(String.format("Circular reference detected while resolving '%s'", value));
        }
        return retVal;
    }
}
