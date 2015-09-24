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
import com.verigreen.collector.common.CommitItemUtils;
import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.collector.decision.Decision;
import com.verigreen.collector.decision.OnSuccessHandler;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.concurrency.RuntimeUtils;
import com.verigreen.common.utils.CollectionUtils;

public class DecisionMakerPassedItems {
    
    public List<Decision> execute(Collection<CommitItem> items) {

    	items = CommitItemUtils.refreshItems(items);
    	List<Decision> ret = new ArrayList<>();

        Collection<CommitItem> passedItems =
                CommitItemUtils.filterItems(items, VerificationStatus.PASSED);
       Collection<CommitItem> forcedItems = 
       	 CommitItemUtils.filterItems(items, VerificationStatus.FORCING_PUSH);
       /**
        * Add the commitItems which have the PASSED status, generating Decision and onSuccesHandler
        * for those items.
        * */
       if (!CollectionUtils.isNullOrEmpty(passedItems)) {
           	for (CommitItem currItem : passedItems) {
               ret.addAll(houseOfCards(currItem));
               ret.add(new Decision(currItem.getKey(), new OnSuccessHandler(currItem)));
               currItem.setDone(true);
               CollectorApi.getCommitItemContainer().save(currItem);
           }
       	}
       /**
        * Add the commitItems which have the FORCING_PUSH status, generating Decision and onSuccesHandler
        * for those items.
        * */
        if (!CollectionUtils.isNullOrEmpty(forcedItems)) {
            for (CommitItem currItem : forcedItems) {
                ret.addAll(houseOfCards(currItem));
                ret.add(new Decision(currItem.getKey(), new OnSuccessHandler(currItem)));
                currItem.setDone(true);
                CollectorApi.getCommitItemContainer().save(currItem);
            }
        }
        
        return ret;
    }
    
    private List<Decision> houseOfCards(CommitItem item) {
        
        // mark all ancestors as done, since if a child has passed - 
        // all ancestors are passed by it (house of cards)
        List<Decision> ret = new ArrayList<>();
        while ((item = item.getParent()) != null) {
            if (!item.isDone() && !item.getStatus().equals(VerificationStatus.MERGE_FAILED) && !item.getStatus().equals(VerificationStatus.GIT_FAILURE)) {
                VerigreenLogger.get().log(
                        getClass().getName(),
                        RuntimeUtils.getCurrentMethodName(),
                        String.format("Setting commit item as done (%s)", item));
                item.setDone(true); 
                ret.add(new Decision(item.getKey(), CollectorApi.getOnSuccessByChildHandler(item)));
                CollectorApi.getCommitItemContainer().save(item);
            }
            if(item.getStatus().equals(VerificationStatus.FAILED))
            {
            	VerigreenLogger.get().log(
                        getClass().getName(),
                        RuntimeUtils.getCurrentMethodName(),
                        String.format("Setting failed item as done (%s)", item));
            	  item.setDone(true);
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
            	item.setStatus(VerificationStatus.PASSED);
                ret.add(new Decision(item.getKey(), CollectorApi.getOnSuccessByChildHandler(item)));
                CollectorApi.getCommitItemContainer().save(item);
            }
        }
        
        return ret;
    }
}
