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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class Index<TKey, TValue> {
    
    protected final ConcurrentMap<TKey, TValue> _map = new ConcurrentHashMap<>();
    
    public void put(TKey key, TValue value) {
        
        _map.put(key, value);
    }
    
    public TValue getDBEntity(TKey key) {
        
        return _map.get(key);
    }
    
    public void delete(TKey key) {
        
        _map.remove(key);
    }
    
    public void clear() {
        
        _map.clear();
    }
    
    public void putIfAbsent(TKey key, TValue value) {
        
        _map.putIfAbsent(key, value);
    }
    
    public abstract TValue get(TKey key);
}
