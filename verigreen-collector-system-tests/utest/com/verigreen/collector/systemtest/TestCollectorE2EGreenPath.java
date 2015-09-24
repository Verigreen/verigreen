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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.verigreen.collector.api.CommitItemPresentation;
import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.api.VerigreenUtils;
import com.verigreen.jgit.JGitOperator;
import com.verigreen.rest.CommitItemRequest;
import com.verigreen.restclient.RestClientImpl;
import com.verigreen.restclient.common.RestClientException;


@Category(SystemTests.class)
public class TestCollectorE2EGreenPath extends SystemTestBase {
    
    final long _timeoutForTestInMilis = 1000 * 60 * 30;

    String path = System.getenv("VG_HOME");
    final String _path = path + "//config.properties";
    
    /***
     * user1 creates new file -> commit+push collector+git verification
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testSimpleCommit() throws IOException, InterruptedException {
        
        String destinationFolderForNewFile = DEFAULT_COMMIT_FOLDER;
        String commiterName = generateUserName();
        String commiterEmail = generateEmailAddress();
        String newFileName = generateFileName();
        String content = getContent("testSimpleCommit");
        
        setProtectedBranch(_masterBranch);
        checkoutExistingBranch(_masterBranch);

        String commitId =
                createCommitAndPushNewFile(
                        destinationFolderForNewFile,
                        newFileName,
                        content,
                        commiterName,
                        commiterEmail,
                        content,
                        true,
                        false,
                        _masterBranch);
        String shortCommitId = processCommitId(commitId, commiterName);
        checkSuccessfulyProccesedCommitThatShouldPass(

                VerigreenUtils.getVerigreenBranchName(shortCommitId),
                _masterBranch,
                _timeoutForTestInMilis,
                commitId,
                commiterName,
                commiterEmail,
                content,
                true);
    }
 
    /***
     * user creates new file on several branches, and checks that all committed well and in the
     * correct branches
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testSimpleCommitMultipleBranches() throws IOException, InterruptedException {
        
        final int NUM_OF_BRANCHES = 3;
        
        String destinationFolderForNewFile = DEFAULT_COMMIT_FOLDER;
        
        String[] branches = generateBranchNames(NUM_OF_BRANCHES);
        
        try {
            createAndPushBranches(branches);
            setProtectedBranches(branches);
            
            String[] commiterNames = generateUserNames(branches.length);
            String[] commiterEmails = generateEmailAddresses(branches.length);
            String[] newFileNames = generateFileNames(branches.length);
            String[] contents = getContents("testSimpleCommitMultipleBranches", branches.length);
            
            String[] commitIds = new String[branches.length];
            
            for (int i = 0; i < branches.length; i++) {
                String branchName = branches[i];
                checkoutExistingBranch(branchName);
                commitIds[i] =
                        createCommitAndPushNewFile(
                                destinationFolderForNewFile,
                                newFileNames[i],
                                contents[i],
                                commiterNames[i],
                                commiterEmails[i],
                                contents[i],
                                true,
                                false,
                                branchName);
            }
            for (int i = 0; i < branches.length; i++) {
            	String shortCommitId = processCommitId(commitIds[i],commiterNames[i]);
                checkSuccessfulyProccesedCommitThatShouldPass(
                        VerigreenUtils.getVerigreenBranchName(shortCommitId),
                        branches[i],
                        _timeoutForTestInMilis,
                        commitIds[i],
                        commiterNames[i],
                        commiterEmails[i],
                        contents[i],
                        true);
            }
        } finally {
            try {
                setProtectedBranch(_masterBranch);
                checkoutExistingBranch(_masterBranch);
                _sourceControlOperator.deleteBranch(branches);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @Test
    
    public void testPermittedUser() throws IOException, InterruptedException, RestClientException {
        
        String destinationFolderForNewFile = DEFAULT_COMMIT_FOLDER;
        
        String commiterName = "permitted.user@nowhere.com";
        String commiterEmail = "permitted.user@nowhere.com";
        String content = "testing the permitted user";
        setPermittedUser(commiterName);
        setProtectedBranch(_masterBranch);
        checkoutExistingBranch(_masterBranch);

        String newFileName = generateFileName();
        
        String commitId =
                createCommitAndPushNewFile(
                        destinationFolderForNewFile,
                        newFileName,
                        getContent("testPermittedUser"),
                        commiterName,
                        commiterEmail,
                        content,
                        true,
                        true,
                        _masterBranch);   
        assertBranchContainsCommit(commitId, _masterBranch, true);
        
        String vgBranchName = VerigreenUtils.getVerigreenBranchName(processCommitId(commitId, commiterName));
        
        CommitItemRequest commitItemRequest =
        		com.verigreen.spring.common.CollectorApi.getCommitItemRequest(vgBranchName, _masterBranch, commitId);
        
        CommitItemPresentation item = null;
        try
        {
        	RestClientImpl restClient = new RestClientImpl();
        	if(restClient.get(commitItemRequest).getStatusInfo().getStatusCode()!=404)
        	{
         		item = restClient.get(commitItemRequest).getEntity(CommitItemPresentation.class);
        	}  
            Assert.assertNull(item);
        }
        catch(Throwable ex)
        {
        	Assert.assertTrue(ex instanceof RestClientException);
        }
  
        
    }
    
    /***
     * User 1 creates file1 and file2 and writes 1 line in each file -> commit+push wait until
     * verification for the commit is finished user 2 adds another line to file1 -> commit+push user
     * 3 adds another line to file2 -> commit+push collector verification for the commit of user2
     * collector+git verification for the commit of user3
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testSimpleMerge() throws IOException, InterruptedException {
        
         String destinationFolderForNewFile = DEFAULT_COMMIT_FOLDER;
        
        String commiter1Name = generateUserName();
        String commiter1Email = generateEmailAddress();
        String commiter2Name = generateUserName();
        String commiter2Email = generateEmailAddress();
        String commiter3Name = generateUserName();
        String commiter3Email = generateEmailAddress();
        
        setProtectedBranch(_masterBranch);
        checkoutExistingBranch(_masterBranch);
        
        String newFileName1 = generateFileName();
        String newFileName2 = generateFileName();
        
        addNewFile(
                destinationFolderForNewFile + "/" + newFileName1,
                "This is line number 1 in file1 !!!");
        addNewFile(
                destinationFolderForNewFile + "/" + newFileName2,
                "This is line number 1 in file2 !!!");
        
        String commit1Message = String.format("User %s added 2 new files", commiter1Name);
        String commit1Id =
                commitAndPush(
                        destinationFolderForNewFile,
                        commiter1Name,
                        commiter1Email,
                        commit1Message,
                        true,
                        _masterBranch);
        checkSuccessfulyProccesedCommitThatShouldPass(
        		
                VerigreenUtils.getVerigreenBranchName(processCommitId(commit1Id,commiter1Name)),
                _masterBranch,
                _timeoutForTestInMilis,
                commit1Id,
                commiter1Name,
                commiter1Email,
                commit1Message,
                true);
        
        String masterBrnachCurrentSHA1 =
                ((JGitOperator) _sourceControlOperator).getRef(
                        getBranchRefsRemotesFullName(_masterBranch)).getObjectId().getName();
        
        String path = constructLocalFilePath(destinationFolderForNewFile, newFileName1);
        appendToFile(path, "This is line number 2 !!!");

        String commit2Message = String.format("user %s commits", commiter2Name);
        String commit2Id =
                commitAndPush(
                        destinationFolderForNewFile,
                        commiter2Name,
                        commiter2Email,
                        commit2Message,
                        false,
                        _masterBranch);
        setProtectedBranch(VerigreenUtils.getVerigreenBranchName(processCommitId(commit2Id, commiter2Name)));
        ((JGitOperator) _sourceControlOperator).reset(masterBrnachCurrentSHA1);
        
        path = constructLocalFilePath(destinationFolderForNewFile, newFileName2);
        appendToFile(path, "This is line number 3 !!!");
        
        String commit3Message = String.format("user %s commits", commiter3Name);
        String commit3Id =
                commitAndPush(
                        destinationFolderForNewFile,
                        commiter3Name,
                        commiter3Email,
                        commit3Message,
                        false,
                        _masterBranch);
        checkSuccessfulyProccesedCommitThatShouldPass(
                VerigreenUtils.getVerigreenBranchName(processCommitId(commit2Id,commiter2Name)),
                _masterBranch,
                _timeoutForTestInMilis,
                commit2Id,
                commiter2Name,
                commiter2Email,
                commit2Message,
                true);
        checkSuccessfulyProccesedCommitThatShouldPass(
                VerigreenUtils.getVerigreenBranchName(processCommitId(commit3Id,commiter3Name)),
                _masterBranch,
                _timeoutForTestInMilis,
                commit3Id,
                commiter3Name,
                commiter3Email,
                commit3Message,
                true);
    }
    
    /**
     * User 1 commits and pushes to the master branch
     * User 2 (permitted user) also pushes to the master branch while the job of user A is running
     * Because User 2 is a permitted user, his commit will be rejected by verigreen and will be pushed to the repository directly 
     * 
     * */
    @Test
    public void permittedUserPushWhileJobIsRunning() throws IOException, InterruptedException, RestClientException
    {
    	  String destinationFolderForNewFile = DEFAULT_COMMIT_FOLDER;
          String commiterName1 = generateUserName();
          String commiterEmail1 = generateEmailAddress();
          
          String commiterName2 = "permitted.user@nowhere.com";
          String commiterEmail2 = "permitted.user@nowhere.com";
          
          String newFileName1 = generateFileName();
          String content1 = getContent("testpermittedUserPushWhileJobIsRunning");
          
          setPermittedUser(commiterName2);
          setProtectedBranch(_masterBranch);
          checkoutExistingBranch(_masterBranch);

          String commitId1 =
                  createCommitAndPushNewFile(
                          destinationFolderForNewFile,
                          newFileName1,
                          content1,
                          commiterName1,
                          commiterEmail1,
                          content1,
                          true,
                          false,
                          _masterBranch);

          Thread.sleep(8000);
          String masterBranchCurrentSHA1 =
               ((JGitOperator) _sourceControlOperator).getRef(
                        getBranchRefsRemotesFullName(_masterBranch)).getObjectId().getName();
          
          ((JGitOperator) _sourceControlOperator).reset(masterBranchCurrentSHA1);
          
          String commitId2 =
                  createCommitAndPushNewFile(
                          destinationFolderForNewFile,
                          newFileName1,
                          content1,
                          commiterName2,
                          commiterEmail2,
                          content1,
                          false,
                          true,
                          _masterBranch);
          String shortCommitId = processCommitId(commitId1, commiterName1);
          checkSuccessfulyProccesedCommitThatShouldPass(

                  VerigreenUtils.getVerigreenBranchName(shortCommitId),
                  _masterBranch,
                  _timeoutForTestInMilis,
                  commitId1,
                  commiterName1,
                  commiterEmail1,
                  content1,
                  true);
          
          String vgBranchName = VerigreenUtils.getVerigreenBranchName(processCommitId(commitId2, commiterName2));
          
          CommitItemRequest commitItemRequest =
          		com.verigreen.spring.common.CollectorApi.getCommitItemRequest(vgBranchName, _masterBranch, commitId2);
          
          CommitItemPresentation item = null;
          try
          {
          	RestClientImpl restClient = new RestClientImpl();
          	if(restClient.get(commitItemRequest).getStatusInfo().getStatusCode()!=404)
          	{
           		item = restClient.get(commitItemRequest).getEntity(CommitItemPresentation.class);
          	}  
              Assert.assertNull(item);
          }
          catch(Throwable ex)
          {
          	Assert.assertTrue(ex instanceof RestClientException);
          }
    }
    
   
    
