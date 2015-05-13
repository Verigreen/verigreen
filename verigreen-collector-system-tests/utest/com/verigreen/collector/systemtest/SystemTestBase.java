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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jgit.lib.PersonIdent;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.test.context.ContextConfiguration;

import com.verigreen.common.spring.SpringTestCase;
import com.verigreen.collector.api.Branches;
import com.verigreen.collector.api.CommitItemPresentation;
import com.verigreen.collector.api.Users;
import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.rest.requests.PermittedUserRequest;
import com.verigreen.collector.rest.requests.ProtectedBranchesRequest;
import com.verigreen.common.utils.PropertiesUtils;
import com.verigreen.jgit.JGitOperator;
import com.verigreen.jgit.SourceControlOperator;
import com.verigreen.rest.CommitItemRequest;
import com.verigreen.spring.common.CollectorApi;
import com.verigreen.restclient.RestClient;
import com.verigreen.restclient.RestClientImpl;


@ContextConfiguration(locations = {
        "/Spring/verigreen-collector-systemtest.xml",
        "/Spring/verigreen-collector-jbosscache-test-context.xml" })
public abstract class SystemTestBase extends SpringTestCase {
    
    protected static final String DEFAULT_COMMIT_FOLDER = "trashFolder";
    
    private static final String path = System.getenv("VG_HOME");
    private static final String DEFAULT_TEST_PROPERTIES_FILE_PATH =
    		path + "//system-tests.properties";
    
    protected static String _refsHeads;
    protected static String _refsRemotes;
    protected static String _masterBranch;
    protected static String _userGitRepositoryPath;
    protected SourceControlOperator _sourceControlOperator;
    protected static SourceControlOperator _localSourceControlOperator;
    protected String _localRepositoryFolder;
    
    String processCommitId(String commitId, String userName)
    {
    	String removeName = userName.replace(", ", "_");
    	removeName = removeName.replace(" ", "_");
    	String commitIdProcessed = commitId.substring(0,7)+"_"+removeName;
    	return commitIdProcessed;
    }
    
    
    private static final Set<VerificationStatus> _passedVerificationStatuses;

    
    static {
    	_passedVerificationStatuses = new HashSet<>(2);
    	_passedVerificationStatuses.add(VerificationStatus.PASSED_AND_PUSHED);
    	_passedVerificationStatuses.add(VerificationStatus.PASSED_BY_CHILD);
    }
    @Before
    public void setUp() throws FileNotFoundException, IOException {
        initFromPropertiesFile();
        _sourceControlOperator = createSourceControlOperator(_userGitRepositoryPath);
        _localRepositoryFolder =
                Paths.get(_sourceControlOperator.getPathOfLocalRepository()).getParent().toString();
        _sourceControlOperator.fetch();
        _sourceControlOperator.rebase(_refsRemotes + _masterBranch);
    }
    
    private static SourceControlOperator createSourceControlOperator(String userGitRepositoryPath) {
        return new JGitOperator(_userGitRepositoryPath);
    }
    
    private static void initFromPropertiesFile() throws FileNotFoundException, IOException {
        if (_refsHeads != null) {
            return;
        }
        
        String propertiesFilePath = System.getProperty("tests.properties.path");
        if (propertiesFilePath == null) {
            propertiesFilePath = DEFAULT_TEST_PROPERTIES_FILE_PATH;
        }
        
        System.out.println(System.getProperty("user.dir"));
        Properties props = new Properties();
        props.load(new FileInputStream(propertiesFilePath));
        
        _refsHeads =
                PropertiesUtils.getPropertyOverridenByEnvVar(props, "refs.heads", "refs/heads/");
        _refsRemotes =
                PropertiesUtils.getPropertyOverridenByEnvVar(
                        props,
                        "refs.remotes",
                        "refs/remotes/origin/");
        _masterBranch =
                PropertiesUtils.getPropertyOverridenByEnvVar(props, "branch.master", "master");
        _userGitRepositoryPath =
                PropertiesUtils.getPropertyOverridenByEnvVar(props, "user.git.repository", null);
        if (_userGitRepositoryPath == null) {
            throw new IllegalArgumentException("Undeclared 'user.git.repository' property");
        }
    }
    
