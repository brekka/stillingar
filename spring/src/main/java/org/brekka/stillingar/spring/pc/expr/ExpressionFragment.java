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

import java.util.Set;

import org.brekka.stillingar.core.ConfigurationSource;
import org.brekka.stillingar.core.ValueChangeListener;
import org.brekka.stillingar.core.ValueDefinition;

/**
 * An expression based fragment. Will normally get its updates via the {@link ValueChangeListener#onChange(Object)}
 * call.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class ExpressionFragment implements Fragment, ValueChangeListener<String> {
    private final String expression;
    private final ExpressionPlaceholderHelper helper;
    private String value;
    private boolean changed;

    public ExpressionFragment(String expression, ExpressionPlaceholderHelper helper) {
        this.expression = expression;
        this.helper = helper;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.ValueChangeListener#onChange(java.lang.Object)
     */
    @Override
    public void onChange(String newValue) {
        this.value = newValue;
        this.changed = true;
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
        return evaluate(theValue, visitedExpressions, helper, configurationSource, false);
    }

    /**
     * Prepare a value defination from this expression fragments
     * @return
     */
    public ValueDefinition<String> toValueDefinition() {
        return new ValueDefinition<String>(String.class, expression, this, false);
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
    
    static String evaluate(String value, Set<String> visitedExpressions, 
            ExpressionPlaceholderHelper helper, ConfigurationSource configurationSource,
            boolean inExpression) {
        String retVal;
        if (visitedExpressions.add(value)) {
            Fragment valueFragment = helper.parse(value, 1, inExpression);
            retVal = valueFragment.evaluate(configurationSource, visitedExpressions);
            visitedExpressions.remove(value);
        } else {
            throw new IllegalArgumentException(String.format(
                    "Circular reference detected while resolving '%s'", value));
        }
        return retVal;
    }
}
