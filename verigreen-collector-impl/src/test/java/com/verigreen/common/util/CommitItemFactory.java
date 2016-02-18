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
package com.verigreen.common.util;

import com.verigreen.collector.api.BranchDescriptor;
import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.utils.StringUtils;

public class CommitItemFactory {

    private CommitItemFactory() {
    }

    public static CommitItem create(VerificationStatus status, String protectedBranch) {
        
        return create(
                status,
                StringUtils.EMPTY_STRING,
                null,
                true,
                false,
                false,
                true,
                true,
                protectedBranch);
    }
    
    public static CommitItem create(String branch, boolean shouldReset, boolean shouldReplay) {
        
        return create(VerificationStatus.NOT_STARTED, branch, null, shouldReset, shouldReplay);
    }
    
    public static CommitItem create(VerificationStatus status) {
        
        return create(status, StringUtils.EMPTY_STRING, null, true, true);
    }
    
    public static CommitItem create(VerificationStatus status, String branch, CommitItem item) {
        
        return create(status, branch, item, true, true);
    }
    
    public static CommitItem create(
            VerificationStatus status,
            String branch,
            CommitItem item,
            boolean shouldReset,
            boolean shouldReplay) {
        
        return create(status, branch, item, true, false, false, true, true, "refs/heads/master");
    }
    
    public static CommitItem create(
            VerificationStatus status,
            String branch,
            CommitItem item,
            boolean mergeSuccess,
            boolean createBranchThrowsException,
            boolean updateBranchThrowsException,
            boolean shouldReset,
            boolean shouldReplay,
            String protectedBranch) {
        
        BranchDescriptor branchDescriptor = new BranchDescriptor();
        branchDescriptor.setNewBranch(branch);
        branchDescriptor.setProtectedBranch(protectedBranch);
        CommitItem ret = new CommitItem(branchDescriptor);
        ret.setStatus(status);
        ret.setParent(item);
        CollectorApi.getCommitItemContainer().save(ret);
        setMockBehaviour(
                ret,
                mergeSuccess,
                createBranchThrowsException,
                updateBranchThrowsException,
                shouldReset,
                shouldReplay);
        
        return ret;
    }
    
    public static CommitItem create(VerificationStatus status, CommitItem item) {
        
        return create(status, "", item);
    }
    
    private static void setMockBehaviour(
            CommitItem item,
            boolean mergeSuccess,
            boolean createBranchThrowsException,
            boolean updateBranchWithParentThrowsException,
            boolean shouldReset,
            boolean shouldReplay) {
        
        SourceControlOperatorMock sourceControlOperator =
                (SourceControlOperatorMock) CollectorApi.getSourceControlOperator();
        if (shouldReset) {
            sourceControlOperator.reset();
        }
        sourceControlOperator.setMockBehaviour(
                item.getBranchDescriptor(),
                item.getMergedBranchName(),
                mergeSuccess,
                createBranchThrowsException,
                updateBranchWithParentThrowsException);
        if (shouldReplay) {
            sourceControlOperator.replay();
        }
    }
}
