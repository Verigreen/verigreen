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

import com.google.gson.Gson;

public class GsonSerializer {
    
    private static final Gson _gson = new Gson();

    private GsonSerializer() {
    }

    /**
     * Serialize a generic object to json
     */
    public static <T> String toJson(T toSerialize) {
        
        return _gson.toJson(toSerialize);
    }
    
    /**
     * Deserialize object from json string
     * 
     * @param json
     *            - json representation of clazz instance
     * @param clazz
     *            - must be a concrete class
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        
        return _gson.fromJson(json, clazz);
    }
}
