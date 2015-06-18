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
package com.verigreen.collector.common;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.verigreen.collector.api.BranchDescriptor;
import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.api.VerigreenNeeded;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.testcase.CollectorUnitTestCase;
import com.verigreen.common.util.CommitItemFactory;
import com.verigreen.common.util.SourceControlOperatorMock;

public class TestVerigreenNeededLogic extends CollectorUnitTestCase {
    
    private static final String protectedBranch = "refs/heads/master";
    private static final String branch = "test";
    
    @Override
    @Before
    public void setUp() {
        
        super.setUp();
        SourceControlOperatorMock sourceControlOperator =
                (SourceControlOperatorMock) CollectorApi.getSourceControlOperator();
        sourceControlOperator.reset();
        sourceControlOperator.setMockBehaviour(new BranchDescriptor(), branch, true, false, false);
        sourceControlOperator.replay();
    }
    
    @Test
    public void testIsVerigreenNeededBranchNotProtected() {
        
        VerigreenNeeded verigreenNeeded =
                CollectorApi.getVerigreenNeededLogic().isVerigreenNeeded(
                        "not-protected",
                        branch,
                        "",
                        "user1");
        Assert.assertEquals(false, verigreenNeeded.getVerigreenNeeded());
        Assert.assertEquals(false, verigreenNeeded.getShouldRejectCommit());
    }
    
    @Test
    public void testIsVerigreenNeededCommitPassed() {
        
        CommitItemFactory.create(VerificationStatus.PASSED, branch, null);
        VerigreenNeeded verigreenNeeded =
                CollectorApi.getVerigreenNeededLogic().isVerigreenNeeded(
                        protectedBranch,
                        branch,
                        "",
                        "user1");
        Assert.assertEquals(false, verigreenNeeded.getVerigreenNeeded());
        Assert.assertEquals(false, verigreenNeeded.getShouldRejectCommit());
    }
    
    @Test
    public void testIsVerigreenNeededCommitNull() {
        
        VerigreenNeeded verigreenNeeded =
                CollectorApi.getVerigreenNeededLogic().isVerigreenNeeded(
                        protectedBranch,
                        branch,
                        "",
                        "user1");
        Assert.assertEquals(true, verigreenNeeded.getVerigreenNeeded());
        Assert.assertEquals(true, verigreenNeeded.getShouldRejectCommit());
    }
    
    @Test
    public void testIsVerigreenNeededCommitRunning() {
        
        CommitItemFactory.create(VerificationStatus.RUNNING, branch, null);
        VerigreenNeeded verigreenNeeded =
                CollectorApi.getVerigreenNeededLogic().isVerigreenNeeded(
                        protectedBranch,
                        branch,
                        "",
                        "user1");
        Assert.assertEquals(false, verigreenNeeded.getVerigreenNeeded());
        Assert.assertEquals(true, verigreenNeeded.getShouldRejectCommit());
    }
    
    @Test
    public void testIsVerigreenNeededPermittedUser() {
        String pUser= VerigreenNeededLogic.properties.getProperty("git.permittedUsers");
        VerigreenNeeded verigreenNeeded =
                CollectorApi.getVerigreenNeededLogic().isVerigreenNeeded(
                        protectedBranch,
                        branch,
                        "",
                        pUser);
        Assert.assertEquals(false, verigreenNeeded.getVerigreenNeeded());
        Assert.assertEquals(false, verigreenNeeded.getShouldRejectCommit());
    }
}
