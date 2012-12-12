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

package org.brekka.stillingar.spring.snapshot;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brekka.stillingar.api.ConfigurationException;
import org.brekka.stillingar.core.delta.DeltaValueInterceptor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;

/**
 * Delta value interceptor that looks for value beans implementing {@link Lifecycle}, {@link InitializingBean} or
 * {@link DisposableBean}, calling their corresponding start/stop methods if it encounters such beans.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class SnapshotDeltaValueInterceptor implements DeltaValueInterceptor {

    private static final Log log = LogFactory.getLog(SnapshotDeltaValueInterceptor.class);
    
    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.delta.DeltaValueInterceptor#created(java.lang.Object)
     */
    @Override
    public <T> T created(T value) {
        if (value instanceof List) {
            List<?> valueList = (List<?>) value;
            for (Object subValue : valueList) {
                initializeBean(subValue);
            }
        } else {
            initializeBean(value);
        }
        return value;
    }

    protected void initializeBean(Object value) {
        if (value instanceof InitializingBean) {
            InitializingBean initializingBean = (InitializingBean) value;
            if (log.isInfoEnabled()) {
                log.info(String.format("Starting InitializingBean: %s", value));
            }
            try {
                initializingBean.afterPropertiesSet();
            } catch (Exception e) {
                throw new ConfigurationException(String.format(
                        "Failed to initialize bean '%s'", value), e);
            }
        } else if (value instanceof Lifecycle) {
            Lifecycle lifecycle = (Lifecycle) value;
            if (log.isInfoEnabled()) {
                log.info(String.format("Starting Lifecycle bean: %s", value));
            }
            lifecycle.start();
        } 
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.stillingar.core.delta.DeltaValueInterceptor#released(java.lang.Object)
     */
    @Override
    public void released(Object value) {
        if (value instanceof List) {
            List<?> valueList = (List<?>) value;
            for (Object subValue : valueList) {
                destroyBean(subValue);
            }
        } else {
            destroyBean(value);
        }
    }

    protected void destroyBean(Object value) {
        if (value instanceof DisposableBean) {
            DisposableBean disposableBean = (DisposableBean) value;
            try {
                disposableBean.destroy();
            } catch (Exception e) {
                throw new ConfigurationException(String.format(
                        "Error while disposing bean '%s'", value), e);
            }
            if (log.isInfoEnabled()) {
                log.info(String.format("Stopped DisposableBean: %s", value));
            }
        } else if (value instanceof Lifecycle) {
            Lifecycle lifecycle = (Lifecycle) value;
            lifecycle.stop();
            if (log.isInfoEnabled()) {
                log.info(String.format("Stopped Lifecycle bean: %s", value));
            }
        }
    }
}
