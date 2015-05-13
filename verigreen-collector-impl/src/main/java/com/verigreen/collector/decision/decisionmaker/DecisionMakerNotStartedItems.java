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
import java.util.List;

import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.common.CommitItemUtils;
import com.verigreen.collector.decision.Decision;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.utils.CollectionUtils;

public class DecisionMakerNotStartedItems {
    
    public List<Decision> execute(Collection<CommitItem> items) {
        
        List<Decision> ret = new ArrayList<>();
        items = CommitItemUtils.refreshItems(items);
        if (!isPending(items)) {
            Collection<CommitItem> notStartedItems =
                    CommitItemUtils.filterItems(items, VerificationStatus.NOT_STARTED);
            if (!CollectionUtils.isNullOrEmpty(notStartedItems)) {
                CommitItem currParent = getParent(items, notStartedItems.iterator().next());
                for (CommitItem currItem : notStartedItems) {
                    currItem.setParent(currParent);
                    ret.add(new Decision(
                            currItem.getKey(),
                            CollectorApi.getCheckinDecisionHandler(currItem)));
                    currParent = currItem;
                    CollectorApi.getCommitItemContainer().save(currItem);
                }
            }
        }
        
        return ret;
    }
    
    private boolean isPending(Collection<CommitItem> items) {
        
        boolean ret = false;
        for (CommitItem currItem : items) {
            if (!currItem.isDone() && currItem.getStatus().isFailureState()) {
                ret = true;
                break;
            }
        }
        
        return ret;
    }
    
    private CommitItem getParent(Collection<CommitItem> items, CommitItem first) {
        
        CommitItem ret = null;
        for (CommitItem currItem : items) {
            if (currItem.equals(first)) {
                break;
            }
            if (currItem.getStatus().equals(VerificationStatus.PASSED)
                || currItem.getStatus().equals(VerificationStatus.RUNNING)) {
                ret = currItem;
            }
        }
        
        return ret;
    }
}
