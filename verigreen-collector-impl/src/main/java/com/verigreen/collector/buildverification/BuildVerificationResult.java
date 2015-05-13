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
package com.verigreen.collector.buildverification;

import java.net.URI;

import com.verigreen.collector.api.VerificationStatus;

public class BuildVerificationResult {
    
    private final int _buildNumber;
    private final URI _buildUrl;
    private final VerificationStatus _status;
    
    public BuildVerificationResult(int buildNumber, URI buildUrl, VerificationStatus status) {
        
        _buildNumber = buildNumber;
        _buildUrl = buildUrl;
        _status = status;
    }
    
    public int getBuildNumber() {
        
        return _buildNumber;
    }
    
    public URI getBuildUrl() {
        
        return _buildUrl;
    }
    
    public VerificationStatus getStatus() {
        
        return _status;
    }
}
