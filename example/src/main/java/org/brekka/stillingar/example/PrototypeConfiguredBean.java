/*
 * Copyright 2011 the original author or authors.
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

package org.brekka.stillingar.example;

import org.brekka.stillingar.api.Replacement;
import org.brekka.stillingar.api.annotations.ConfigurationListener;
import org.brekka.stillingar.api.annotations.Configured;

@Configured
public class PrototypeConfiguredBean {

    private float frequency;
    
    private boolean configureCalled;
    
    @ConfigurationListener
    public void configure(
            @Configured("//c:MOTD")
            Replacement<String> motd) {
        configureCalled = true;
    }
    
    
    public float getFrequency() {
        return frequency;
    }
    
    public boolean isConfigureCalled() {
        return configureCalled;
    }
}
