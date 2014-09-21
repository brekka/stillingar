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

package org.brekka.stillingar.jackson.config;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 * TODO Description of TestConfig
 *
 * @author Andrew Taylor
 */
public class TestConfig {

    private MOTD motd;

    private CompanyY companyY;

    private CompanyX companyX;

    private Database database;

    private List<FeatureFlag> featureFlag;

    private Services services;

    private Security security;

    public CompanyY getCompanyY() {
        return companyY;
    }

    public void setCompanyY(CompanyY companyY) {
        this.companyY = companyY;
    }

    public CompanyX getCompanyX() {
        return companyX;
    }

    public void setCompanyX(CompanyX companyX) {
        this.companyX = companyX;
    }

    public List<FeatureFlag> getFeatureFlag() {
        return featureFlag;
    }

    public void setFeatureFlag(List<FeatureFlag> featureFlag) {
        this.featureFlag = featureFlag;
    }

    public MOTD getMotd() {
        return motd;
    }

    public void setMotd(MOTD motd) {
        this.motd = motd;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public Services getServices() {
        return services;
    }

    public void setServices(Services services) {
        this.services = services;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public static class CompanyY {
        private WebService warehouseWebService;

        public WebService getWarehouseWebService() {
            return warehouseWebService;
        }

        public void setWarehouseWebService(WebService warehouseWebService) {
            this.warehouseWebService = warehouseWebService;
        }
    }

    public static class FeatureFlag {
        private String key;
        private boolean enabled;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Database {
        private String dataSource;
        private String url;
        private String driver;
        private String username;
        private String password;

        public String getDataSource() {
            return dataSource;
        }

        public void setDataSource(String dataSource) {
            this.dataSource = dataSource;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDriver() {
            return driver;
        }

        public void setDriver(String driver) {
            this.driver = driver;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class MOTD {
        private String id;
        private String message;
        private DateTime expires;
        private String language;
        private List<String> references;
        private Integer number;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public DateTime getExpires() {
            return expires;
        }

        public void setExpires(DateTime expires) {
            this.expires = expires;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public List<String> getReferences() {
            return references;
        }

        public void setReferences(List<String> references) {
            this.references = references;
        }

        public Integer getNumber() {
            return number;
        }

        public void setNumber(Integer number) {
            this.number = number;
        }

    }

    public static class Services {

        private Rules rules;

        public Rules getRules() {
            return rules;
        }

        public void setRules(Rules rules) {
            this.rules = rules;
        }

        public static class Rules {

            private Transaction transaction;
            private Fraud fraud;

            public Transaction getTransaction() {
                return transaction;
            }

            public void setTransaction(Transaction transaction) {
                this.transaction = transaction;
            }

            public Fraud getFraud() {
                return fraud;
            }

            public void setFraud(Fraud fraud) {
                this.fraud = fraud;
            }

            public static class Fraud {

                private boolean enabled;
                private float triggerFactor;
                private short scale;
                private long length;
                private List<String> keyword;

                public boolean isEnabled() {
                    return enabled;
                }

                public void setEnabled(boolean enabled) {
                    this.enabled = enabled;
                }

                public float getTriggerFactor() {
                    return triggerFactor;
                }

                public void setTriggerFactor(float triggerFactor) {
                    this.triggerFactor = triggerFactor;
                }

                public short getScale() {
                    return scale;
                }

                public void setScale(short scale) {
                    this.scale = scale;
                }

                public long getLength() {
                    return length;
                }

                public void setLength(long length) {
                    this.length = length;
                }

                public List<String> getKeyword() {
                    return keyword;
                }

                public void setKeyword(List<String> keyword) {
                    this.keyword = keyword;
                }

            }

            public static class Transaction {

                private int maxQuantity;
                private BigDecimal maxAmount;

                public int getMaxQuantity() {
                    return maxQuantity;
                }

                public void setMaxQuantity(int maxQuantity) {
                    this.maxQuantity = maxQuantity;
                }

                public BigDecimal getMaxAmount() {
                    return maxAmount;
                }

                public void setMaxAmount(BigDecimal maxAmount) {
                    this.maxAmount = maxAmount;
                }
            }
        }
    }

    public static class Security {
        private byte[] publicKey;
        private BigInteger factor;
        private byte flag;
        private String timeUnit;
        private Period lockDuration;

        public byte[] getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(byte[] publicKey) {
            this.publicKey = publicKey;
        }

        public BigInteger getFactor() {
            return factor;
        }

        public void setFactor(BigInteger factor) {
            this.factor = factor;
        }

        public byte getFlag() {
            return flag;
        }

        public void setFlag(byte flag) {
            this.flag = flag;
        }

        public String getTimeUnit() {
            return timeUnit;
        }

        public void setTimeUnit(String timeUnit) {
            this.timeUnit = timeUnit;
        }

        public Period getLockDuration() {
            return lockDuration;
        }

        public void setLockDuration(Period lockDuration) {
            this.lockDuration = lockDuration;
        }
    }

    public static class CompanyX {

    }

    public static class WebService {
        private String url;
        private String username;
        private String password;
        private HttpProxy proxy;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public HttpProxy getProxy() {
            return proxy;
        }

        public void setProxy(HttpProxy proxy) {
            this.proxy = proxy;
        }

    }

    public static class HttpProxy {
        private String url;
        private String username;
        private String password;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

    }
}
