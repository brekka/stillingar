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

/**
 * Represents a fragment of a string containing replacement placeholders. Those placeholders will then be replaced into
 * actual values and combined into a single string.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface Fragment {

    /**
     * Evaluate this fragment into a value, optionally using the configuration source to resolve values from expression
     * within the resolved value itself. Loops should be avoided by ensuring that expressions are unique via the
     * <code>visitedExpressions</code>
     * 
     * @param configurationSource
     *            the source to dynamically lookup variables from
     * @param visitedExpressions
     *            the set of previously encountered expressions.
     * @return the evaluation result.
     */
    String evaluate(ConfigurationSource configurationSource, Set<String> visitedExpressions);
}