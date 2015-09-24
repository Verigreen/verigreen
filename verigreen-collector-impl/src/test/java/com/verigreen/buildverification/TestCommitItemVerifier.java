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
package com.verigreen.buildverification;

import junit.framework.Assert;

import org.junit.Test;

import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.buildverification.CommitItemVerifier;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.concurrency.ExecutorServiceFactory;
import com.verigreen.common.concurrency.SynchronizeableThreadPoolExecutor;
import com.verigreen.common.testcase.CollectorUnitTestCase;
import com.verigreen.common.util.CommitItemFactory;

public class TestCommitItemVerifier extends CollectorUnitTestCase {
    
    @Test
    public void testVerify() {
        
        CommitItem item = CommitItemFactory.create(VerificationStatus.RUNNING);
        CollectorApi.getCommitItemVerifier().verify(item);
        SynchronizeableThreadPoolExecutor executor =
                (SynchronizeableThreadPoolExecutor) ExecutorServiceFactory.getCachedThreadPoolExecutor();
        executor.join();
        item = CollectorApi.getCommitItemContainer().get(item.getKey());
        Assert.assertEquals(VerificationStatus.PASSED, item.getStatus());
    }
    
    @Test
    public void testVerifyWithCancel() {
        
        CommitItem item = CommitItemFactory.create(VerificationStatus.RUNNING);
        CommitItemVerifier commitItemVerifier = CollectorApi.getCommitItemVerifier();
        commitItemVerifier.verify(item);
        boolean result = CollectorApi.getJenkinsVerifier().stop(CollectorApi.getVerificationJobName(), String.valueOf(item.getBuildNumber()));
        SynchronizeableThreadPoolExecutor executor =
                (SynchronizeableThreadPoolExecutor) ExecutorServiceFactory.getCachedThreadPoolExecutor();
        executor.join();
        item = CollectorApi.getCommitItemContainer().get(item.getKey());
        Assert.assertEquals(true, result);
        Assert.assertEquals(VerificationStatus.RUNNING, item.getStatus());
    }
    
    @Test
    public void testVerifyWithTimeout() {
        
        CommitItem item = CommitItemFactory.create(VerificationStatus.RUNNING);
        CommitItemVerifier commitItemVerifier = CollectorApi.getCommitItemVerifier();
        commitItemVerifier.verify(item);
        SynchronizeableThreadPoolExecutor executor =
                (SynchronizeableThreadPoolExecutor) ExecutorServiceFactory.getCachedThreadPoolExecutor();
        executor.join();
        item = CollectorApi.getCommitItemContainer().get(item.getKey());
        Assert.assertEquals(VerificationStatus.TIMEOUT, item.getStatus());
    }
}
