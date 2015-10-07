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
import com.verigreen.collector.buildverification.CommitItemCanceler;
import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.common.concurrency.RuntimeUtils;



public class OnFailedByParentHandler extends DecisionHandler {
    
    public OnFailedByParentHandler(CommitItem item) {
        
        super(item);
    }
    
    @Override
    protected void doHandle() {
        
        VerigreenLogger.get().log(
                getClass().getName(),
                RuntimeUtils.getCurrentMethodName(),
                String.format("Cancelling verification of %s...", _commitItem));
        CommitItemCanceler.getInstance().add(_commitItem);
    }
}