    protected boolean checkIfFinalState(
            CommitItemRequest verificationResultRequest,
            RestClient restClient) {
        
        CommitItemPresentation item =
                restClient.get(verificationResultRequest).getEntity(CommitItemPresentation.class);
        
        return item.getStatus().isFinalState();
    }
    
    protected String createCommitAndPushNewFile(
            String destinationFolderForNewFile,
            String filename,
            String content,
            String commiterName,
            String commiterEmail,
            String commitMessage,
            boolean shouldAddTheFile,
            boolean pushResult,
            String branchName) throws IOException {
        
        addNewFile(destinationFolderForNewFile + "/" + filename, content);
        
        return commitAndPush(
                destinationFolderForNewFile,
                commiterName,
                commiterEmail,
                commitMessage,
                shouldAddTheFile,
                pushResult,
                branchName);
    }
    
    protected String commitAndPush(
            String destinationFolderForNewFile,
            String commiterName,
            String commiterEmail,
            String commitMessage,
            boolean shouldAddTheFile,
            boolean pushResult,
            String branchName) {
        
        String commitId;
        if (shouldAddTheFile) {
            _sourceControlOperator.add(destinationFolderForNewFile);
        }
        commitId = _sourceControlOperator.commit(commiterName, commiterEmail, commitMessage);
        _sourceControlOperator.fetch();
        boolean rebase = _sourceControlOperator.rebase(branchName);
        Assert.assertTrue(rebase);
        boolean push =
                _sourceControlOperator.push(
                        _refsHeads.concat(branchName),
                        _refsHeads.concat(branchName));
        Assert.assertEquals(pushResult, push);
        return commitId;
    }
    
    protected String commitAndPush(
            String destinationFolderForNewFile,
            String commiterName,
            String commiterEmail,
            String commitMessage,
            boolean shouldAddTheFile,
            String branchName) {
        
        return commitAndPush(
                destinationFolderForNewFile,
                commiterName,
                commiterEmail,
                commitMessage,
                shouldAddTheFile,
                false,
                branchName);
    }
    
    protected void checkSuccessfulyProccesedCommitThatShouldPass(
            String vgBranchName,
            String protectedBranch,
            long timeoutForTestInMilis,
            String commitId,
            String expectedCommiterName,
            String expectedCommiterEmail,
            String expectedCommitMessage,
            boolean includingGitCheck) throws InterruptedException {
        
        waitForVerificationComplete(
                vgBranchName,
                getBranchRefsHeadFullName(protectedBranch),
                timeoutForTestInMilis,
                commitId,
                _passedVerificationStatuses);
        if (includingGitCheck) {
            assertBranchContainsCommit(commitId, protectedBranch, true);
        }
        
        assertCommit(commitId, expectedCommiterName, expectedCommiterEmail, expectedCommitMessage);
    }
    
