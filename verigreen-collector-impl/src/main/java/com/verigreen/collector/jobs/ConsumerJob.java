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
package com.verigreen.collector.jobs;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.verigreen.collector.decision.DecisionExecuter;
import com.verigreen.collector.decision.decisionmaker.ProtectedBranchesDecisionMaker;

@DisallowConcurrentExecution
public class ConsumerJob implements Job {
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        
        new DecisionExecuter().execute(new ProtectedBranchesDecisionMaker().decide());
    }
}
