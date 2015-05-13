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
package com.verigreen.jgit;

import java.util.List;

import com.verigreen.common.utils.Pair;

public interface SourceControlOperator {
    
    String createBranch(String commitId, String branchName);
    
    String fetch();
    
    String fetch(String localBranchName, String remoteBranchName);
    
    /**
     * Checkout a branch
     * 
     * @param branchName
     *            The branch name
	 * @param useBranchNameAsStartPoint
	 *            Whether to use the branch as starting point.
     * @param createBranch
     *            Whether to create a branch or not. Will fail if set to true, and branch already
     *            exist
     * @param useForce
     *            if true and the branch with the given name already exists, the start-point of an
     *            existing branch will be set to a new start-point; if false, the existing branch
     *            will not be changed
     * @return The full path to the branch
     */
    String checkout(
            String branchName,
            boolean useBranchNameAsStartPoint,
            boolean createBranch,
            boolean useForce);
    
    /**
     * Checkout a branch
     * 
     * @param branchName
     *            The branch name
     * @param createBranch
     *            Whether to create a branch or not. Will fail if set to true, and branch already
     *            exist
     * @param useForce
     *            if true and the branch with the given name already exists, the start-point of an
     *            existing branch will be set to a new start-point; if false, the existing branch
     *            will not be changed
     * @return The full path to the branch
     */
    String checkout(String branchName, boolean createBranch, boolean useForce);
    
    boolean rebase(String upStreamBranchName);
    
    boolean push(String sourceBranch, String destinationBranch);
    
    Pair<Boolean, String> merge(String branchToUpdate, String branchHead);
    
    void deleteBranch(String... branchesToDelete);
    
    Pair<Boolean, String> updateBranchWithParent(
            String branchToUpdate,
            String parentBranch,
            boolean useBranchNameAsStartPoint,
            boolean createBranchIfNotExists,
            boolean useForce);
    
    String getPathOfLocalRepository();
    
    /**
     * @return The root of the branches in the local repository
     */
    String getLocalBranchesRoot();
    
    /**
     * @return The root of the branches on the remote repository
     */
    String getRemoteBranchesRoot();
    
    void add(String itemToAdd);
    
    String commit(String author, String email, String message);
    
    boolean isThereAnyDifs();
    
    boolean isBranchContainsCommit(String brnachName, String commitId);
    
    boolean isBranchExist(String branch);
    
    List<String> retreiveBranches();
}