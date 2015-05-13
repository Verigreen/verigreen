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
package com.verigreen.common.jbosscache.entity;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.verigreen.common.concurrency.RuntimeUtils;


public abstract class Entity<TKey> implements Serializable {
    
    private static final long serialVersionUID = 7814818932463266806L;
    private final TKey _key;
    
    public Entity(TKey key) {
        
        _key = key;
    }
    
    public TKey getKey() {
        
        return _key;
    }
    
    @Override
    public int hashCode() {
        
        return new HashCodeBuilder().append(_key).toHashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Entity<TKey> other = RuntimeUtils.cast(obj);
        if (_key == null) {
            if (other._key != null)
                return false;
        } else if (!_key.equals(other._key)) {
            return false;
        }
        
        return true;
    }
}
