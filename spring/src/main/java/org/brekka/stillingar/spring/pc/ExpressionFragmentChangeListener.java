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

package org.brekka.stillingar.spring.pc;

import org.brekka.stillingar.core.ValueChangeListener;
import org.brekka.stillingar.spring.expr.ExpressionFragment;

/**
 * Change listener for updating the value of an {@link ExpressionFragment}
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class ExpressionFragmentChangeListener implements ValueChangeListener<String> {
    /**
     * The fragment to update
     */
    private final ExpressionFragment expressionFragment;
    
    /**
     * @param expressionFragment The fragment to update
     */
    public ExpressionFragmentChangeListener(ExpressionFragment expressionFragment) {
        this.expressionFragment = expressionFragment;
    }

    /* (non-Javadoc)
     * @see org.brekka.stillingar.core.ValueChangeListener#onChange(java.lang.Object)
     */
    @Override
    public void onChange(String newValue, String oldValue) {
        expressionFragment.setValue(newValue);
    }
}
