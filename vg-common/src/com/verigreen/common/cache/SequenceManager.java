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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages sequences for DB {@link Entity}. see also {@link AutoIncrementedField}
 * 
 * @author kornfeld
 */
public class SequenceManager {
    
    private final static SequenceManager _instance = new SequenceManager();
    private final Map<Class<? extends Entity>, AtomicInteger> _entitiesToSequences =
            new ConcurrentHashMap<Class<? extends Entity>, AtomicInteger>();
    private final int _seed = 0;
    
    private SequenceManager() {}
    
    public static SequenceManager getInstance() {
        
        return _instance;
    }
    
    /**
     * Gets the next integer in sequence for the given entity class
     */
    public synchronized int getNext(Class<? extends Entity> clazz) {
        
        AtomicInteger next = _entitiesToSequences.get(clazz);
        if (next == null) {
            _entitiesToSequences.put(clazz, new AtomicInteger(_seed));
        }
        
        return _entitiesToSequences.get(clazz).addAndGet(1);
    }
    
    public void clear() {
        
        _entitiesToSequences.clear();
    }
}