    protected void assertBranchContainsCommit(
            String commitId,
            String branchName,
            boolean checkContains) {
        checkoutExistingBranch(branchName);
        
        String refsRemotesBranch = getBranchRefsRemotesFullName(branchName);
        _sourceControlOperator.fetch();
        _sourceControlOperator.rebase(refsRemotesBranch);
        Assert.assertEquals(false, ((JGitOperator) _sourceControlOperator).isThereAnyDifs());
        boolean isBranchContainsCommit =
                _sourceControlOperator.isBranchContainsCommit(refsRemotesBranch, commitId);
        
        String errorMessage =
                checkContains ? String.format(
                        "Commit ID: %s does not exist in branch: %s, while it should have been",
                        commitId,
                        branchName) : String.format(
                        "Commit ID: %s exists in branch: %s, while it should not have been",
                        commitId,
                        branchName); 
        Assert.assertEquals(errorMessage, checkContains, isBranchContainsCommit);
    }
    
   
    private void waitForVerificationComplete(
            String vgBranchname,
            String protectedBranch,
            long timeoutForTestInMilis,
            String commitId,
            Set<VerificationStatus> expectedStatuses) throws InterruptedException {
        
        RestClient restClient = new RestClientImpl();
        CommitItemRequest verificationResultRequest =
                CollectorApi.getCommitItemRequest(vgBranchname, protectedBranch, commitId);
        Assert.assertNotNull(verificationResultRequest);
        long startTime = System.currentTimeMillis();
        
        boolean finalStateReached = checkIfFinalState(verificationResultRequest, restClient);
        while (!finalStateReached
               && (System.currentTimeMillis() < (startTime + timeoutForTestInMilis))) {
            Thread.sleep(1000 * 10);
            finalStateReached = checkIfFinalState(verificationResultRequest, restClient);
        }
        
        Assert.assertTrue(String.format(
                "Timeout occurred... Checking commit id: %s, in branch: %s",
                commitId,
                protectedBranch), finalStateReached);
        
        VerificationStatus resultStatus =
                restClient.get(verificationResultRequest).getEntity(CommitItemPresentation.class).getStatus();
        Assert.assertTrue(
                String.format(
                        "Expected statuses: %s, result status: %s",
                        expectedStatuses.toString(),
                        resultStatus),
                expectedStatuses.contains(resultStatus));
    }
    private void waitForVerificationComplete(
            String vgBranchName,
            String protectedBranch,
            long timeoutForTestInMilis,
            String commitId,
            VerificationStatus expectedStatus) throws InterruptedException {
        Set<VerificationStatus> expectedStatuses = new HashSet<>(1);
        expectedStatuses.add(expectedStatus);
        waitForVerificationComplete(
                vgBranchName,
                getBranchRefsHeadFullName(protectedBranch),
                timeoutForTestInMilis,
                commitId,
                expectedStatuses);
    }
    
    
    protected void checkSuccessfulyProccesedCommitThatShouldFail(
            String vgBranchName,
            String protectedBranch,
            long timeoutForTestInMilis,
            String commitId,
            String expectedCommiterName,
            String expectedCommiterEmail,
            String expectedCommitMessage,
            VerificationStatus expectedStatus,
            boolean includingGitCheck) throws InterruptedException {
        
        waitForVerificationComplete(
                vgBranchName,
                protectedBranch,
                timeoutForTestInMilis,
                commitId,
                expectedStatus);
        
        if (includingGitCheck) {
            assertBranchContainsCommit(commitId, protectedBranch, false);
        }
        assertCommit(commitId, expectedCommiterName, expectedCommiterEmail, expectedCommitMessage);
    }
    
    private void assertCommit(
            String commitId,
            String expectedCommiterName,
            String expectedCommiterEmail,
            String expectedCommitMessage) {
        
        PersonIdent committerIdent =
                ((JGitOperator) _sourceControlOperator).getRevCommit(commitId).getCommitterIdent();
        String commitMessage =
                ((JGitOperator) _sourceControlOperator).getRevCommit(commitId).getFullMessage();
        
        Assert.assertEquals(expectedCommiterName, committerIdent.getName());
        Assert.assertEquals(expectedCommiterEmail, committerIdent.getEmailAddress());
        Assert.assertEquals(expectedCommitMessage, commitMessage);
    }
    
    protected void addNewFile(String filePath, String content) throws IOException {
        
        File newFileToAdd = new File(_localRepositoryFolder + "/" + filePath);
        newFileToAdd.getParentFile().mkdirs();
        BufferedWriter output = new BufferedWriter(new FileWriter(newFileToAdd, true));
        output.append(content);
        output.close();
    }
    
