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
package com.verigreen.collector.decision.decisionmaker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.common.CommitItemUtils;
import com.verigreen.collector.common.VerigreenNeededLogic;
import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.collector.decision.Decision;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.common.concurrency.RuntimeUtils;
import com.verigreen.common.utils.CollectionUtils;

public class ProtectedBranchesDecisionMaker {
	
	private int numberCommits = Integer.parseInt(VerigreenNeededLogic.properties.getProperty("number.commits"));
    
    public Collection<List<Decision>> decide() {
        
        Collection<List<Decision>> ret = new ArrayList<>();
        CommitItem item = null;
        try {
        	ArrayList<CommitItem> notStarted = new ArrayList<>(CommitItemUtils.filterItems(CommitItemUtils.getNotDone(), VerificationStatus.NOT_STARTED));
            Collection<CommitItem> notDone = CommitItemUtils.getNotDone();
            notDone.removeAll(notStarted);            
            while(notDone.size()<numberCommits && !notStarted.isEmpty()) {
            	item = notStarted.iterator().next();
            	notDone.add(item);
            	notStarted.remove(item);
            }
            VerigreenLogger.get().log(
                    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format("There are %d not done item(s)", notDone.size()));
            if (!CollectionUtils.isNullOrEmpty(notDone)) {
                ret = handle(notDone);
            }
        } catch (Exception e) {
            VerigreenLogger.get().error(
                    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format("Consumer job has failed."),
                    e);
        }
        
        return ret;
    }
    
    private Collection<List<Decision>> handle(Collection<CommitItem> commits) {
        
        Collection<List<Decision>> ret = new ArrayList<>();
        for (List<CommitItem> currProtectedBranchCommits : commitsByProtectedBranch(commits)) {
            ret.add(new DecisionMaker().decide(currProtectedBranchCommits));
        }
        
        return ret;
    }
    
    private Collection<List<CommitItem>> commitsByProtectedBranch(Collection<CommitItem> commits) {
        
        Map<String, List<CommitItem>> ret = new HashMap<>();
        for (CommitItem currCommit : commits) {
            String currProtectedBranch = currCommit.getBranchDescriptor().getProtectedBranch();
            List<CommitItem> currProtectedBranchCommits = ret.get(currProtectedBranch);
            if (currProtectedBranchCommits == null) {
                currProtectedBranchCommits = new ArrayList<>();
                ret.put(currProtectedBranch, currProtectedBranchCommits);
            }
            currProtectedBranchCommits.add(currCommit);
        }
        
        return ret.values();
    }
}
