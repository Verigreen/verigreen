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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;

import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.cache.container.CommitItemContainer;
import com.verigreen.collector.common.CommitItemUtils;
import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.collector.decision.Decision;
import com.verigreen.collector.decision.OnFailureHandler;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.concurrency.RuntimeUtils;
import com.verigreen.common.utils.CollectionUtils;

public class DecisionMakerFailedItems {
    
    public List<Decision> execute(Collection<CommitItem> items) {
        
        List<Decision> ret = new ArrayList<>();
        Collection<CommitItem> failedItems = getFailedAndRefresh(items);
        if (!CollectionUtils.isNullOrEmpty(failedItems)) {
            for (CommitItem currItem : failedItems) {
                if (!isPending(currItem.getParent())) {
                    ret.addAll(handleItem(currItem));
                }
            }
        }
        
        return ret;
    }
    
    private boolean isPending(CommitItem item) {
        
        if (item == null || item.isDone()) {
            
            return false;
        }
        if (item.getStatus().equals(VerificationStatus.RUNNING)) {
            
            return true;
        }
        
        return isPending(item.getParent());
    }
    
    private List<Decision> handleItem(CommitItem item) {
        
        List<Decision> ret = new ArrayList<>();
        CommitItem parent = item.getParent();
        CommitItem houseOfCardsStart = item;
        if (parent == null || parent.getStatus().equals(VerificationStatus.PASSED)) {
            item.setDone(true);
            ret.add(new Decision(item.getKey(), new OnFailureHandler(item)));
            houseOfCardsStart = item.getChild();
            CollectorApi.getCommitItemContainer().save(item);
            houseOfCards(houseOfCardsStart, ret);
        }
        
        return ret;
    }
    
    /**
     * If an item has failed - reset all of its running/failed children to not started
     */
    private void houseOfCards(CommitItem item, List<Decision> decisions) {
        
        if (item == null || item.isDone()) {
            
            return;
        }
        if (item.getStatus().equals(VerificationStatus.PASSED)) {
            
            return;
        }
        if (item.getStatus().equals(VerificationStatus.RUNNING)) {
            decisions.add(new Decision(item.getKey(), CollectorApi.getOnFailedByParentHandler(item)));
        }
        VerigreenLogger.get().log(
                getClass().getName(),
                RuntimeUtils.getCurrentMethodName(),
                String.format("Setting commit item status to not started (%s)", item));
        
        try {
			CommitItemUtils.createJsonFile(item,true);
		} catch (JSONException e) {
			VerigreenLogger.get().error(
					getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format("Failed creating JSON object",
                    e));
		} catch (IOException e) {
			VerigreenLogger.get().error(
					getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format("Failed creating json file: " + System.getenv("VG_HOME") + "\\history.json",
                    e));
		}
        
        item.setStatus(VerificationStatus.NOT_STARTED);
        houseOfCards(item.getChild(), decisions);
        
        item.setParent(null);
        item.setChild(null);
        CollectorApi.getCommitItemContainer().save(item);
    }
    
    private Collection<CommitItem> getFailedAndRefresh(Collection<CommitItem> items) {
        
        Collection<CommitItem> ret = new ArrayList<>();
        CommitItemContainer commitItemContainer = CollectorApi.getCommitItemContainer();
        for (CommitItem currItem : items) {
            if (currItem.getStatus().isFailureState()) {
                ret.add(commitItemContainer.get(currItem.getKey()));
            }
        }
        
        return ret;
    }
}
