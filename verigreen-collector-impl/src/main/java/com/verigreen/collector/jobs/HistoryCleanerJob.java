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

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.verigreen.collector.common.VerigreenNeededLogic;
import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.concurrency.RuntimeUtils;

@DisallowConcurrentExecution 
public class HistoryCleanerJob implements Job {
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
	    	VerigreenLogger.get().log(
	                getClass().getName(),
	                RuntimeUtils.getCurrentMethodName(),
	                String.format(
	                        "History Cleaner is running, deleting items %s days old...", 
	                        VerigreenNeededLogic.VerigreenMap.get("daysThreashold")));
	        getHistory();
	}

	private void getHistory() {
		Map<String,List<JSONObject>> newHistory = new HashMap<>();
		List<CommitItem> commitItems = CollectorApi.getCommitItemContainer().getAll();
		
		for (CommitItem commit : commitItems) {
			if (VerigreenNeededLogic.history.containsKey(commit.getBranchDescriptor().getNewBranch())) {
					newHistory.put(commit.getBranchDescriptor().getNewBranch(), VerigreenNeededLogic.history.get(commit.getBranchDescriptor().getNewBranch()));
				}
		}
		
		if(!VerigreenNeededLogic.history.equals(newHistory)) {
			updateHistory(newHistory);
		}
		
	}
	
	private void updateHistory(Map<String,List<JSONObject>> newHistory) {
		FileWriter file;
		VerigreenNeededLogic.history = newHistory;
		try {
			file = new FileWriter(System.getenv("VG_HOME") + "//history.json");
			JSONObject history = new JSONObject(VerigreenNeededLogic.history);
			file.write(history.toString());
			file.flush();
			file.close();
		} catch (IOException e) {
			VerigreenLogger.get().error(
				    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format("Failed updating json file: " + System.getenv("VG_HOME") + "\\history.json",
                    e));
		}
	}
}
