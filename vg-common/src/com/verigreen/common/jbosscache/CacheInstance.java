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

import org.jboss.cache.Cache;
import org.jboss.cache.CacheFactory;
import org.jboss.cache.DefaultCacheFactory;

public class CacheInstance {
    
    private final String CONFIG_PATH;
    
    private Cache<String, Object> _cache;
    
    private CacheInstance(String configPath) {
        
        CONFIG_PATH = configPath;
    }
    
    public Cache<String, Object> getCache() {
        
        return _cache;
    }
    
    public void destroy() {
        
        _cache.stop();
        _cache.destroy();
    }
    
    public void start() {
        
        CacheFactory<String, Object> factory = new DefaultCacheFactory<String, Object>();
        String configFileName = CONFIG_PATH;
        _cache = factory.createCache(configFileName, false);
        _cache.create();
        _cache.start();
    }
}
