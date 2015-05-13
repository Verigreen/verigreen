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
package com.verigreen.collector.buildverification;

import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.concurrency.ExecutorServiceFactory;
import com.verigreen.common.concurrency.RuntimeUtils;
import com.verigreen.common.utils.StringUtils;

public class CommitItemVerifier {
    
    private boolean _canceled = false;
    private int _timeoutInMillis;
    private int _pollTimeMillis = 10 * 1000;
    private String _commitItemKey = StringUtils.EMPTY_STRING;
    
    public void verify(final CommitItem item) {
        
        _commitItemKey = item.getKey();
        final Future<BuildVerificationResult> future =
                verifyAsync("origin/" + item.getMergedBranchName());
        ExecutorServiceFactory.fireAndForget(new Runnable() {
            
            @Override
            public void run() {
                
                waitForResult(future);
            }
        });
    }
    
    public void cancel() {
        
        _canceled = true;
        if (!StringUtils.isNullOrEmpty(_commitItemKey)) {
            CommitItem commitItem = CollectorApi.getCommitItemContainer().get(_commitItemKey);
            VerigreenLogger.get().log(
                    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format("cancelling verification of %s...", commitItem));
            CollectorApi.getJenkinsVerifier().stop(
                    CollectorApi.getVerificationJobName(),
                    String.valueOf(commitItem.getBuildNumber()));
        }
    }
    
    public void setTimeoutInMillis(int timeoutInMillis) {
        
        _timeoutInMillis = timeoutInMillis;
    }
    
    public void setPollTimeMillis(int pollTimeMillis) {
        
        _pollTimeMillis = pollTimeMillis;
    }
    
    public boolean isCanceled() {
        
        return _canceled;
    }
    
    private void waitForResult(Future<BuildVerificationResult> future) {
        
        CommitItem commitItem = CollectorApi.getCommitItemContainer().get(_commitItemKey);
        BuildVerificationResult result = null;
        long timeOut = System.currentTimeMillis() + _timeoutInMillis;
        try {
            do {
                try {
                    result = future.get(_pollTimeMillis, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    VerigreenLogger.get().log(
                            getClass().getName(),
                            RuntimeUtils.getCurrentMethodName(),
                            String.format(
                                    "commit item is still in the process of verification %s...",
                                    commitItem));
                }
            } while (result == null && !_canceled && !hasTimedOut(timeOut));
        } catch (Throwable thrown) {
            VerigreenLogger.get().error(
                    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format("Unexpected failure during verification of %s", commitItem),
                    thrown);
        }
        if (!_canceled) {
            commitItem = CollectorApi.getCommitItemContainer().get(_commitItemKey);
            VerificationStatus finalStatus =
                    result == null ? VerificationStatus.TIMEOUT : result.getStatus();
            commitItem.setStatus(finalStatus);
            CollectorApi.getCommitItemContainer().save(commitItem);
        }
    }
    
    private boolean hasTimedOut(long timeOut) {
        
        return System.currentTimeMillis() > timeOut;
    }
    
    private Future<BuildVerificationResult> verifyAsync(final String branchName) {
        
        return ExecutorServiceFactory.getCachedThreadPoolExecutor().submit(
                new Callable<BuildVerificationResult>() {
                    
                    @Override
                    public BuildVerificationResult call() {
                        
                        BuildVerificationResult result =
                                CollectorApi.getJenkinsVerifier().BuildAndVerify(
                                        CollectorApi.getVerificationJobName(),
                                        CollectorApi.getBranchParamName(),
                                        branchName,
                                        getCallback());
                        
                        return result;
                    }
                });
    }
    
    private BuildDataCallback getCallback() {
        
        return new BuildDataCallback() {
            
            @Override
            public void buildStarted(URI buildUrl, int buildNumber) {
                
                CommitItem commitItem = CollectorApi.getCommitItemContainer().get(_commitItemKey);
                commitItem.setBuildUrl(buildUrl);
                commitItem.setBuildNumber(buildNumber);
                CollectorApi.getCommitItemContainer().save(commitItem);
            }
        };
    }
}
