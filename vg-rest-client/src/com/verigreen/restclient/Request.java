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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;

public class Request {
    
    private final String _uri;
    private Map<String, String> _queryParams;
    private Map<String, Object> _pathTemplateParams;
    private final Object _entity;
    private final String _mediaType;
    
    private Collection<Cookie> _cookies = new ArrayList<>();
    private Map<String, Object> _headers = new HashMap<>();
    private String _body;
    
    public Request(String uri, Object entity, MediaType mediaType) {
        
        this(uri, entity, mediaType.toString(), null, null);
    }
    
    public Request(String uri, Object entity, String mediaType) {
        
        this(uri, entity, mediaType, null, null);
    }
    
    public Request(
            String uri,
            Object entity,
            String mediaType,
            List<Cookie> cookies,
            Map<String, Object> headers) {
        
        _uri = uri;
        _entity = entity;
        _mediaType = mediaType;
        if (cookies != null) {
            _cookies = cookies;
        }
        if (headers != null) {
            _headers = headers;
        }
    }
    
    public Request(String uri) {
        
        this(uri, null, MediaType.APPLICATION_JSON_TYPE.toString(), null, null);
    }
    
    public String getUri() {
        
        return _uri;
    }
    
    public Object getEntity() {
        
        return _entity;
    }
    
    public String getMediaType() {
        
        return _mediaType;
    }
    
    public Map<String, String> getQueryParams() {
        
        return _queryParams;
    }
    
    public void setQueryParams(Map<String, String> queryParams) {
        
        _queryParams = queryParams;
    }
    
    public Map<String, Object> getPathTemplateParams() {
        
        return _pathTemplateParams;
    }
    
    public void setPathTemplateParams(Map<String, Object> pathTemplateParams) {
        
        _pathTemplateParams = pathTemplateParams;
    }
    
    public Collection<Cookie> getCookies() {
        
        return _cookies;
    }
    
    public void setCookie(Cookie cookie) {
        
        _cookies.add(cookie);
    }
    
    public Map<String, Object> getHeaders() {
        
        return _headers;
    }
    
    public void addHeader(String name, String value) {
        
        _headers.put(name, value);
    }
    
    public String getBody() {
        
        return _body;
    }
    
    public void setBody(String body) {
        
        this._body = body;
    }
}