    /***
     * User 1 creates file1 and file2 and writes 1 line in each file -> commit+push wait until
     * verification for the commit is finished user 2 adds another line to file1 -> commit+push user
     * 3 adds another line to file2 -> commit+push collector verification for the commit of user2
     * collector+git verification for the commit of user3
     * 
     * This happens on multiple branches.
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testSimpleMergeMultipleBranches() throws IOException, InterruptedException {
        
        final int NUM_OF_BRANCHES = 3;
        // This constant is used only for array generation. The usage is not generic.
        final int NUM_OF_USERS = 3;
        final int NUM_OF_FILES = 2;
        
        String destinationFolderForNewFile = DEFAULT_COMMIT_FOLDER;
        
        String[] branches = generateBranchNames(NUM_OF_BRANCHES);
        
        try {
            createAndPushBranches(branches);
            setProtectedBranches(branches);
            
            String[] userNames = generateUserNames(NUM_OF_USERS);
            String[] emails = generateEmailAddresses(NUM_OF_USERS);
            
            // first index is the branch id, second is the file name id
            String[][] branchFileNames = new String[NUM_OF_BRANCHES][NUM_OF_FILES];
            for (int i = 0; i < NUM_OF_BRANCHES; i++) {
                for (int j = 0; j < NUM_OF_FILES; j++) {
                    branchFileNames[i][j] = generateFileName();
                }
            }
            
            // Step 1. user 0 commits and pushes 2 files (for each branch)
            String fileNameContentPattern = "This is line number 1 in file: %s. Made by user %s";
            for (int i = 0; i < NUM_OF_BRANCHES; i++) {
                String branchName = branches[i];
                checkoutExistingBranch(branchName);
                
                for (int j = 0; j < NUM_OF_FILES; j++) {
                    String fileName = branchFileNames[i][j];
                    addNewFile(
                            destinationFolderForNewFile + "/" + fileName,
                            String.format(fileNameContentPattern, fileName, userNames[0]));
                }
                
                String commit1Message =
                        String.format(
                                "User %s added 2 new files in branch %s",
                                userNames[0],
                                branchName);
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
                        _timeoutForTestInMilis,
                        commit1Id,
                        userNames[0],
                        emails[0],
                        commit1Message,
                        true);
                
                System.out.println(String.format(
                        "Created initial commit for branch no: %d, branch: %s",
                        i + 1,
                        branchName));
            }
            
            // Step 2. For each branch user 1 and user 2 change file 0 and file 1 respectively, 
            // both based on the previous commit in step 1.
            CommitIdAndMessage[][] commits =
                    new CommitIdAndMessage[NUM_OF_BRANCHES][NUM_OF_USERS - 1];
            for (int i = 0; i < NUM_OF_BRANCHES; i++) {
                String branchName = branches[i];
                checkoutExistingBranch(branchName);
                
                String branchCurrentSHA1 =
                        ((JGitOperator) _sourceControlOperator).getRef(
                                getBranchRefsRemotesFullName(branchName)).getObjectId().getName();
                
                String fileName = branchFileNames[i][0];
                commits[i][0] =
                        appendToFileAndCommitAndPush(
                                destinationFolderForNewFile,
                                userNames[1],
                                emails[1],
                                branchName,
                                fileName,
                                2);
                
                ((JGitOperator) _sourceControlOperator).reset(branchCurrentSHA1);
                
                fileName = branchFileNames[i][1];
                commits[i][1] =
                        appendToFileAndCommitAndPush(
                                destinationFolderForNewFile,
                                userNames[2],
                                emails[2],
                                branchName,
                                fileName,
                                3);
            }
            
            // Step 3. Verify that for each branch all the commits and pushes 
            // were completed successfuly for user 1 and user 2.           
            for (int i = 0; i < NUM_OF_BRANCHES; i++) {
                String branchName = branches[i];
                
                checkSuccessfulyProccesedCommitThatShouldPass(
                        VerigreenUtils.getVerigreenBranchName(processCommitId(commits[i][0].commitId, userNames[1])),
                        branchName,
                        _timeoutForTestInMilis,
                        commits[i][0].commitId,
                        userNames[1],
                        emails[1],
                        commits[i][0].message,
                        true);
                
                checkSuccessfulyProccesedCommitThatShouldPass(
                        VerigreenUtils.getVerigreenBranchName(processCommitId(commits[i][1].commitId, userNames[2])),
                        branchName,
                        _timeoutForTestInMilis,
                        commits[i][1].commitId,
                        userNames[2],
                        emails[2],
                        commits[i][1].message,
                        true);
            }
        } finally {
            try {
                setProtectedBranch(_masterBranch);
                checkoutExistingBranch(_masterBranch);
                _sourceControlOperator.deleteBranch(branches);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private CommitIdAndMessage appendToFileAndCommitAndPush(
            String destinationFolderForNewFile,
            String commiterName,
            String commiterEmail,
            String branchName,
            String fileName,
            int lineNumber) throws IOException {
        
        final String fileNameContentPattern = "This is line number %d in file: %s. Made by user %s";
        
        String path = constructLocalFilePath(destinationFolderForNewFile, fileName);
        appendToFile(
                path,
                String.format(fileNameContentPattern, lineNumber, fileName, commiterName));
        
        CommitIdAndMessage commitIdAndMessage = new CommitIdAndMessage();
        commitIdAndMessage.message =
                String.format("user %s commits in branch %s", commiterName, branchName);
        commitIdAndMessage.commitId =
                commitAndPush(
                        destinationFolderForNewFile,
                        commiterName,
                        commiterEmail,
                        commitIdAndMessage.message,
                        false,
                        branchName);
        
        return commitIdAndMessage;
    }
    
    @Test
    public void test5Users() throws IOException, InterruptedException {
        
        final int NUM_OF_USERS = 5;
        final int NUM_OF_FILES = 4;
        
        long timeoutForTestInMilis = 1000 * 60 * 10;
        String destinationFolderForNewFile = DEFAULT_COMMIT_FOLDER;
        
        String[] userNames = generateUserNames(NUM_OF_USERS);
        String[] emails = generateEmailAddresses(NUM_OF_USERS);
        
        setProtectedBranch(_masterBranch);
        checkoutExistingBranch(_masterBranch);
        
        String[] fileNames = generateFileNames(NUM_OF_FILES);
        String[] fileContents = getContents("test5Users", NUM_OF_FILES);
        
        for (int i = 0; i < NUM_OF_FILES; i++) {
            addNewFile(destinationFolderForNewFile + "/" + fileNames[i], fileContents[i]);
        }
        
        String commit1Message = String.format("user %s added 4 new files", userNames[0]);
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
        
        String masterBranchCurrentSHA1 =
                ((JGitOperator) _sourceControlOperator).getRef(
                        getBranchRefsRemotesFullName(_masterBranch)).getObjectId().getName();
        
        CommitIdAndMessage[] commits = new CommitIdAndMessage[NUM_OF_FILES];
        
        for (int i = 0; i < NUM_OF_FILES; i++) {
            commits[i] =
                    appendToFileAndCommitAndPush(
                            destinationFolderForNewFile,
                            userNames[i + 1],
                            emails[i + 1],
                            _masterBranch,
                            fileNames[i],
                            2);
            
            if (i < (NUM_OF_FILES - 1)) {
                ((JGitOperator) _sourceControlOperator).reset(masterBranchCurrentSHA1);
                masterBranchCurrentSHA1 =
                        ((JGitOperator) _sourceControlOperator).getRef(
                                getBranchRefsRemotesFullName(_masterBranch)).getObjectId().getName();
            }
        }
        
        for (int i = 0; i < NUM_OF_FILES; i++) {
            System.out.println(String.format("Checking intex %d", i));
            checkSuccessfulyProccesedCommitThatShouldPass(
                    VerigreenUtils.getVerigreenBranchName(processCommitId(commits[i].commitId, userNames[i + 1])),
                    _masterBranch,
                    timeoutForTestInMilis,
                    commits[i].commitId,
                    userNames[i + 1],
                    emails[i + 1],
                    commits[i].message,
                    true);
        }
    }
    
    /**
     * User 1 commits 4 different files in 3 branches. Then other 4 users change each different file
     * in all the branches. All should be merged in correct branches.
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void test5UsersMultipleBranches() throws IOException, InterruptedException {
        
        final int NUM_OF_BRANCHES = 3;
        final int NUM_OF_USERS = 5;
        final int NUM_OF_FILES = 4;
        
        long timeoutForTestInMilis = 1000 * 60 * 10;
        String destinationFolderForNewFile = DEFAULT_COMMIT_FOLDER;
        
        String[] userNames = generateUserNames(NUM_OF_USERS);
        String[] emails = generateEmailAddresses(NUM_OF_USERS);
        
        String[] branches = generateBranchNames(NUM_OF_BRANCHES);
        String[][] fileNames = new String[NUM_OF_BRANCHES][NUM_OF_FILES];
        String[][] fileContents = new String[NUM_OF_BRANCHES][NUM_OF_FILES];
        
        for (int i = 0; i < NUM_OF_BRANCHES; i++) {
            fileNames[i] = generateFileNames(NUM_OF_FILES);
            fileContents[i] = getContents("test5UsersMultippleBranches", NUM_OF_FILES);
        }
        
        try {
            createAndPushBranches(branches);
            setProtectedBranches(branches);
            
            for (int i = 0; i < NUM_OF_BRANCHES; i++) {
                String branchName = branches[i];
                checkoutExistingBranch(branchName);
                for (int j = 0; j < NUM_OF_FILES; j++) {
                    addNewFile(
                            destinationFolderForNewFile + "/" + fileNames[i][j],
                            fileContents[i][j]);
                }
                String commitMessage = String.format("user %s added 4 new files", userNames[0]);
                String commitId =
                        commitAndPush(
                                destinationFolderForNewFile,
                                userNames[0],
                                emails[0],
                                commitMessage,
                                true,
                                branchName);
                checkSuccessfulyProccesedCommitThatShouldPass(
                        VerigreenUtils.getVerigreenBranchName(processCommitId(commitId, userNames[0])),
                        branchName,
                        timeoutForTestInMilis,
                        commitId,
                        userNames[0],
                        emails[0],
                        commitMessage,
                        true);
            }
            CommitIdAndMessage[][] commits = new CommitIdAndMessage[NUM_OF_BRANCHES][NUM_OF_FILES];
            for (int i = 0; i < NUM_OF_BRANCHES; i++) {
                String branchName = branches[i];
                checkoutExistingBranch(branchName);
                
                String masterBranchCurrentSHA1 =
                        ((JGitOperator) _sourceControlOperator).getRef(
                                getBranchRefsRemotesFullName(branchName)).getObjectId().getName();
                
                for (int j = 0; j < NUM_OF_FILES; j++) {
                    commits[i][j] =
                            appendToFileAndCommitAndPush(
                                    destinationFolderForNewFile,
                                    userNames[i + 1],
                                    emails[i + 1],
                                    branchName,
                                    fileNames[i][j],
                                    2);
                    
                    if (i < (NUM_OF_FILES - 1)) {
                        ((JGitOperator) _sourceControlOperator).reset(masterBranchCurrentSHA1);
                        masterBranchCurrentSHA1 =
                                ((JGitOperator) _sourceControlOperator).getRef(
                                        getBranchRefsRemotesFullName(branchName)).getObjectId().getName();
                    }
                }
            }
            
            for (int i = 0; i < NUM_OF_BRANCHES; i++) {
                String branchName = branches[i];
                checkoutExistingBranch(branchName);
                
                for (int j = 0; j < NUM_OF_FILES; j++) {
                    checkSuccessfulyProccesedCommitThatShouldPass(
                            VerigreenUtils.getVerigreenBranchName(processCommitId(commits[i][j].commitId, userNames[i + 1])),
                            branchName,
                            timeoutForTestInMilis,
                            commits[i][j].commitId,
                            userNames[i + 1],
                            emails[i + 1],
                            commits[i][j].message,
                            true);
                }
            }
        } finally {
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
     * 
     * Change the repository name in the config.properties file
     * */
    public void changeConfigFileRepositoryPath(String repo) throws IOException
    {
    	InputStream input = new FileInputStream(_path);

        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder out = new StringBuilder();
        String line, row = null;
		while ((line = reader.readLine()) != null) {
			row = line.toString();
			if(line.toString().contains("git.repositoryLocation")){
				row = "git.repositoryLocation=" + repo;
			}
			out.append(row + "\n");
        }
        reader.close();
        
        OutputStream output = new FileOutputStream(_path);

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
		writer.write(out.toString());
    	writer.close();
	}
    
