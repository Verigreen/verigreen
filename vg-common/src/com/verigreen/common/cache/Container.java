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

import java.util.Collection;
import java.util.List;

import com.verigreen.common.exception.LabException;
import com.verigreen.common.utils.StringUtils;

public abstract class Container<TEntity extends Entity> {
    
    protected final UniqueIndex<String, TEntity> _keyToEntity = new UniqueIndex<>();
    
    public TEntity get(String id) {
        
        if (StringUtils.isNullOrEmpty(id)) {
            throw new LabException(String.format("Invalid key: %s", id));
        }
        
        return _keyToEntity.get(id);
    }
    
    public List<TEntity> getAll() {
        
        return _keyToEntity.getAll();
    }
    
    public void add(List<TEntity> entities) {
        
        for (TEntity currEntity : entities) {
            add(currEntity);
        }
    }
    
    public void add(TEntity entity) {
        
        _keyToEntity.putIfAbsent(entity.getId(), entity);
    }
    
    public void update(TEntity entity) {
        
        _keyToEntity.put(entity.getId(), entity);
    }
    
    public void update(Collection<TEntity> etities) {
        
        for (TEntity currEntity : etities) {
            update(currEntity);
        }
    }
    
    public void delete(Collection<TEntity> etities) {
        
        for (TEntity currEntity : etities) {
            _keyToEntity.delete(currEntity.getId());
        }
    }
    
    public void clear() {
        
        _keyToEntity.clear();
    }
    
    @Override
    public String toString() {
        
        StringBuilder builder = new StringBuilder();
        for (TEntity currEntity : _keyToEntity.getAll()) {
            builder.append(currEntity);
        }
        
        return String.format("Container (size: %s) [%s]", _keyToEntity.getAll().size(), builder);
    }
}
