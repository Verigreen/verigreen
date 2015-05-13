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

public class VerigreenNeeded {
    
    private boolean _verigreenNeeded;
    private boolean _shouldRejectCommit;
    private String _reason;
    
    public VerigreenNeeded() {}
    
    public VerigreenNeeded(boolean verigreenNeeded, String reason) {
        
        _verigreenNeeded = verigreenNeeded;
        _reason = reason;
    }
    
    public VerigreenNeeded(boolean verigreenNeeded, boolean shouldRejectCommit, String reason) {
        
        _verigreenNeeded = verigreenNeeded;
        _reason = reason;
        _shouldRejectCommit = shouldRejectCommit;
    }
    
    public boolean getVerigreenNeeded() {
        
        return _verigreenNeeded;
    }
    
    public void setVerigreenNeeded(boolean verigreenNeeded) {
        
        _verigreenNeeded = verigreenNeeded;
    }
    
    public String getReason() {
        
        return _reason;
    }
    
    public void setReason(String reason) {
        
        _reason = reason;
    }
    
    public boolean getShouldRejectCommit() {
        
        return _shouldRejectCommit;
    }
    
    public void setShouldRejectCommit(boolean shouldRejectCommit) {
        
        _shouldRejectCommit = shouldRejectCommit;
    }
    
    @Override
    public String toString() {
        
        return String.format(
                "VerigreenNeeded [_verigreenNeeded=%s, _shouldRejectCommit=%s, _reason=%s]",
                _verigreenNeeded,
                _shouldRejectCommit,
                _reason);
    }
}
