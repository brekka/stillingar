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

package org.brekka.stillingar.example.support;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import org.brekka.stillingar.api.annotations.Configured;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test loading types via configured fields
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Configured
public class ConfiguredFieldTypes {

    
    @Configured("//c:Testing/c:AnyURI")
    private URI uri;
    
    @Configured("//c:Testing/c:Boolean")
    private Boolean booleanValue;
    
    @Configured("//c:Testing/c:Boolean")
    private boolean booleanPrimitive;
    
    @Configured("//c:FeatureFlag[@key='turbo']")
    private boolean booleanFeatureFlag;
    
    @Configured("//c:Testing/c:Byte")
    private Byte byteValue;
    
    @Configured("//c:Testing/c:Byte")
    private byte bytePrimitive;
    
    @Configured("//c:Testing/c:Date")
    private Calendar dateAsCalendar;
    
    @Configured("//c:Testing/c:Date")
    private Date dateAsDate;
    
    @Configured("//c:Testing/c:DateTime")
    private Calendar dateTimeAsCalendar;
    
    @Configured("//c:Testing/c:DateTime")
    private Date dateTimeAsDate;
    
    @Configured("//c:Testing/c:Decimal")
    private BigDecimal decimal;
    
    @Configured("//c:Testing/c:Double")
    private Double doubleValue;
    
    @Configured("//c:Testing/c:Double")
    private double doublePrimitive;
    
    @Configured("//c:Testing/c:Float")
    private Float floatValue;
    
    @Configured("//c:Testing/c:Float")
    private float floatPrimitive;    
    
    @Configured("//c:Testing/c:Int")
    private Integer intValue;
    
    @Configured("//c:Testing/c:Int")
    private int intPrimitive;    
    
    @Configured("//c:Testing/c:Integer")
    private BigInteger integerValue;
    
    @Configured("//c:Testing/c:Language")
    private Locale language;
    
    @Configured("//c:Testing/c:Long")
    private Long longValue;
    
    @Configured("//c:Testing/c:Long")
    private long longPrimitive;
    
    @Configured("//c:Testing/c:Short")
    private Short shortValue;
    
    @Configured("//c:Testing/c:Short")
    private short shortPrimitive;
    
    @Configured("//c:Testing/c:String")
    private String string;
    
    @Configured("//c:Testing/c:Time")
    private Calendar timeAsCalendar;
    
    @Configured("//c:Testing/c:Binary")
    private byte[] binary;
    
    @Configured("//c:Testing/c:UUID")
    private UUID uuid;
    
    @Configured("//c:Testing")
    private Object testingObject;
    
    
    @Configured("//c:Testing")
    private Element testingElement;
    
    @Configured("/c:Configuration")
    private Document root;

    /**
     * @return the uri
     */
    public URI getUri() {
        return uri;
    }

    /**
     * @return the booleanValue
     */
    public Boolean getBooleanValue() {
        return booleanValue;
    }

    /**
     * @return the booleanPrimitive
     */
    public boolean isBooleanPrimitive() {
        return booleanPrimitive;
    }

    /**
     * @return the byteValue
     */
    public Byte getByteValue() {
        return byteValue;
    }

    /**
     * @return the bytePrimitive
     */
    public byte getBytePrimitive() {
        return bytePrimitive;
    }

    /**
     * @return the dateAsCalendar
     */
    public Calendar getDateAsCalendar() {
        return dateAsCalendar;
    }

    /**
     * @return the dateAsDate
     */
    public Date getDateAsDate() {
        return dateAsDate;
    }

    /**
     * @return the dateTimeAsCalendar
     */
    public Calendar getDateTimeAsCalendar() {
        return dateTimeAsCalendar;
    }

    /**
     * @return the dateTimeAsDate
     */
    public Date getDateTimeAsDate() {
        return dateTimeAsDate;
    }

    /**
     * @return the decimal
     */
    public BigDecimal getDecimal() {
        return decimal;
    }

    /**
     * @return the doubleValue
     */
    public Double getDoubleValue() {
        return doubleValue;
    }

    /**
     * @return the doublePrimitive
     */
    public double getDoublePrimitive() {
        return doublePrimitive;
    }

    /**
     * @return the floatValue
     */
    public Float getFloatValue() {
        return floatValue;
    }

    /**
     * @return the floatPrimitive
     */
    public float getFloatPrimitive() {
        return floatPrimitive;
    }

    /**
     * @return the intValue
     */
    public Integer getIntValue() {
        return intValue;
    }

    /**
     * @return the intPrimitive
     */
    public int getIntPrimitive() {
        return intPrimitive;
    }

    /**
     * @return the integerValue
     */
    public BigInteger getIntegerValue() {
        return integerValue;
    }

    /**
     * @return the language
     */
    public Locale getLanguage() {
        return language;
    }

    /**
     * @return the longValue
     */
    public Long getLongValue() {
        return longValue;
    }

    /**
     * @return the longPrimitive
     */
    public long getLongPrimitive() {
        return longPrimitive;
    }

    /**
     * @return the shortValue
     */
    public Short getShortValue() {
        return shortValue;
    }

    /**
     * @return the shortPrimitive
     */
    public short getShortPrimitive() {
        return shortPrimitive;
    }

    /**
     * @return the string
     */
    public String getString() {
        return string;
    }

    /**
     * @return the timeAsCalendar
     */
    public Calendar getTimeAsCalendar() {
        return timeAsCalendar;
    }

    /**
     * @return the binary
     */
    public byte[] getBinary() {
        return binary;
    }

    /**
     * @return the uuid
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * @return the testingObject
     */
    public Object getTestingObject() {
        return testingObject;
    }

    /**
     * @return the testingElement
     */
    public Element getTestingElement() {
        return testingElement;
    }

    /**
     * @return the root
     */
    public Document getRoot() {
        return root;
    }
}
