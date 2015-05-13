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
package com.verigreen.collector.api;

public enum VerificationStatus {
    
    NOT_STARTED("Not started", false, false),
    RUNNING("Running", false, false),
    PASSED("Passed", false, false),
    PASSED_AND_PUSHED("Passed and pushed", true, false),
    PASSED_BY_CHILD("Passed transitively by a child commit", true, false),
    FAILED("Failed", true, true),
    TIMEOUT("Timed out", true, true),
    TRIGGER_FAILED("Failed triggering build", true, true),
    MERGE_FAILED("Failed to merge automatically, try solving the conflicts and re-commit", true, true), 
    GIT_FAILURE("Failed due to git failure, could be a techincal issue", true, true), 
    FORCING_PUSH("Job failed, but still will be pushed", false, false),
    FAILED_AND_PUSHED("Job failed, but was successfully pushed", true, false);
   
    
    private String _description;
    private boolean _isFinalState;
    private boolean _isFailureState;
    
    VerificationStatus(String description, boolean isFinalState, boolean isFailureState) {
        
        _description = description;
        _isFinalState = isFinalState;
        _isFailureState = isFailureState;
    }
    
    public boolean isFinalState() {
        
        return _isFinalState;
    }
    
    public boolean isFailureState() {
        
        return _isFailureState;
    }
    
    @Override
    public String toString() {
        
        return _description;
    }
}