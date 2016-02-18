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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.verigreen.common.utils.CloneUtils;

public class UniqueIndex<TKey, TValue extends Serializable> extends Index<TKey, TValue> {
    
    @Override
    public TValue get(TKey key) {
        
        return CloneUtils.clone(getDBEntity(key));
    }
    
    public List<TValue> getAll() {
        
        List<TValue> ret = new ArrayList<>();
        for (Entry<TKey, TValue> entry : _map.entrySet()) {
            ret.add(entry.getValue());
        }
        
        return ret;
    }
}