    /**
     * 
     * Change the job name in the config.properties file
     * */
    public void changeConfigFileJobName(String job) throws IOException
    {

    	InputStream input = new FileInputStream(_path);

        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder out = new StringBuilder();
        String line, row = null;
		while ((line = reader.readLine()) != null) {
			row = line.toString();
			if(line.toString().contains("jenkins.jobName")){
				row = "jenkins.jobName=" + job;
			}
			out.append(row + "\n");
        }
        reader.close();
       
        OutputStream output = new FileOutputStream(_path);

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
		writer.write(out.toString());
    	writer.close();
	}
    
    /**
     * Testing the retry in case of a trigger failure. 
     * An invalid username is specified, the branch is committed and pushed, asserted as failed with the
     * TRIGGER FAILURE status.
     * Then the same commit id is pushed again with a valid user and asserted as PASSED AND PUSHED.
     * @throws IOException 
     * */
    @Test
    public void testRetryTriggerFailed() throws IOException, InterruptedException {
        
        String destinationFolderForNewFile = DEFAULT_COMMIT_FOLDER;
        long timeoutForTestInMilis = 10000 * 60 * 3;
        String commiterName = generateUserName();
        String commiterEmail = generateEmailAddress();
        String content = getContent("testRetryTriggerFailed");
        
        setProtectedBranch(_masterBranch);
        checkoutExistingBranch(_masterBranch);
        String originJobName = getJobName();
        changeConfigFileJobName("NotARealJobName");
        String commitId =
        		commitAndPush(
                        destinationFolderForNewFile,
                        commiterName,
                        commiterEmail,
                        content,
                        true,
                        _masterBranch);
             
        String shortCommitId = processCommitId(commitId, commiterName);
        checkSuccessfulyProccesedCommitThatShouldFail(
                VerigreenUtils.getVerigreenBranchName(shortCommitId),
                _masterBranch,
                timeoutForTestInMilis,
                commitId,
                commiterName,
                commiterEmail,
                content,
                VerificationStatus.TRIGGER_FAILED,
                true);
        
        // change job back
        checkoutExistingBranch(_masterBranch);
        changeConfigFileJobName(originJobName);
       _sourceControlOperator.push(_masterBranch, _masterBranch);
       Thread.sleep(15000);
        checkSuccessfulyProccesedCommitThatShouldPass(
                VerigreenUtils.getVerigreenBranchName(shortCommitId),
                _masterBranch,
                timeoutForTestInMilis,
                commitId,
                commiterName,
                commiterEmail,
                content,
                true);
        
    }
    private String getJobName() throws IOException{
    	InputStream input = new FileInputStream(_path);

        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line, jobName = null;
		while ((line = reader.readLine()) != null) {
			if(line.toString().contains("jenkins.jobName")){
				jobName = line.replace("jenkins.jobName=", "");
				break;
			}
        }
		
        reader.close();
        return jobName;
	}

