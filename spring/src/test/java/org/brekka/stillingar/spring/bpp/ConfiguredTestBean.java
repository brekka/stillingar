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

package org.brekka.stillingar.spring.bpp;

import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.brekka.stillingar.api.annotations.ConfigurationListener;
import org.brekka.stillingar.api.annotations.Configured;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * ConfiguredTestBean
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Configured
class ConfiguredTestBean {

    @Configured("/c:value1")
    private String value1;
    
    @Configured("/c:value2")
    private Integer value2;

    
    private Long value3;
    
    private UUID value4;
    
    @Configured("/c:value5")
    private List<Date> value5;
    
    private List<URI> value6;
    
    private List<Locale> value7;
    
    private Calendar value8;
    
    private String value9;
    
    @Configured("/c:value3")
    public void setValue3(Long value3) {
        this.value3 = value3;
    }
    
    /**
     * @return the value3
     */
    public Long getValue3() {
        return value3;
    }
    
    /**
     * @return the value4
     */
    public UUID getValue4() {
        return value4;
    }
    
    /**
     * @return the value1
     */
    public String getValue1() {
        return value1;
    }
    
    /**
     * @return the value2
     */
    public Integer getValue2() {
        return value2;
    }
    
    /**
     * @param value6 the value6 to set
     */
    @Configured
    public void setValue6(List<URI> value6) {
        this.value6 = value6;
    }
    
    /**
     * @return the value5
     */
    public List<Date> getValue5() {
        return value5;
    }

    /**
     * @return the value6
     */
    public List<URI> getValue6() {
        return value6;
    }

    /**
     * @return the value7
     */
    public List<Locale> getValue7() {
        return value7;
    }
    
    /**
     * @return the value8
     */
    public Calendar getValue8() {
        return value8;
    }

    /**
     * @return the value9
     */
    public String getValue9() {
        return value9;
    }

    @ConfigurationListener
    public void init(@Configured 
                       UUID value4, 
                       @Configured("/c:value7") 
                       List<Locale> value7,
                       Calendar value8,
                       @Qualifier("value9")
                       String value9) {
        this.value4 = value4;
        this.value7 = value7;
        this.value8 = value8;
        this.value9 = value9;
    }
}
