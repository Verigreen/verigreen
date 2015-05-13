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

/**
 * DB-like auto incremented integer, should be used for entities who need an order field for
 * example.
 * 
 * @author kornfeld
 */
public class AutoIncrementedField implements Serializable {
    
    private static final long serialVersionUID = -7062232788934057843L;
    
    private final int _sequence;
    
    public AutoIncrementedField(Class<? extends Entity> type) {
        
        _sequence = SequenceManager.getInstance().getNext(type);
    }
    
    public AutoIncrementedField(int sequence) {
        
        _sequence = sequence;
    }
    
    public int getSequence() {
        
        return _sequence;
    }
}