	/**
     * Testing the retry in case of a git failure. 
     * An invalid path is specified, the branch is committed and pushed, asserted as failed with the
     * GIT FAILED status.
     * Then the same commit id is pushed again with a valid path to the 
     * repository and asserted as PASSED AND PUSHED.
     * */
    @Test
    @Ignore
    public void testRetryGitFailed() throws IOException, InterruptedException {
        
    	String destinationFolderForNewFile = DEFAULT_COMMIT_FOLDER;
        long timeoutForTestInMilis = 1000 * 60 * 3;
        String commiterName = generateUserName();
        String commiterEmail = generateEmailAddress();
        String content = getContent("testRetryGitFailed");
        setProtectedBranch(_masterBranch);
        checkoutExistingBranch(_masterBranch);
        String _reaRepository = _localRepositoryFolder;
       
        String commitId =
        		commitAndPush(
                        destinationFolderForNewFile,
                        commiterName,
                        commiterEmail,
                        content,
                        true,
                        _masterBranch);
      
        String shortCommitId = processCommitId(commitId, commiterName);
        _localRepositoryFolder = "";
        checkSuccessfulyProccesedCommitThatShouldFail(
                VerigreenUtils.getVerigreenBranchName(shortCommitId),
                _masterBranch,
                timeoutForTestInMilis,
                commitId,
                commiterName,
                commiterEmail,
                content,
                VerificationStatus.GIT_FAILURE,
                true);
        
        checkoutExistingBranch(_masterBranch);
        
       _sourceControlOperator.push(_masterBranch, _masterBranch);
       _localRepositoryFolder = _reaRepository;
        checkSuccessfulyProccesedCommitThatShouldPass(
                VerigreenUtils.getVerigreenBranchName(shortCommitId),
                _masterBranch,
                timeoutForTestInMilis,
                commitId,
                commiterName,
                commiterEmail,
                content,
                true);
        
    }

    
    @Test
    public void test5UsersWithMergeFailure() throws IOException, InterruptedException {
        
        long timeoutForTestInMilis = 1000 * 60 * 15;
        String destinationFolderForNewFile = DEFAULT_COMMIT_FOLDER;
        
        final int NUM_OF_USERS = 5;
        final int NUM_OF_FILES = 4;
        
        String[] userNames = generateUserNames(NUM_OF_USERS);
        String[] emails = generateEmailAddresses(NUM_OF_USERS);
        
        setProtectedBranch(_masterBranch);
        checkoutExistingBranch(_masterBranch);
        
        String[] fileNames = generateFileNames(NUM_OF_FILES);
        String[] fileContents = getContents("test5UsersWithMergeFailure", NUM_OF_FILES);
        
        for (int i = 0; i < NUM_OF_FILES; i++) {
            addNewFile(destinationFolderForNewFile + "/" + fileNames[i], fileContents[i]);
        }
        
        CommitIdAndMessage[] commits = new CommitIdAndMessage[NUM_OF_USERS];
        
        CommitIdAndMessage currentCommitIdAndMessage = new CommitIdAndMessage();
        commits[0] = currentCommitIdAndMessage;
        currentCommitIdAndMessage.message =
                String.format("User %s added %d new files", userNames[0], NUM_OF_FILES);
        currentCommitIdAndMessage.commitId =
                commitAndPush(
                        destinationFolderForNewFile,
                        userNames[0],
                        emails[0],
                        currentCommitIdAndMessage.message,
                        true,
                        _masterBranch);
        checkSuccessfulyProccesedCommitThatShouldPass(
                VerigreenUtils.getVerigreenBranchName(processCommitId(currentCommitIdAndMessage.commitId, userNames[0])),
                _masterBranch,
                timeoutForTestInMilis,
                currentCommitIdAndMessage.commitId,
                userNames[0],
                emails[0],
                currentCommitIdAndMessage.message,
                true);
        
        String masterBrnachCurrentSHA1 = null;
        for (int i = 1; i < NUM_OF_USERS; i++) {
            if (i < (NUM_OF_USERS - 1)) {
                masterBrnachCurrentSHA1 =
                        ((JGitOperator) _sourceControlOperator).getRef(
                                getBranchRefsRemotesFullName(_masterBranch)).getObjectId().getName();
            }
            commits[i] =
                    currentCommitIdAndMessage =
                            appendToFileAndCommitAndPush(
                                    destinationFolderForNewFile,
                                    userNames[i],
                                    emails[i],
                                    _masterBranch,
                                    i == 2 ? fileNames[0] : fileNames[i - 1], // commit for user 2 will have a conflict, because is made on the same file as the commit for user 1.
                                    i == 2 ? 3 : 2);
            
            if (i < (NUM_OF_USERS - 1)) {
                ((JGitOperator) _sourceControlOperator).reset(masterBrnachCurrentSHA1);
            }
        }
        
        for (int i = 1; i < NUM_OF_USERS; i++) {
            if (i != 2) {
                checkSuccessfulyProccesedCommitThatShouldPass(
                        VerigreenUtils.getVerigreenBranchName(processCommitId(commits[i].commitId, userNames[i])),
                        _masterBranch,
                        timeoutForTestInMilis,
                        commits[i].commitId,
                        userNames[i],
                        emails[i],
                        commits[i].message,
                        true);
            } else {
                checkSuccessfulyProccesedCommitThatShouldFail(
                        VerigreenUtils.getVerigreenBranchName(processCommitId(commits[i].commitId, userNames[i])),
                        _masterBranch,
                        timeoutForTestInMilis,
                        commits[i].commitId,
                        userNames[i],
                        emails[i],
                        commits[i].message,
                        VerificationStatus.MERGE_FAILED,
                        true);
            }
        }
    }
    