    protected static String getBranchRefsHeadFullName(String branchName) {
        return _refsHeads + branchName;
    }
    
    protected static String getBranchRefsRemotesFullName(String branchName) {
        return _refsRemotes + branchName;
    }
    
    protected void createAndPushBranches(String[] branches) {
        for (String branch : branches) {
            _sourceControlOperator.checkout(branch, true, false);
            
            // creation of branches should bypass verigreen
            boolean pushed =
                    _sourceControlOperator.push(
                            getBranchRefsHeadFullName(branch),
                            getBranchRefsHeadFullName(branch));
            Assert.assertTrue(pushed);
        }
    }
    
    protected void setProtectedBranch(String branchToSet) {
        setProtectedBranches(new String[] { branchToSet });
    }
    
    protected void setProtectedBranches(String[] branchesToSet) {
        Branches branches = new Branches();
        branches.setBranches(Arrays.asList(branchesToSet));
        
        new RestClientImpl().put(new ProtectedBranchesRequest(
                CollectorApi.getCollectorAddress(),
                branches));
    }
    protected void setPermittedUser(String userToSet) {
        setPermittedUsers(new String[] { userToSet });
    }
    
    protected void setPermittedUsers(String[] usersToSet) {
        Users users = new Users();
        users.setUsers(Arrays.asList(usersToSet));
        
        new RestClientImpl().put(new PermittedUserRequest(
                CollectorApi.getCollectorAddress(),
                users));
    }

    
    protected static String generateUserName() {
        return "user_" + UUID.randomUUID().toString().substring(0,7);
    }
    
    protected static String[] generateUserNames(int count) {
        String[] userNames = new String[count];
        for (int i = 0; i < count; i++) {
            userNames[i] = generateUserName();
        }
        
        return userNames;
    }
    
    protected static String generateEmailAddress() {
        return "email_" + UUID.randomUUID().toString().substring(0,7) + "@nowhere.com";
    }
    
    protected static String[] generateEmailAddresses(int count) {
        String[] emails = new String[count];
        for (int i = 0; i < count; i++) {
            emails[i] = generateEmailAddress();
        }
        
        return emails;
    }
    
    protected static String generateFileName() {
        return "jgitTest_" + UUID.randomUUID().toString() + ".txt";
    }
    
    protected static String[] generateFileNames(int count) {
        String[] fileNames = new String[count];
        for (int i = 0; i < count; i++) {
            fileNames[i] = generateFileName();
        }
        
        return fileNames;
    }
    
    protected static String getContent(String methodName) {
        return methodName + ": system test - " + UUID.randomUUID().toString();
    }
    
    protected static String[] getContents(String methodName, int count) {
        String[] contents = new String[count];
        for (int i = 0; i < count; i++) {
            contents[i] = generateFileName();
        }
        
        return contents;
    }
    
    /**
     * @param destinationFolderForNewFile
     * @param newFileName
     * @throws IOException
     */
    protected void appendToFile(String pathToFile, String content) throws IOException {
        BufferedWriter outputCommiter3 = new BufferedWriter(new FileWriter(pathToFile, true));
        outputCommiter3.newLine();
        outputCommiter3.append(content);
        outputCommiter3.close();
    }
    
    protected void checkoutExistingBranch(String branchName) {
        _sourceControlOperator.checkout(branchName, false, false);
    }
    
    protected String constructLocalFilePath(String folder, String fileName) {
        return _localRepositoryFolder + "/" + folder + "/" + fileName;
    }
    
    protected static String[] generateBranchNames(int numOfBranches) {
        String[] branchNames = new String[numOfBranches];
        for (int i = 0; i < numOfBranches; i++) {
            branchNames[i] = "vg_test_" + UUID.randomUUID().toString().substring(0,7);
        }
        return branchNames;
    }
    
    /**
     * This is a helper structure to contain a pair of commit id and message of the commit
     */
    protected static class CommitIdAndMessage {
        
        String message;
        String commitId;
    }
}
