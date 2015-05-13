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

import com.verigreen.common.concurrency.RuntimeUtils;
import com.verigreen.common.exception.LabException;
import com.verigreen.common.utils.GsonSerializer;

public class JsonCommandParametersConverter implements CommandParametersConverter {
    
    private static final String SEPARATOR = ";";
    
    @Override
    public String serialize(TEQCommandParameters params) {
        
        return params.getClass().getName().concat(SEPARATOR).concat(GsonSerializer.toJson(params));
    }
    
    @Override
    public TEQCommandParameters deserialize(String params) {
        
        String[] split = params.split(SEPARATOR, 2);
        if (split == null || split.length != 2) {
            throw new IllegalArgumentException(String.format("Wrong format of params: %s", params));
        }
        
        return GsonSerializer.fromJson(split[1], getParamsClass(split));
    }
    
    private Class<? extends TEQCommandParameters> getParamsClass(String[] split) {
        
        Class<? extends TEQCommandParameters> paramsClass = null;
        try {
            paramsClass = RuntimeUtils.cast(Class.forName(split[0]));
        } catch (ClassNotFoundException e) {
            throw new LabException(String.format(
                    "Failed creating parameters class for class name: %s",
                    split[0]), e);
        }
        
        return paramsClass;
    }
}
