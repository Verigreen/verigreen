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
package com.verigreen.collector.systemtest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.api.VerigreenUtils;
import com.verigreen.jgit.JGitOperator;

@Category(SystemTests.class)
public class TestCollectorE2EFailures extends SystemTestBase {
	  
    @Test
    public void testMergeFailure() throws IOException, InterruptedException {
        final int NUM_OF_USERS = 3;
        
        long timeoutForTestInMilis = 1000 * 60 * 5;
        String destinationFolderForNewFile = DEFAULT_COMMIT_FOLDER;
        
        String[] userNames = generateUserNames(NUM_OF_USERS);
        String[] emails = generateEmailAddresses(NUM_OF_USERS);
        
        setProtectedBranch(_masterBranch);
        checkoutExistingBranch(_masterBranch);
        
        String newFileName = generateFileName();
        
        addNewFile(
                destinationFolderForNewFile + "/" + newFileName,
                "This is line number 1 added by: " + userNames[0]);
        
        String commit1Message = String.format("user %s added file %s", userNames[0], newFileName);
        String commit1Id =
                commitAndPush(
                        destinationFolderForNewFile,
                        userNames[0],
                        emails[0],
                        commit1Message,
                        true,
                        _masterBranch);
        checkSuccessfulyProccesedCommitThatShouldPass(
                VerigreenUtils.getVerigreenBranchName(processCommitId(commit1Id, userNames[0])),
                _masterBranch,
                timeoutForTestInMilis,
                commit1Id,
                userNames[0],
                emails[0],
                commit1Message,
                true);
        
        String pathToFile = constructLocalFilePath(destinationFolderForNewFile, newFileName);
        
        String masterBrnachCurrentSHA1 =
                ((JGitOperator) _sourceControlOperator).getRef(
                        getBranchRefsRemotesFullName(_masterBranch)).getObjectId().getName();
        
        writeToLocalFileInBeginning(
                pathToFile,
                String.format("This is line number 2 added by: %s", userNames[1]));
        
        String commit2Message =
                String.format("user %s commits in same file %s", userNames[1], newFileName);
        String commit2Id =
                commitAndPush(
                        destinationFolderForNewFile,
                        userNames[1],
                        emails[1],
                        commit2Message,
                        false,
                        _masterBranch);
        
        ((JGitOperator) _sourceControlOperator).reset(masterBrnachCurrentSHA1);
        
        appendToFile(pathToFile, newFileName);
        String commitMessage3 =
                String.format("user %s commits in same file %s", userNames[2], newFileName);
        
        String commit3Id =
                commitAndPush(
                        destinationFolderForNewFile,
                        userNames[2],
                        emails[2],
                        commitMessage3,
                        false,
                        _masterBranch);
        
        checkSuccessfulyProccesedCommitThatShouldPass(
                VerigreenUtils.getVerigreenBranchName(processCommitId(commit2Id, userNames[1])),
                _masterBranch,
                timeoutForTestInMilis,
                commit2Id,
                userNames[1],
                emails[1],
                commit2Message,
                true);
        
        checkSuccessfulyProccesedCommitThatShouldFail(
                VerigreenUtils.getVerigreenBranchName(processCommitId(commit3Id, userNames[2])),
                _masterBranch,
                timeoutForTestInMilis,
                commit3Id,
                userNames[2],
                emails[2],
                commitMessage3,
                VerificationStatus.MERGE_FAILED,
                true);
    }
    
