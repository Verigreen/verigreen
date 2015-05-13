/*******************************************************************************
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.verigreen.common.cache;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.verigreen.common.cache.Entity;
import com.verigreen.common.cache.EntityImpl;
import com.verigreen.common.cache.SequenceManager;

public class TestSequenceManager {
    
    @Before
    public void setUp() {
        
        SequenceManager.getInstance().clear();
    }
    
    @Test
    public void testGetNext() {
        
        int sequence = SequenceManager.getInstance().getNext(EntityImpl.class);
        int next = SequenceManager.getInstance().getNext(EntityImpl.class);
        Assert.assertEquals(sequence, next - 1);
    }
    
    @Test
    public void testGetNextDifferentEntities() {
        
        int sequence1 = SequenceManager.getInstance().getNext(Entity.class);
        int sequence2 = SequenceManager.getInstance().getNext(EntityImpl.class);
        // The second call to getNext is with different entity therefore its sequence
        // should not be influenced by the first call to getNext
        Assert.assertEquals(sequence1, sequence2);
    }
}
