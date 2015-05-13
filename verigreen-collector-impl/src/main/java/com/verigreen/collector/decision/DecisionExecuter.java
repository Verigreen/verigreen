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

import java.util.Collection;
import java.util.List;

import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.common.concurrency.RuntimeUtils;
import com.verigreen.common.utils.CollectionUtils;

public class DecisionExecuter {
    
    public void execute(Collection<List<Decision>> decisions) {
        
        if (!CollectionUtils.isNullOrEmpty(decisions)) {
            doExecute(decisions);
        }
    }
    
    private void doExecute(Collection<List<Decision>> decisions) {
        
        for (List<Decision> currBranchDecisions : decisions) {
            for (Decision currDecision : currBranchDecisions) {
                try {
                    VerigreenLogger.get().log(
                            getClass().getName(),
                            RuntimeUtils.getCurrentMethodName(),
                            String.format("Going to run decision (%s)", currDecision));
                    currDecision.getHandler().handle();
                } catch (Exception e) {
                    VerigreenLogger.get().error(
                            getClass().getName(),
                            RuntimeUtils.getCurrentMethodName(),
                            String.format("Decision handler failed (%s)", currDecision),
                            e);
                }
            }
        }
    }
}
