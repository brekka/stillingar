/*
 * Copyright 2014 the original author or authors.
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

package org.brekka.stillingar.core.support;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents some kind of typed configuration bean for the purpose of testing.
 *
 * @author Andrew Taylor
 */
public class ConfigBean {

    
    public static List<ConfigBean> listOf(int cnt) {
        List<ConfigBean> beans = new ArrayList<ConfigBean>(cnt);
        for (int i = 0; i < cnt; i++) {
            beans.add(new ConfigBean());
        }
        return beans;
    }
}
