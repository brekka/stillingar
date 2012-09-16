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

/**
 * Parses strings that could potentially contain various placeholders that need to be resolved to actual values.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface PlaceholderParser {

    /**
     * Parse the specified string into a {@link Fragment} that can be subsequently evaluated to produce a value.
     * 
     * @param stringToParse
     *            the value to parse which can consist of multiple concatenated placeholders that can themselves contain
     *            nested placeholders.
     * @return the fragment
     */
    Fragment parse(String stringToParse);

}