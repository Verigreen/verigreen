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

public class VerigreenNeededRequest extends Request {
    
    private final static String request = "/verigreen-needed";
    private final static String postRequest = "/add-commit";
    
    public VerigreenNeededRequest(
            String baseUri,
            String parentBranchName,
            String branchNameToVerify,
            String commitId,
            String committer) throws UnsupportedEncodingException {
        
        super(baseUri + request);
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("parentBranchName", URLEncoder.encode(parentBranchName, "UTF-8"));
        queryParams.put("branchNameToVerify", branchNameToVerify);
        queryParams.put("commitId", commitId);
        queryParams.put("committer", committer);
        setQueryParams(queryParams);
    }
    
    public VerigreenNeededRequest(
            String baseUri,
            String commitId) throws UnsupportedEncodingException {
        
        super(baseUri + request + postRequest);
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("commitId", commitId);
        setQueryParams(queryParams);
    }
}
