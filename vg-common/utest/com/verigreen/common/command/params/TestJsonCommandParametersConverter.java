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
package com.verigreen.common.command.params;

import junit.framework.Assert;

import org.junit.Test;

import com.verigreen.common.command.params.CommandParametersConverter;
import com.verigreen.common.command.params.JsonCommandParametersConverter;
import com.verigreen.common.command.params.TEQCommandParameters;
import com.verigreen.common.exception.LabException;

public class TestJsonCommandParametersConverter {
    
    private final CommandParametersConverter _converter = new JsonCommandParametersConverter();
    
    @Test
    public void testSerialize() {
        
        TEQCommandParameters params = new DummyCommandParameters(1, "blabla", true);
        String serializedParams = _converter.serialize(params);
        TEQCommandParameters deserializedParams = _converter.deserialize(serializedParams);
        Assert.assertEquals(params, deserializedParams);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeWithBadFormat() {
        
        _converter.deserialize("ddd");
    }
    
    @Test(expected = LabException.class)
    public void testDeserializeWithClassNotFound() {
        
        TEQCommandParameters params = new DummyCommandParameters(1, "blabla", true);
        String serializedParams = _converter.serialize(params);
        //mess with class name to fail deserialization
        String badParams = serializedParams.replaceAll(params.getClass().getSimpleName(), "0");
        try {
            _converter.deserialize(badParams);
        } catch (LabException e) {
            Assert.assertEquals(ClassNotFoundException.class, e.getCause().getClass());
            throw e;
        }
    }
}
