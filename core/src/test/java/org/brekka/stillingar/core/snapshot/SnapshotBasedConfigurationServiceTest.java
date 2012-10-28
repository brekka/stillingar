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

package org.brekka.stillingar.core.snapshot;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.brekka.stillingar.api.ConfigurationSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * TODO Description of SnapshotBasedConfigurationSourceTest
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@RunWith(MockitoJUnitRunner.class)
public class SnapshotBasedConfigurationServiceTest {

    @Mock
    private SnapshotManager snapshotManager;
    
    @Mock 
    private Snapshot initialSnapshot;
    
    @Mock
    private ConfigurationSource defaultConfigurationSource;
    
    @Mock
    private ConfigurationSource snapshotConfigurationSource;
    
    
    private SnapshotBasedConfigurationService source;
    
    @Test
    public void testInitWithDefaults() throws Exception {
        when(snapshotManager.retrieveInitial()).thenReturn(initialSnapshot);
        when(initialSnapshot.getSource()).thenReturn(snapshotConfigurationSource);
        
        source = new SnapshotBasedConfigurationService(snapshotManager, defaultConfigurationSource);
        
        verify(snapshotManager).retrieveInitial();
        verify(initialSnapshot).getSource();
    }
}
