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

import junit.framework.Assert;

import org.junit.Test;

import com.verigreen.common.concurrency.SynchronizeableThreadPoolExecutor;
import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.buildverification.CommitItemVerifier;
import com.verigreen.collector.decision.CheckinDecisionHandler;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.concurrency.ExecutorServiceFactory;
import com.verigreen.common.testcase.CollectorUnitTestCase;
import com.verigreen.common.util.CommitItemFactory;

public class TestCheckinDecisionHandler extends CollectorUnitTestCase {
    
    @Test
    public void testHandle() {
        
        CommitItem item = CommitItemFactory.create(VerificationStatus.NOT_STARTED);
        CheckinDecisionHandler handler = CollectorApi.getCheckinDecisionHandler(item);
        handler.handle();
        Assert.assertEquals(VerificationStatus.RUNNING, item.getStatus());
        CommitItemVerifier verifier =
                CollectorApi.getCommitItemVerifierManager().get(item.getKey());
        Assert.assertNotNull(verifier);
        SynchronizeableThreadPoolExecutor executor =
                (SynchronizeableThreadPoolExecutor) ExecutorServiceFactory.getCachedThreadPoolExecutor();
        executor.join();
        item = CollectorApi.getCommitItemContainer().get(item.getKey());
        Assert.assertEquals(VerificationStatus.PASSED, item.getStatus());
    }
    
    @Test
    public void testHandleWithCancel() {
        
        CommitItem item = CommitItemFactory.create(VerificationStatus.NOT_STARTED);
        CheckinDecisionHandler handler = CollectorApi.getCheckinDecisionHandler(item);
        handler.handle();
        Assert.assertEquals(VerificationStatus.RUNNING, item.getStatus());
        CommitItemVerifier verifier =
                CollectorApi.getCommitItemVerifierManager().get(item.getKey());
        Assert.assertNotNull(verifier);
        boolean result = CollectorApi.getJenkinsVerifier().stop(CollectorApi.getVerificationJobName(), String.valueOf(item.getBuildNumber()));
        SynchronizeableThreadPoolExecutor executor =
                (SynchronizeableThreadPoolExecutor) ExecutorServiceFactory.getCachedThreadPoolExecutor();
        executor.join();
        Assert.assertTrue(result);
    }
    
    @Test
    public void testHandleWithCancelAndRerun() {
        
        CommitItem item = CommitItemFactory.create(VerificationStatus.NOT_STARTED);
        CheckinDecisionHandler handler = CollectorApi.getCheckinDecisionHandler(item);
        handler.handle();
        Assert.assertEquals(VerificationStatus.RUNNING, item.getStatus());
        CommitItemVerifier verifier =
                CollectorApi.getCommitItemVerifierManager().get(item.getKey());
        Assert.assertNotNull(verifier);
        boolean result = CollectorApi.getJenkinsVerifier().stop(CollectorApi.getVerificationJobName(), String.valueOf(item.getBuildNumber()));;
        SynchronizeableThreadPoolExecutor executor =
                (SynchronizeableThreadPoolExecutor) ExecutorServiceFactory.getCachedThreadPoolExecutor();
        executor.join();
        Assert.assertTrue(result);
        handler.handle();
        CommitItemVerifier verifier2 =
                CollectorApi.getCommitItemVerifierManager().get(item.getKey());
        Assert.assertNotSame(verifier, verifier2);
        executor.join();
        Assert.assertFalse(!result);
        item = CollectorApi.getCommitItemContainer().get(item.getKey());
        Assert.assertEquals(VerificationStatus.PASSED, item.getStatus());
    }
}
