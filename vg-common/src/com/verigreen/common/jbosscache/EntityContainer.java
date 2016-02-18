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
package com.verigreen.common.jbosscache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jboss.cache.Cache;
import org.jboss.cache.CacheStatus;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;

import com.verigreen.common.concurrency.RuntimeUtils;
import com.verigreen.common.jbosscache.entity.Entity;
import com.verigreen.common.spring.SpringContextHolder;
import com.verigreen.common.utils.CloneUtils;
import com.verigreen.common.utils.RetriableOperationExecutor;
import com.verigreen.common.utils.RetriableOperationExecutor.RetriableOperation;


public abstract class EntityContainer<K, V extends Entity<K>> implements
        EntityContainerFacade<K, V> {
    
    private Cache<String, Object> _cache;
    private final String _cacheName;
    
    public EntityContainer() {
        
        _cacheName = getClass().getSimpleName();
    }
    
    @Override
    public V get(K key) {
        
        V cacheValue = RuntimeUtils.cast(getCache().get(getFqn(key), key.toString()));
        
        return getClonedValue(cacheValue);
    }

    
    @Override
    public V getWithRetry(final K key) {
        
        try {
            return RetriableOperationExecutor.execute(new RetriableOperation<V>() {
                
                @Override
                public V execute() {
                    
                    V value = get(key);
                    
                    if (value == null) {
                        throw new RuntimeException("Key [" + key + "] was not found");
                    }
                    return value;
                }
            }, 1000, 10000, 3, RuntimeException.class);
        } catch (RuntimeException e) {
        	
            return null;
        }
    };
    
    @Override
    public void save(V value) {
        
        K key = value.getKey();
        getCache().put(getFqn(key), key.toString(), value);
    }
    
    @Override
    public void save(List<V> value) {
    	
        for (V v : value) {
            save(v);
        }
    }
    
    @Override
    public List<V> findByCriteria(Criteria<V> criteria) {
        
        List<V> items = getAll();
        
        return internalFindByCriteria(criteria, items);
    }
    
    
    @Override
    public void deleteAll() {
        
        getCache().removeNode(Fqn.fromString(_cacheName));
    }
    
    @Override
    public void delete(K key) {
        
        getCache().remove(getFqn(key), key.toString());
    }
    
    @Override
    public void delete(List<V> values) {
        
        for (V v : values) {
            delete(v.getKey());
        }
    }
    
    @Override
    public void deleteByCriteria(Criteria<V> criteria) {
        
        List<V> list = findByCriteria(criteria);
        for (V v : list) {
            delete(v.getKey());
        }
    }
    
    @Override
    public List<V> getAll() {
        
        Node<String, Object> cache = getCache().getNode(Fqn.fromString(_cacheName));
        ArrayList<V> list = new ArrayList<>();
        if (cache != null) {
            populateValues(cache, list);
        }
        
        return list;
    }
    
    private void populateValues(Node<String, Object> cache, ArrayList<V> list) {
        
        Set<Node<String, Object>> children = cache.getChildren();
        for (Node<String, Object> node : children) {
            Iterator<String> iterator = node.getKeys().iterator();
            if (iterator.hasNext()) {
                String key = iterator.next();
                V value = RuntimeUtils.<V> cast(node.get(key));
                list.add(getClonedValue(value));
            }
        }
    }
    
    private Fqn<String> getFqn(K key) {
        
        return Fqn.fromElements(_cacheName, key.toString());
    }
    
    protected Cache<String, Object> getCache() {
        
        if (_cache == null || _cache.getCacheStatus() != CacheStatus.STARTED) {
            _cache = SpringContextHolder.getInstance().getBean(CacheInstance.class).getCache();
        }
        
        return _cache;
    }
    
    protected List<V> internalFindByCriteria(Criteria<V> criteria, List<V> items) {
        
        List<V> foundItems = new ArrayList<>();
        
        for (V wi : items) {
            if (criteria.match(wi)) {
                foundItems.add(wi);
            }
        }
        
        return foundItems;
    }
    
    private V getClonedValue(V cacheValue) {
        
        return CloneUtils.clone(cacheValue);
    }
}
