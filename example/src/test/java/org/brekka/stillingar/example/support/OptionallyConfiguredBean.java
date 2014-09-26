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

package org.brekka.stillingar.example.support;

import org.brekka.stillingar.api.annotations.Configured;

/**
 * A bean that has a @Configured field with required=false to make it optional. It should
 * be possible to load this class without any error occurring if there is no configuration value for it.
 *
 * @author Andrew Taylor
 */
@Configured
public class OptionallyConfiguredBean {
    @Configured(value="//c:ThirdPartyConfiguration/c:Host", required=false)
    private String host;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
