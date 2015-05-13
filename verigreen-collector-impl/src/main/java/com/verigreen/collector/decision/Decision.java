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
package com.verigreen.collector.decision;

public class Decision {
    
    private final String _commitItemId;
    private final DecisionHandler _handler;
    
    public Decision(String commitItemId, DecisionHandler handler) {
        
        _commitItemId = commitItemId;
        _handler = handler;
    }
    
    public String getCommitItemId() {
        
        return _commitItemId;
    }
    
    public DecisionHandler getHandler() {
        
        return _handler;
    }
    
    @Override
    public String toString() {
        
        return String.format(
                "Decision [_commitItemId=%s, _handler=%s]",
                _commitItemId,
                _handler.getClass());
    }
}
