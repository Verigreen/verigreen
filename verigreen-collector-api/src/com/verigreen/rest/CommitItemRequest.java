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
package com.verigreen.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import com.verigreen.restclient.Request;

public class CommitItemRequest extends Request {
    
    private final static String request = "/commit-items";
    
    public CommitItemRequest(
            String baseUri,
            String protectedBranch,
            String branchId,
            String commitId) throws UnsupportedEncodingException {
        
        super(baseUri + request);
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("protectedBranch", URLEncoder.encode(protectedBranch, "UTF-8"));
        queryParams.put("branchId", branchId);
        queryParams.put("commitId", commitId);
        setQueryParams(queryParams);
    }
}
