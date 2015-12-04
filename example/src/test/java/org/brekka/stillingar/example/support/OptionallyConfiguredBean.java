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

import org.brekka.stillingar.api.annotations.ConfigurationListener;
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
    private String host = "NoChange";
    
    @Configured(value="//c:Testing/c:Float", required=false)
    private float floatValue = -1f;
    
    private int intValue = -1;
    
    private long longValue = -1;
    
    private short shortValue = (short)-1;
    
    private String stringValue = "NoChange";
    
    private String motd = "NoChange";

    @Configured(value="//c:Testing/c:Short", required=false)
    public void setShort(short shortValue) {
        this.shortValue = shortValue;
    }
    
    @ConfigurationListener
    public void configure(
            @Configured(value="//c:Testing/c:Int", required=false) int intValue,
            @Configured("//c:Testing/c:Long") long longValue,
            @Configured(value="//c:Testing/c:String", required=false) String stringValue,
            @Configured("//c:MOTD") String motd) {
        this.intValue = intValue;
        this.longValue = longValue;
        this.stringValue = stringValue;
        this.motd = motd;
    }
    

    public String getHost() {
        return host;
    }
    
    public int getIntValue() {
        return intValue;
    }
    
    public long getLongValue() {
        return longValue;
    }
    
    public short getShortValue() {
        return shortValue;
    }
    
    public String getMotd() {
        return motd;
    }
    
    public String getStringValue() {
        return stringValue;
    }
    
    public float getFloatValue() {
        return floatValue;
    }
}