    /**
     * User 1 commits 4 different files in 3 branches. Users 2 and 3 update file number 1
     * simultaneously in the same row for 3 branches. Users 4 and 5 update each different files in 3
     * branches
     * 
     * User 3 should receive FAIL_MERGE and all the rest should be committed and pushed successfully
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void test5UsersWithMergeFailureMultipleBranches() throws IOException,
            InterruptedException {
        
        long timeoutForTestInMilis = 5000 * 60 * 10;
        String destinationFolderForNewFile = DEFAULT_COMMIT_FOLDER;
        
        final int NUM_OF_BRANCHES = 3;
        final int NUM_OF_USERS = 5;
        final int NUM_OF_FILES = 4;
        
        String[] userNames = generateUserNames(NUM_OF_USERS);
        String[] emails = generateEmailAddresses(NUM_OF_USERS);
        String[] branches = generateBranchNames(NUM_OF_BRANCHES);
        
        String[][] fileNames = new String[NUM_OF_BRANCHES][NUM_OF_FILES];
        String[][] fileContents = new String[NUM_OF_BRANCHES][NUM_OF_FILES];
        
        for (int i = 0; i < NUM_OF_BRANCHES; i++) {
            fileNames[i] = generateFileNames(NUM_OF_FILES);
            fileContents[i] =
                    getContents("test5UsersWithMergeFailureMultipleBranches", NUM_OF_FILES);
        }
        
        try {
            createAndPushBranches(branches);
            setProtectedBranches(branches);
            
            CommitIdAndMessage[][] commits = new CommitIdAndMessage[NUM_OF_BRANCHES][NUM_OF_USERS];
            
            // Step 1. User 0 creates 4 files, commits and pushes in all 3 branches
            for (int i = 0; i < NUM_OF_BRANCHES; i++) {
                String branchName = branches[i];
                checkoutExistingBranch(branchName);
                
                for (int j = 0; j < NUM_OF_FILES; j++) {
                    addNewFile(
                            destinationFolderForNewFile + "/" + fileNames[i][j],
                            fileContents[i][j]);
                }
                
                CommitIdAndMessage currentCommitIdAndMessage = new CommitIdAndMessage();
                commits[i][0] = currentCommitIdAndMessage;
                currentCommitIdAndMessage.message =
                        String.format(
                                "User %s added %d new files in branch %s",
                                userNames[0],
                                NUM_OF_FILES,
                                branchName);
                currentCommitIdAndMessage.commitId =
                        commitAndPush(
                                destinationFolderForNewFile,
                                userNames[0],
                                emails[0],
                                currentCommitIdAndMessage.message,
                                true,
                                branchName);
                String shortCommitId = processCommitId(currentCommitIdAndMessage.commitId, userNames[0]);
                checkSuccessfulyProccesedCommitThatShouldPass(
                        VerigreenUtils.getVerigreenBranchName(shortCommitId),
                        branchName,
                        timeoutForTestInMilis,
                        currentCommitIdAndMessage.commitId,
                        userNames[0],
                        emails[0],
                        currentCommitIdAndMessage.message,
                        true);
            }
            
            // Step 2. User 1 and 2 update file 0 and commit and push, user 3 updates file 2 commits and pushes, 
            // and user 4 updates file 3 and commits and pushes (for all branches)
            String branchCurrentSHA1 = null;
            for (int i = 0; i < NUM_OF_BRANCHES; i++) {
                String branchName = branches[i];
                checkoutExistingBranch(branchName);
                
                for (int j = 1; j < NUM_OF_USERS; j++) {
                    if (j < (NUM_OF_USERS - 1)) {
                        branchCurrentSHA1 =
                                ((JGitOperator) _sourceControlOperator).getRef(
                                        getBranchRefsRemotesFullName(branchName)).getObjectId().getName();
                    }
                    
                    commits[i][j] =
                            appendToFileAndCommitAndPush(
                                    destinationFolderForNewFile,
                                    userNames[i],
                                    emails[i],
                                    branchName,
                                    // commit for user 2 will have a conflict, because is made on the same file as the commit for user 1.
                                    j == 2 ? fileNames[i][0] : fileNames[i][j - 1],
                                    j == 2 ? 3 : 2);
                    
                    if (j < (NUM_OF_USERS - 1)) {
                        ((JGitOperator) _sourceControlOperator).reset(branchCurrentSHA1);
                    }
                }
            }
            
            // Step 3. Verify that commits of user 1, 3 and 4 passed, and user 2 was failed on merge.
            for (int i = 0; i < NUM_OF_BRANCHES; i++) {
                String branchName = branches[i];
                checkoutExistingBranch(branchName);
                
                for (int j = 1; j < NUM_OF_USERS; j++) {
                 	String shortCommitId = processCommitId(commits[i][j].commitId, userNames[i]);
                    if (j != 2) {
                        checkSuccessfulyProccesedCommitThatShouldPass(
                                VerigreenUtils.getVerigreenBranchName(shortCommitId),
                                branchName,
                                timeoutForTestInMilis,
                                commits[i][j].commitId,
                                userNames[i],
                                emails[i],
                                commits[i][j].message,
                                true);
                    } else {
                        checkSuccessfulyProccesedCommitThatShouldFail(
                                VerigreenUtils.getVerigreenBranchName(shortCommitId),
                                branchName,
                                timeoutForTestInMilis,
                                commits[i][j].commitId,
                                userNames[i],
                                emails[i],
                                commits[i][j].message,
                                VerificationStatus.MERGE_FAILED,
                                true);
                    }
                }
            }
        } finally {
            try {
                setProtectedBranch(_masterBranch);
                checkoutExistingBranch(_masterBranch);
                _sourceControlOperator.deleteBranch(branches);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /***
     * Testing change of commiter name in real time. 
     * using real properties form collector config.properties
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testCommiterNameChangesInRT() throws IOException, InterruptedException {
        
    	String destinationFolderForNewFile = DEFAULT_COMMIT_FOLDER;
        String commiterName = generateUserName();
        String commiterEmail = generateEmailAddress();
        String newFileName = generateFileName();
        String content = getContent("testCommiterNameChangesInRT");
        
        InputStream input = new FileInputStream(_path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder out = new StringBuilder();
        StringBuilder outOriginal = new StringBuilder();
        String line, row = null;
        while ((line = reader.readLine()) != null) {
        	row = line.toString();
        	outOriginal.append(row + "\n");
        	if(line.toString().contains("git.permittedUsers")){
        		row = "git.permittedUsers=" + commiterEmail;
        	}
        	out.append(row + "\n");
        }
        reader.close();
        
        OutputStream output = new FileOutputStream(_path);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
        writer.write(out.toString());
        writer.close();
//        setProtectedBranch(_masterBranch);
        checkoutExistingBranch(_masterBranch);
        
        createCommitAndPushNewFile(
		        destinationFolderForNewFile,
		        newFileName,
		        content,
		        commiterName,
		        commiterEmail,
		        content,
		        true,
		        true,
		        _masterBranch);
        
        OutputStream output2 = new FileOutputStream(_path);
		BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(output2));
		writer2.write(outOriginal.toString());
    	writer2.close();
        
    }
    
    /***
     * Testing change of protected branch in real time. 
     * user1 creates new file -> commit+push collector+git verification
     * using real properties form collector config.properties
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testProtectedBranchesChangesInRT() throws IOException, InterruptedException {
        
        String destinationFolderForNewFile = DEFAULT_COMMIT_FOLDER;
        String commiterName = generateUserName();
        String commiterEmail = generateEmailAddress();
        String newFileName = generateFileName();
        String content = getContent("testProtectedBranchesChangesInRT");
        
        String[] branches = {"refs/heads/master"};
        
        InputStream input = new FileInputStream(_path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder out = new StringBuilder();
        StringBuilder outOriginal = new StringBuilder();
        String line, row = null;
		while ((line = reader.readLine()) != null) {
			row = line.toString();
			outOriginal.append(row + "\n");
			if(line.toString().contains("git.protectedBranches")){
				row = "git.protectedBranches=" + branches[0];
			}
			out.append(row + "\n");
        }
        reader.close();
        
        OutputStream output = new FileOutputStream(_path);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
		writer.write(out.toString());
    	writer.close();
        
        checkoutExistingBranch(branches[0]);
        
        String commitId =
                createCommitAndPushNewFile(
                        destinationFolderForNewFile,
                        newFileName,
                        content,
                        commiterName,
                        commiterEmail,
                        content,
                        true,
                        false,
                        branches[0].split(_refsHeads)[1]);
        String shortCommitId = processCommitId(commitId, commiterName);
        checkSuccessfulyProccesedCommitThatShouldPass(
                VerigreenUtils.getVerigreenBranchName(shortCommitId),
                branches[0].split(_refsHeads)[1],
                _timeoutForTestInMilis,
                commitId,
                commiterName,
                commiterEmail,
                content,
                true);
        OutputStream output2 = new FileOutputStream(_path);
		BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(output2));
		writer2.write(outOriginal.toString());
    	writer2.close();
        
    }
    @Test
    public void testPermittedUsersChangesInRT() throws IOException, InterruptedException, RestClientException {
        
    	 String destinationFolderForNewFile = DEFAULT_COMMIT_FOLDER;
         
        String commiterName = "permitted.user@nowhere.com";
        String commiterEmail = "permitted.user@nowhere.com";
        String content = "check permitted user changes in RT";
        setPermittedUser(commiterName);
        setProtectedBranch(_masterBranch);
        checkoutExistingBranch(_masterBranch);
        String newFileName = generateFileName();
       
        String commitId =
                createCommitAndPushNewFile(
                        destinationFolderForNewFile,
                        newFileName,
                        content,
                        commiterName,
                        commiterEmail,
                        content,
                        true,
                        true,
                        _masterBranch); 
   
        assertBranchContainsCommit(commitId, _masterBranch, true);
        
        String vgBranchName = VerigreenUtils.getVerigreenBranchName(processCommitId(commitId, commiterName));
        
        CommitItemRequest commitItemRequest =
        		com.verigreen.spring.common.CollectorApi.getCommitItemRequest(vgBranchName, _masterBranch, commitId);
        
        CommitItemPresentation item = null;
        try
        {
        	RestClientImpl restClient = new RestClientImpl();
        	if(restClient.get(commitItemRequest).getStatusInfo().getStatusCode()!=404)
        	{
         		item = restClient.get(commitItemRequest).getEntity(CommitItemPresentation.class);
        	}  
            Assert.assertNull(item);
        }
        catch(Throwable ex)
        {
        	Assert.assertTrue(ex instanceof RestClientException);
        }
    }
}