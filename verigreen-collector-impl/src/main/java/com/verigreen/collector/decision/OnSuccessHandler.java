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

import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.common.EmailSender;
import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.concurrency.RuntimeUtils;
import com.verigreen.common.utils.Pair;
import com.verigreen.common.utils.StringUtils;
import com.verigreen.jgit.SourceControlOperator;

public class OnSuccessHandler extends DecisionHandler {
    
    public OnSuccessHandler(CommitItem item) {
        
        super(item);
    }
    
    @Override
    protected void doHandle() {
        
        if (commitSuccessfullCheckin()) {
        	 
        	if(_commitItem.getStatus().equals(VerificationStatus.FORCING_PUSH))
        	{
        		_commitItem.setStatus(VerificationStatus.FAILED_AND_PUSHED);
        		CollectorApi.getEmailSender().notifyCommiter(
        				_commitItem.getBranchDescriptor().getCommitId(),
        				_commitItem.getStatus(),
        				_commitItem.getBuildUrl(),
        				"Verigreen Status - Success (Build Failed)",
        				EmailSender.getFailedPushSignature(),
        				_commitItem.getBranchDescriptor().getCommitter(),
        				_commitItem.getBranchDescriptor().getProtectedBranch(),
        				_commitItem.getMergedBranchName());

        	}
        	else{
        		_commitItem.setStatus(VerificationStatus.PASSED_AND_PUSHED);
        		CollectorApi.getEmailSender().notifyCommiter(
        				_commitItem.getBranchDescriptor().getCommitId(),
        				_commitItem.getStatus(),
        				_commitItem.getBuildUrl(),
        				"Verigreen Status - Success",
        				EmailSender.getSuccessSignature(),
        				_commitItem.getBranchDescriptor().getCommitter(),
        				_commitItem.getBranchDescriptor().getProtectedBranch(),
        				_commitItem.getMergedBranchName());
        	}
        	CollectorApi.getCommitItemContainer().save(_commitItem);
        } else {
            _commitItem.setDone(false);
            CollectorApi.getCommitItemContainer().save(_commitItem);
        }
    }
    
    private boolean commitSuccessfullCheckin() {
        
        boolean ret = false;
        try {
            VerigreenLogger.get().log(
                    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format("Commit is successfull, commiting... %s", _commitItem));
            SourceControlOperator srcControlOperator = CollectorApi.getSourceControlOperator();
            // update branch in case a permitted user checked in...
            Pair<Boolean, String> result =
                    srcControlOperator.updateBranchWithParent(
                            _commitItem.getMergedBranchName(),
                            _commitItem.getBranchDescriptor().getProtectedBranch(),
                            false,
                            false,
                            false);
            if (result.getFirst()) {
                if (!StringUtils.isNullOrEmpty(result.getSecond())) {
                    _commitItem.setChildCommit(result.getSecond());
                    CollectorApi.getCommitItemContainer().save(_commitItem);
                }
                ret = push(srcControlOperator);
            } else {
                _commitItem.setStatus(VerificationStatus.MERGE_FAILED);
                CollectorApi.getCommitItemContainer().save(_commitItem);
            }
        } catch (Exception e) {
            VerigreenLogger.get().error(
                    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format(
                            "Failed updating protected branch with successful checkin [%s]",
                            _commitItem.getMergedBranchName()),
                    e);
            _commitItem.setStatus(VerificationStatus.GIT_FAILURE);
            CollectorApi.getCommitItemContainer().save(_commitItem);
        }
        
        return ret;
    }
    
    private boolean push(SourceControlOperator srcControlOperator) {
        
        boolean ret = true;
        /*switch to the protected branch -> merge the changes from the merged branch and push to the protected branch*/
        String protectedBranch = _commitItem.getBranchDescriptor().getProtectedBranch();
        srcControlOperator.checkout(protectedBranch, false, false);
        Pair<Boolean, String> result =
                srcControlOperator.merge(protectedBranch, _commitItem.getMergedBranchName());
        if (!result.getFirst() || !srcControlOperator.push(protectedBranch, protectedBranch)) {
            _commitItem.setStatus(VerificationStatus.GIT_FAILURE);
            CollectorApi.getCommitItemContainer().save(_commitItem);
            ret = false;
        } else {
            deleteVerificationBranches(srcControlOperator);
        }
        
        return ret;
    }
    
    private void deleteVerificationBranches(SourceControlOperator srcControlOperator) {
        
        try {
            srcControlOperator.deleteBranch(
                    _commitItem.getBranchDescriptor().getNewBranch(),
                    _commitItem.getMergedBranchName());
        } catch (Exception e) {
            VerigreenLogger.get().error(
                    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format(
                            "Failed deleting temp branches [%s], [%s]",
                            _commitItem.getBranchDescriptor().getNewBranch(),
                            _commitItem.getMergedBranchName()),
                    e);
        }
    }
}
