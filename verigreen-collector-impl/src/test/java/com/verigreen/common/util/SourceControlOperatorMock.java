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

import java.util.List;

import org.easymock.EasyMock;

import com.verigreen.collector.api.BranchDescriptor;
import com.verigreen.common.utils.Pair;
import com.verigreen.jgit.SourceControlOperator;

public class SourceControlOperatorMock implements SourceControlOperator {
    
    SourceControlOperator _mock = EasyMock.createNiceMock(SourceControlOperator.class);
    
    @Override
    public String createBranch(String commitId, String branchName) {
        
        return _mock.createBranch(commitId, branchName);
    }
    
    @Override
    public String fetch() {
        
        return _mock.fetch();
    }
    
    @Override
    public String fetch(String localBranchName, String remoteBranchName) {
        
        return _mock.fetch(localBranchName, remoteBranchName);
    }
    
    @Override
    public String checkout(
            String branchName,
            boolean useBranchNameAsStartPoint,
            boolean createBranchIfNotExists,
            boolean useForce) {
        
        return _mock.checkout(
                branchName,
                useBranchNameAsStartPoint,
                createBranchIfNotExists,
                useForce);
    }
    
    @Override
    public String checkout(String branchName, boolean createBranchIfNotExists, boolean useForce) {
        
        return _mock.checkout(branchName, createBranchIfNotExists, useForce);
    }
    
    @Override
    public boolean rebase(String upStreamBranchName) {
        
        return _mock.rebase(upStreamBranchName);
    }
    
    @Override
    public boolean push(String sourceBranch, String destinationBranch) {
        
        return _mock.push(sourceBranch, destinationBranch);
    }
    
    @Override
    public void deleteBranch(String... branchesToDelete) {
        
        _mock.deleteBranch(branchesToDelete);
    }
    
    public void setMockBehaviour(
            BranchDescriptor branchDescriptor,
            String mergedBranch,
            boolean mergeResult,
            boolean createBranchThrowsException,
            boolean updateBranchWithParentThrowsException) {
        
        String branch = branchDescriptor.getNewBranch();
        String protectedBranch = branchDescriptor.getProtectedBranch();
        EasyMock.expect(_mock.isBranchExist(EasyMock.anyString())).andReturn(false);
        if (createBranchThrowsException) {
            EasyMock.expect(_mock.createBranch(EasyMock.anyString(), EasyMock.anyString())).andThrow(
                    new RuntimeException("create branch failed"));
        } else {
            EasyMock.expect(_mock.createBranch(EasyMock.anyString(), EasyMock.anyString())).andReturn(
                    mergedBranch);
        }
        EasyMock.expect(_mock.checkout(branch, true, true, true)).andReturn(branch);
        EasyMock.expect(_mock.checkout(protectedBranch, false, false)).andReturn(protectedBranch);
        EasyMock.expect(_mock.checkout(mergedBranch, false, false)).andReturn(mergedBranch);
        EasyMock.expect(_mock.merge(EasyMock.anyString(), EasyMock.anyString())).andReturn(
                new Pair<Boolean, String>(mergeResult, "")).times(2);
        EasyMock.expect(_mock.push(protectedBranch, protectedBranch)).andReturn(true);
        EasyMock.expect(_mock.push(mergedBranch, mergedBranch)).andReturn(true).atLeastOnce();
        if (updateBranchWithParentThrowsException) {
            EasyMock.expect(
                    _mock.updateBranchWithParent(
                            EasyMock.anyString(),
                            EasyMock.anyString(),
                            EasyMock.eq(false),
                            EasyMock.eq(false),
                            EasyMock.eq(false))).andThrow(new RuntimeException("update failed")).times(
                    2);
        } else {
            EasyMock.expect(
                    _mock.updateBranchWithParent(
                            EasyMock.anyString(),
                            EasyMock.anyString(),
                            EasyMock.eq(false),
                            EasyMock.eq(false),
                            EasyMock.eq(false))).andReturn(
                    new Pair<Boolean, String>(mergeResult, "")).times(2);
        }
        EasyMock.expect(_mock.getPathOfLocalRepository()).andReturn("");
        EasyMock.expect(_mock.commit("", "", "")).andReturn("");
        _mock.add("");
        EasyMock.expectLastCall();
        _mock.deleteBranch();
        EasyMock.expectLastCall();
    }
    
    @Override
    public String getPathOfLocalRepository() {
        
        return _mock.getPathOfLocalRepository();
    }
    
    @Override
    public void add(String itemToAdd) {
        
        _mock.add(itemToAdd);
    }
    
    @Override
    public String commit(String author, String email, String message) {
        
        return _mock.commit(author, email, message);
    }
    
    @Override
    public Pair<Boolean, String> updateBranchWithParent(
            String branchToUpdate,
            String parentBranch,
            boolean useBranchNameAsStartPoint,
            boolean createBranchIfNotExists,
            boolean useForce) {
        
        return _mock.updateBranchWithParent(
                branchToUpdate,
                parentBranch,
                useBranchNameAsStartPoint,
                createBranchIfNotExists,
                useForce);
    }
    
    @Override
    public boolean isThereAnyDifs() {
        
        return _mock.isThereAnyDifs();
    }
    
    @Override
    public boolean isBranchContainsCommit(String brnachName, String commitId) {
        
        return _mock.isBranchContainsCommit(brnachName, commitId);
    }
    
    public void replay() {
        
        EasyMock.replay(_mock);
    }
    
    public void reset() {
        
        EasyMock.reset(_mock);
    }
    
    @Override
    public boolean isBranchExist(String branch) {
        
        return _mock.isBranchExist(branch);
    }
    
    @Override
    public Pair<Boolean, String> merge(String branchToUpdate, String branchHead) {
        
        return _mock.merge(branchToUpdate, branchHead);
    }
    
    @Override
    public String getLocalBranchesRoot() {
        return "refs/heads/";
    }
    
    @Override
    public String getRemoteBranchesRoot() {
        return "refs/remotes/origin/";
    }

	@Override
	public List<String> retreiveBranches() {
		return _mock.retreiveBranches();
	}
}
