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

import com.verigreen.collector.buildverification.CommitItemVerifier;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;

public class OnFailedByParentHandler extends DecisionHandler {
    
    public OnFailedByParentHandler(CommitItem item) {
        
        super(item);
    }
    
    @Override
    protected void doHandle() {
        
        CommitItemVerifier verifier =
                CollectorApi.getCommitItemVerifierManager().get(_commitItem.getKey());
        if (verifier != null) {
            verifier.cancel();
        }
    }
}