    /**
     * User 1 commits and pushes one file. User 2 adds a line in the same file at the beginning, and
     * user 3 adds a line at the end. Both users are committing and pushing. Failure should occur
     * 
     * All this in 3 branches
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testMergeFailureMultipleBranches() throws IOException, InterruptedException {
        final int NUM_OF_BRANCHES = 3;
        final int NUM_OF_USERS = 3;
        
        long timeoutForTestInMilis = 1000 * 60 * 10;
        String destinationFolderForNewFile = DEFAULT_COMMIT_FOLDER;
        
        String[] userNames = generateUserNames(NUM_OF_USERS);
        String[] emails = generateEmailAddresses(NUM_OF_USERS);
        String[] branches = generateBranchNames(NUM_OF_BRANCHES);
        String[] newFileNames = generateFileNames(NUM_OF_BRANCHES);
        
        try {
            createAndPushBranches(branches);
            setProtectedBranches(branches);
            
            for (int i = 0; i < NUM_OF_BRANCHES; i++) {
                String branchName = branches[i];
                checkoutExistingBranch(branchName);
                
                addNewFile(destinationFolderForNewFile + "/" + newFileNames[i], String.format(
                        "This is line number 1 added by user %s in branch %s",
                        userNames[0],
                        branchName));
                
                String commit1Message =
                        String.format("user %s added file %s", userNames[0], newFileNames[i]);
                String commit1Id =
                        commitAndPush(
                                destinationFolderForNewFile,
                                userNames[0],
                                emails[0],
                                commit1Message,
                                true,
                                branchName);
                checkSuccessfulyProccesedCommitThatShouldPass(
                        VerigreenUtils.getVerigreenBranchName(processCommitId(commit1Id, userNames[0])),
                        branchName,
                        timeoutForTestInMilis,
                        commit1Id,
                        userNames[0],
                        emails[0],
                        commit1Message,
                        true);
            }
            
            CommitIdAndMessage[][] commits =
                    new CommitIdAndMessage[NUM_OF_BRANCHES][NUM_OF_USERS - 1];
            
            for (int i = 0; i < NUM_OF_BRANCHES; i++) {
                String branchName = branches[i];
                checkoutExistingBranch(branchName);
                
                String pathToFile =
                        constructLocalFilePath(destinationFolderForNewFile, newFileNames[i]);
                
                String masterBrnachCurrentSHA1 =
                        ((JGitOperator) _sourceControlOperator).getRef(
                                getBranchRefsRemotesFullName(branchName)).getObjectId().getName();
                
                writeToLocalFileInBeginning(pathToFile, String.format(
                        "This is line number 2 added by user %s in branch %s",
                        userNames[1],
                        branchName));
                
                commits[i][0] = new CommitIdAndMessage();
                commits[i][1] = new CommitIdAndMessage();
                
                commits[i][0].message =
                        String.format(
                                "user %s commits in same file %s",
                                userNames[1],
                                newFileNames[i]);
                commits[i][0].commitId =
                        commitAndPush(
                                destinationFolderForNewFile,
                                userNames[1],
                                emails[1],
                                commits[i][0].message,
                                false,
                                branchName);
                
                ((JGitOperator) _sourceControlOperator).reset(masterBrnachCurrentSHA1);
                
                appendToFile(pathToFile, newFileNames[i]);
                commits[i][1].message =
                        String.format(
                                "user %s commits in same file %s",
                                userNames[2],
                                newFileNames[i]);
                
                commits[i][1].commitId =
                        commitAndPush(
                                destinationFolderForNewFile,
                                userNames[2],
                                emails[2],
                                commits[i][1].message,
                                false,
                                branchName);
            }
            
            for (int i = 0; i < NUM_OF_BRANCHES; i++) {
                String branchName = branches[i];
                checkoutExistingBranch(branchName);
                
                checkSuccessfulyProccesedCommitThatShouldPass(
                        VerigreenUtils.getVerigreenBranchName(processCommitId(commits[i][0].commitId,userNames[1])),
                        branchName,
                        timeoutForTestInMilis,
                        commits[i][0].commitId,
                        userNames[1],
                        emails[1],
                        commits[i][0].message,
                        true);
                
                checkSuccessfulyProccesedCommitThatShouldFail(
                		VerigreenUtils.getVerigreenBranchName(processCommitId(commits[i][1].commitId, userNames[2])),
                        branchName,
                        timeoutForTestInMilis,
                        commits[i][1].commitId,
                        userNames[2],
                        emails[2],
                        commits[i][1].message,
                        VerificationStatus.MERGE_FAILED,
                        true);
            	}
        	}
            finally {
            try {
                setProtectedBranch(_masterBranch);
                checkoutExistingBranch(_masterBranch);
                _sourceControlOperator.deleteBranch(branches);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * @param pathToFile
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void writeToLocalFileInBeginning(String pathToFile, String content)
            throws FileNotFoundException, IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(new File(pathToFile), "rw");
        byte[] bytes = new byte[(int) randomAccessFile.length()];
        randomAccessFile.readFully(bytes);
        randomAccessFile.seek(0);
        randomAccessFile.write(content.getBytes());
        randomAccessFile.write("\n".getBytes());
        randomAccessFile.write(bytes);
        randomAccessFile.close();
    }
    
}