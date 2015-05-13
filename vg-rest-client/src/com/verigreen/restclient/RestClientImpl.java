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
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.lang.CharEncoding;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import com.verigreen.restclient.common.RestClientException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

public class RestClientImpl implements RestClient {
    
    protected Client _client;
    
    public RestClientImpl() {
        
        _client = ClientBuilder.newBuilder().register(JacksonFeatures.class).build();
    }
    
    @Override
    public RestClientResponse post(Request request) {
        
        Builder builder = prepareRequest(request);
        Entity<Object> entity = Entity.entity(request.getEntity(), request.getMediaType());
        Response response = builder.accept(request.getMediaType()).post(entity);
        checkResponse(response);
        
        return new RestClientResponseImpl(response);
    }
    
    @Override
    public RestClientResponse get(Request request) {
        
        Builder builder = prepareRequest(request);
        Response response = builder.accept(request.getMediaType()).get();
        checkResponse(response);
        
        return new RestClientResponseImpl(response);
    }
    
    @Override
    public void put(Request request) {
        
        WebTarget webResource = _client.target(request.getUri());
        Entity<Object> entity = Entity.entity(request.getEntity(), request.getMediaType());
        Response response = webResource.request().accept(request.getMediaType()).put(entity);
        checkResponse(response);
    }
    
    @Override
    public void setAuthentcation(String userName, String password) {
        
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(userName, password);
        _client.register(feature);
    }
    
    private void checkResponse(javax.ws.rs.core.Response response) throws RestClientException {
        
        if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
            String responseStr = "";
            try (Scanner scanner =
                    new Scanner((InputStream) response.getEntity(), CharEncoding.UTF_8)) {
                responseStr = scanner.useDelimiter("\\A").next();
            }
            throw new RestClientException(String.format(
                    "Failed : HTTP error code : %d response message: %s",
                    response.getStatusInfo().getStatusCode(),
                    responseStr), response.getStatusInfo().getStatusCode());
        }
    }
    
    private Builder prepareRequest(Request request) {
        
        WebTarget webTarget = _client.target(request.getUri());
        Map<String, String> queryParams = request.getQueryParams();
        if (queryParams != null) {
            for (Entry<String, String> param : queryParams.entrySet()) {
                webTarget = webTarget.queryParam(param.getKey(), param.getValue());
            }
        }
        Map<String, Object> pathTemplateParams = request.getPathTemplateParams();
        if (pathTemplateParams != null) {
            webTarget = webTarget.resolveTemplates(pathTemplateParams);
        }
        Builder builder = webTarget.request();
        for (Cookie currCookie : request.getCookies()) {
            builder = builder.cookie(currCookie);
        }
        for (Entry<String, Object> currEntry : request.getHeaders().entrySet()) {
            builder = builder.header(currEntry.getKey(), currEntry.getValue());
        }
        
        return builder;
    }
}
