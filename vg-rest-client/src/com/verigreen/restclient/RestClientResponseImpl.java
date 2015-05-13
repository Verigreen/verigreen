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
package com.verigreen.restclient;

import java.io.InputStream;
import java.util.Map;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

public class RestClientResponseImpl implements RestClientResponse {
    
    private final Response _response;
    
    public RestClientResponseImpl(Response response) {
        
        _response = response;
    }
    
    @Override
    public <T> T getEntity(Class<T> entityType) {
        
        return _response.readEntity(entityType);
    }
    
    @Override
    public <T> T getEntity(GenericType<T> entityType) {
        
        return _response.readEntity(entityType);
    }
    
    @Override
    public StatusType getStatusInfo() {
        
        return _response.getStatusInfo();
    }
    
    @Override
    public InputStream getEntityStream() {
        
        return _response.hasEntity() ? (InputStream) _response.getEntity() : null;
    }
    
    @Override
    public Map<String, NewCookie> getCookies() {
        
        return _response.getCookies();
    }
}
