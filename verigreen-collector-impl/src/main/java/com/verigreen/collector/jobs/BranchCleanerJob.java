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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.verigreen.collector.common.VerigreenNeededLogic;
import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.concurrency.RuntimeUtils;
import com.verigreen.common.spring.SpringContextHolder;
import com.verigreen.common.utils.CollectionUtils;
import com.verigreen.jgit.JGitOperator;

@DisallowConcurrentExecution
public class BranchCleanerJob implements Job{
	
	private JGitOperator _jgit;
	 
    public void setbranchCleanDaysThreashold(int branchCleanDaysThreashold) {
		VerigreenNeededLogic.VerigreenMap.put("branchCleanDaysThreashold", String.valueOf(branchCleanDaysThreashold));
	}

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
    	VerigreenLogger.get().log(
                getClass().getName(),
                RuntimeUtils.getCurrentMethodName(),
                String.format(
                        "Branch Cleaner is running, deleting branches %s days old...", 
                        VerigreenNeededLogic.VerigreenMap.get("branchCleanDaysThreashold")));
        deleteBranches();
    }

	private void deleteBranches() {
		_jgit = (JGitOperator)SpringContextHolder.getInstance().getBean("jgitOperator");
		List <String> branchesToBeDeleteList = branchesToBeDelete(_jgit.getBranchesList(_jgit.retreiveBranches()));
		if(!CollectionUtils.isNullOrEmpty(branchesToBeDeleteList))
			_jgit.deleteBranch(true,(branchesToBeDeleteList.toArray(new String[branchesToBeDeleteList.size()])));
	}

	private List <String> branchesToBeDelete(List <String> branchesList){
		List<String> result = new ArrayList<String>();
		Repository repo = null;
		Map<String, List <String>> branchesMap = new HashMap<String, List <String>>();
		for (String branch : branchesList) {
			List<String> values = null;
			if (!branchesMap.containsKey(branch.subSequence(0, 10))){
				values = new ArrayList<String>();
				values.add(branch);
				branchesMap.put(branch.subSequence(0, 10).toString(), values);
			}
			else if(!branchesMap.get(branch.subSequence(0, 10)).contains(branch)){
					values = branchesMap.get(branch.subSequence(0, 10));
					values.add(branch);
					branchesMap.put(branch.subSequence(0, 10).toString(),values);
				}
		}
		List<CommitItem> allBranches = CollectorApi.getCommitItemContainer().getAll();
		for (CommitItem branch : allBranches) {
				if (branchesMap.containsKey(branch.getBranchDescriptor().getNewBranch().subSequence(0, 10))) {
					branchesMap.remove(branch.getBranchDescriptor().getNewBranch().subSequence(0, 10));
				}
		}	
		for(String key : branchesMap.keySet()) {
				result.addAll(branchesMap.get(key));
		}
		
		try {
			repo = new FileRepository(VerigreenNeededLogic.properties.getProperty("git.repositoryLocation"));
			if(result.contains(repo.getBranch())) {
				_jgit.checkout(VerigreenNeededLogic.VerigreenMap.get("_protectedBranches"), false, false);
			}
		} catch (IOException e) {
			 VerigreenLogger.get().error(
	                    getClass().getName(),
	                    RuntimeUtils.getCurrentMethodName(),
	                    String.format("Failed creating git repository for path [%s]",VerigreenNeededLogic.properties.getProperty("git.repositoryLocation")),
	                    e);
		}
		
		return result;
	}	
}
