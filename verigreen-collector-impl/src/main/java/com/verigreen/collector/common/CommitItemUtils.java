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
package com.verigreen.collector.common;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.cache.container.CommitItemContainer;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.jbosscache.Criteria;
import com.verigreen.common.utils.CollectionUtils;
import com.verigreen.common.utils.LocalMachineCurrentTimeProvider;

public class CommitItemUtils {

	public static Collection<CommitItem> filterItems(
            Collection<CommitItem> items,
            final VerificationStatus state) {
        
        return CollectionUtils.isNullOrEmpty(items)
                ? new ArrayList<CommitItem>()
                : Collections2.filter(items, new Predicate<CommitItem>() {
                    
                    @Override
                    public boolean apply(CommitItem input) {
                        
                        return input != null ? input.getStatus().equals(state) : false;
                    }
                });
    }
    
    public static Collection<CommitItem> getNotDone() {
        
        List<CommitItem> ret =
                CollectorApi.getCommitItemContainer().findByCriteria(new Criteria<CommitItem>() {
                    
                    @Override
                    public boolean match(CommitItem entity) {
                        
                        return !entity.isDone();
                    }
                });
        Collections.sort(ret);
        
        return ret;
    }
    
    public static Collection<CommitItem> getRunning() {
        
        List<CommitItem> ret =
                CollectorApi.getCommitItemContainer().findByCriteria(new Criteria<CommitItem>() {
                    
                    @Override
                    public boolean match(CommitItem entity) {
                        
                        return entity.getStatus().equals(VerificationStatus.RUNNING);
                    }
                });
        
        return ret;
    }
    
    public static Collection<CommitItem> filterNotDone(Collection<CommitItem> items) {
        
        return Collections2.filter(items, new Predicate<CommitItem>() {
            
            @Override
            public boolean apply(CommitItem input) {
                
                return !input.isDone();
            }
        });
    }
    
    public static Collection<CommitItem> refreshItems(Collection<CommitItem> items) {
        
        List<CommitItem> ret = new ArrayList<>();
        CommitItemContainer commitItemContainer = CollectorApi.getCommitItemContainer();
        for (CommitItem commitItem : items) {
            ret.add(commitItemContainer.get(commitItem.getKey()));
        }
        
        return ret;
    }
    
    public static void createJsonFile(CommitItem localCommitItem, boolean canceledItem) throws JSONException, IOException {
		List<JSONObject> objectList = new ArrayList<JSONObject>();
		JSONObject jsonObject = new JSONObject();
		String parent = localCommitItem.getParent() !=null ? localCommitItem.getParent().getBranchDescriptor().getNewBranch()
                : localCommitItem.getBranchDescriptor().getProtectedBranch();
		FileWriter file;
			jsonObject.put("commitId", localCommitItem.getBranchDescriptor().getNewBranch());
			jsonObject.put("parentBranch",parent);
			jsonObject.put("committer", localCommitItem.getBranchDescriptor().getCommitter());
			if(localCommitItem.getBuildUrl()!=null && !checkIfBuildUrlExist(localCommitItem)) {
					jsonObject.put("buildUrl", localCommitItem.getBuildUrl().toString());
			}
			else  {
				jsonObject.put("buildUrl", "");
			}
			if(canceledItem == false) {
				jsonObject.put("status", localCommitItem.getStatus().name());
				if(localCommitItem.getEndTime()!=null) {
					jsonObject.put("endTime", localCommitItem.getEndTime().getTime());
				} else {
					jsonObject.put("endTime", new Date(0).getTime());
				}
				
			} else {
				if(localCommitItem.getStatus().equals(VerificationStatus.NOT_STARTED) || localCommitItem.getStatus().equals(VerificationStatus.RUNNING)){
					jsonObject.put("status", "CANCELED");
					jsonObject.put("endTime", new LocalMachineCurrentTimeProvider().getCurrentTime().getTime());
				} else {
					jsonObject.put("status", localCommitItem.getStatus().name());
					jsonObject.put("endTime", new LocalMachineCurrentTimeProvider().getCurrentTime().getTime());
				}
			}
		objectList.add(jsonObject);
		if(VerigreenNeededLogic.history.containsKey(localCommitItem.getBranchDescriptor().getNewBranch())) {
			for (String key : VerigreenNeededLogic.history.keySet()) {
				if(key.equals(localCommitItem.getBranchDescriptor().getNewBranch())) {
					objectList.addAll(VerigreenNeededLogic.history.get(key));
					VerigreenNeededLogic.history.put(key, objectList);
				}		
			}
		} else {
			VerigreenNeededLogic.history.put(localCommitItem.getBranchDescriptor().getNewBranch(), objectList);
		}
			file = new FileWriter(System.getenv("VG_HOME") + "//history.json");
			JSONObject history = new JSONObject(VerigreenNeededLogic.history);
			file.write(history.toString());
			file.flush();
			file.close();
	}
    
    private static boolean checkIfBuildUrlExist(CommitItem localCommitItem) throws JSONException {
		List<JSONObject> jsonList = VerigreenNeededLogic.history.get(localCommitItem.getBranchDescriptor().getNewBranch());
		if(jsonList != null) { 	
			for (JSONObject jobj : jsonList) {
				if(jobj.get("buildUrl").equals(localCommitItem.getBuildUrl().toString())) {
							return true;
				}
			}
		}
		return false;
	}
	
}
