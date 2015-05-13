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
package com.verigreen.collector.rest.requests;

import javax.ws.rs.core.MediaType;

import com.verigreen.collector.api.Users;
import com.verigreen.restclient.Request;

public class PermittedUserRequest extends Request {
    
    private final static String request = "/verigreen-needed/permitted-users";
    
    public PermittedUserRequest(String baseUri, Users users) {
        
        super(baseUri + request, users, MediaType.APPLICATION_JSON_TYPE);
    }
}
