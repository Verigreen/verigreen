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
package com.verigreen.collector.e2e;

import junit.framework.Assert;

import org.junit.Test;
import org.quartz.JobExecutionException;

import com.verigreen.common.concurrency.SynchronizeableThreadPoolExecutor;
import com.verigreen.buildverification.JenkinsVerifierMockFactory;
import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.common.TestCollectorConstants;
import com.verigreen.collector.jobs.ConsumerJob;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.concurrency.ExecutorServiceFactory;
import com.verigreen.common.jbosscache.Criteria;
import com.verigreen.common.testcase.CollectorUnitTestCase;
import com.verigreen.common.util.CommitItemFactory;

public class TestCollectorE2E extends CollectorUnitTestCase {
    
    @Test
    public void testE2E() throws Exception {
        
        test1Item(true, false, false, VerificationStatus.PASSED_AND_PUSHED, true);
    }
    
    @Test
    public void testE2EWithFailedMerge() throws Exception {
        
        test1Item(false, false, false, VerificationStatus.MERGE_FAILED, true);
    }
    
    @Test
    public void testE2EWithCreateBranchException() throws Exception {
        
        test1Item(true, true, false, VerificationStatus.GIT_FAILURE, true);
    }
    
    @Test
    public void testE2EWithUpdateBranchException() throws Exception {
        
        test1Item(true, false, true, VerificationStatus.GIT_FAILURE, false);
        // run decision maker again
        new ConsumerJob().execute(null);
        assertItem(TestCollectorConstants.BRANCH_1, VerificationStatus.GIT_FAILURE, true);
    }
    
    @Test
    public void testE2EWith2Items() throws Exception {
        
        createItem(TestCollectorConstants.BRANCH_1, true, false, false, true, false);
        createItem(TestCollectorConstants.BRANCH_2, true, false, false, false, true);
        // consume
        new ConsumerJob().execute(null);
        // wait for task in queue to finish
        SynchronizeableThreadPoolExecutor executor =
                (SynchronizeableThreadPoolExecutor) ExecutorServiceFactory.getCachedThreadPoolExecutor();
        executor.join();
        // run decision maker again
        new ConsumerJob().execute(null);
        assertItem(TestCollectorConstants.BRANCH_1, VerificationStatus.PASSED_AND_PUSHED, true);
        assertItem(TestCollectorConstants.BRANCH_2, VerificationStatus.PASSED_AND_PUSHED, true);
    }
    
    @Test
    public void testE2EWith2ItemsSeparately() throws Exception {
        
        JenkinsVerifierMockFactory.setHangMock();
        CommitItem item1 =
                createItem(TestCollectorConstants.BRANCH_1, true, false, false, true, true);
        // consume
        new ConsumerJob().execute(null);
        CommitItem item2 =
                createItem(TestCollectorConstants.BRANCH_2, true, false, false, true, true);
        new ConsumerJob().execute(null);
        item2 = CollectorApi.getCommitItemContainer().get(item2.getKey());
        Assert.assertEquals(item2.getParent(), item1);
        // run decision maker again
        // wait for task in queue to finish
        SynchronizeableThreadPoolExecutor executor =
                (SynchronizeableThreadPoolExecutor) ExecutorServiceFactory.getCachedThreadPoolExecutor();
        executor.join();
        new ConsumerJob().execute(null);
        item1 = CollectorApi.getCommitItemContainer().get(item1.getKey());
        item2 = CollectorApi.getCommitItemContainer().get(item2.getKey());
        assertItem(TestCollectorConstants.BRANCH_1, VerificationStatus.TIMEOUT, true);
        assertItem(TestCollectorConstants.BRANCH_2, VerificationStatus.RUNNING, false);
        Assert.assertNull(item2.getParent());
    }
    
    private void assertItem(final String branch, VerificationStatus status, boolean isDone) {
        
        CommitItem commitItem1 =
                CollectorApi.getCommitItemContainer().findByCriteria(new Criteria<CommitItem>() {
                    
                    @Override
                    public boolean match(CommitItem entity) {
                        
                        return entity.getBranchDescriptor().getNewBranch().equals(branch);
                    }
                }).get(0);
        Assert.assertEquals(status, commitItem1.getStatus());
        Assert.assertEquals(isDone, commitItem1.isDone());
        if (isDone) {
            Assert.assertNotNull(commitItem1.getEndTime());
        }
    }
    
    private void test1Item(
            boolean mergeSuccess,
            boolean createBranchThrowsException,
            boolean updateBranchThrowsException,
            VerificationStatus finalStatus,
            boolean isDone) throws JobExecutionException {
        
        createItem(
                TestCollectorConstants.BRANCH_1,
                mergeSuccess,
                createBranchThrowsException,
                updateBranchThrowsException,
                true,
                true);
        // consume
        new ConsumerJob().execute(null);
        // wait for task in queue to finish
        SynchronizeableThreadPoolExecutor executor =
                (SynchronizeableThreadPoolExecutor) ExecutorServiceFactory.getCachedThreadPoolExecutor();
        executor.join();
        // run decision maker again
        new ConsumerJob().execute(null);
        assertItem(TestCollectorConstants.BRANCH_1, finalStatus, isDone);
    }
    
    private CommitItem createItem(
            String branch,
            boolean mergeSuccess,
            boolean createBranchThrowsException,
            boolean updateBranchThrowsException,
            boolean shouldReset,
            boolean shouldReplay) {
        
        return CommitItemFactory.create(
                VerificationStatus.NOT_STARTED,
                branch,
                null,
                mergeSuccess,
                createBranchThrowsException,
                updateBranchThrowsException,
                shouldReset,
                shouldReplay,
                TestCollectorConstants.PROTECTED_BRANCH_MASTER);
    }
}
