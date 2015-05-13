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

public class OnFailureHandler extends DecisionHandler {
    
    public OnFailureHandler(CommitItem item) {
        
        super(item);
    }
    
    @Override
    protected void doHandle() {
        
        VerigreenLogger.get().log(
                getClass().getName(),
                RuntimeUtils.getCurrentMethodName(),
                String.format(
                        "Commit (%s) verification completed with failure (%s)",
                        _commitItem.getBranchDescriptor().getNewBranch(),
                        _commitItem.getStatus()));
        if(_commitItem.getStatus().equals(VerificationStatus.MERGE_FAILED)) {
        	CollectorApi.getEmailSender().notifyCommiter(
                    _commitItem.getBranchDescriptor().getCommitId(),
                    _commitItem.getStatus(),
                    _commitItem.getBuildUrl(),
                    "Verigreen Status - Failure",
                    EmailSender.getFailedSignature(),
                    _commitItem.getBranchDescriptor().getCommitter(),
                    _commitItem.getBranchDescriptor().getProtectedBranch(),
                    _commitItem.getParent().getBranchDescriptor().getCommitId(),
                    _commitItem.getMergedBranchName()
                    );
        }
        else {
        	CollectorApi.getEmailSender().notifyCommiter(
                    _commitItem.getBranchDescriptor().getCommitId(),
                    _commitItem.getStatus(),
                    _commitItem.getBuildUrl(),
                    "Verigreen Status - Failure",
                    EmailSender.getFailedSignature(),
                    _commitItem.getBranchDescriptor().getCommitter(),
                    _commitItem.getBranchDescriptor().getProtectedBranch(),
                    _commitItem.getMergedBranchName()
                    );
        }
    }
}
