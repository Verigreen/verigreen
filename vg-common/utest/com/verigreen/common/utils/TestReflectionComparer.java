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
package com.verigreen.common.utils;

import org.junit.Assert;
import org.junit.Test;

import com.verigreen.common.utils.ReflectionComparer;

public class TestReflectionComparer {
    
    @Test
    public void testIsEqualsGetterNotEquals() {
        
        Sample x = new Sample(1, "a1");
        Sample y = new Sample(2, "a1");
        Assert.assertFalse(ReflectionComparer.isEquals(x, y));
    }
    
    @Test
    public void testEquals() {
        
        Sample x = new Sample(1, "a1");
        Sample y = new Sample(1, "a2");
        Assert.assertTrue(ReflectionComparer.isEquals(x, y));
    }
    
    private class Sample {
        
        private final int _field1;
        private final String _field2;
        
        public Sample(int field1, String field2) {
            
            _field1 = field1;
            _field2 = field2;
        }
        
        public int getField1() {
            
            return _field1;
        }
        
        // reflection use 
        @SuppressWarnings("unused")
        public void assertIfCall() {
            
            Assert.fail(getField1() + " " + _field2);
        }
    }
}
