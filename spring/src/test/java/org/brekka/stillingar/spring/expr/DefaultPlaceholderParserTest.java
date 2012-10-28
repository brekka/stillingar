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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.brekka.stillingar.api.ConfigurationSource;
import org.junit.Test;

/**
 * Test of ExpressionPlaceholderHelper
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class DefaultPlaceholderParserTest {

    /**
     * Test method for {@link org.brekka.stillingar.spring.expr.DefaultPlaceholderParser#parse(java.lang.String)}.
     */
    @Test
    public void testParse() {
        PlaceholderParser helper = new DefaultPlaceholderParser("$config{", "}");
        Fragment fragment = helper.parse("$config{abc.${test1.${val2}.a}}.other.$config{conf2}");
        Map<String, String> map = new HashMap<String, String>();
        map.put("val2", "bob");
        map.put("test1.bob.a", "${internal}thing");
        map.put("internal", "some");
        map.put("abc.something", "part1");
        map.put("conf2", "part2");
        MappedSource source = new MappedSource(map);
        
        List<ExpressionFragment> expressionFragments = DefaultPlaceholderParser.findExpressionFragments(fragment);
        for (ExpressionFragment expressionFragment : expressionFragments) {
            expressionFragment.setValue(map.get(expressionFragment.getExpression()));
        }
        
        String value = fragment.evaluate(source, new HashSet<String>());
        assertEquals("part1.other.part2", value);
    }

    
    private static class MappedSource implements ConfigurationSource {
        
        private final Map<String, String> map;
        
        /**
         * @param map
         */
        public MappedSource(Map<String, String> map) {
            this.map = map;
        }
        @Override
        public boolean isAvailable(String expression) {
            return map.containsKey(expression);
        }
        @Override
        public boolean isAvailable(Class<?> valueType) {
            return false;
        }
        @Override
        public <T> T retrieve(String expression, Class<T> valueType) {
            System.out.println(expression);
            return (T) map.get(expression);
        }
        @Override
        public <T> T retrieve(Class<T> valueType) {
            return null;
        }
        @Override
        public <T> List<T> retrieveList(String expression, Class<T> valueType) {
            return null;
        }
        @Override
        public <T> List<T> retrieveList(Class<T> valueType) {
            return null;
        }
        
    }
}
