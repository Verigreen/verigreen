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
package com.verigreen.common.concurrency.timeboundedexecuter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;

import com.verigreen.common.concurrency.ExecutorServiceFactory;
import com.verigreen.common.logger.Logger;
import com.verigreen.common.utils.Action;

/**
 * @author zilbersa
 * 
 */
public class TimeBoundedExecuter {
    
    @Autowired
    private Logger _logger;
    private final TimeBoundedPolicy _policy = new KeepWaitingPolicy<Void>();
    private ExecutorService _executerService = ExecutorServiceFactory.getCachedThreadPoolExecutor();
    
    public void setThreadPoolExecuter(ExecutorService executerService) {
        
        _executerService = executerService;
    }
    
    public <TResult> TResult perform(Action<TResult> actionDelegate, long timeBoundInMillis) {
        
        return perform(actionDelegate, timeBoundInMillis, _policy);
    }
    
    // callers of this method need to transfer an IActionDelegate, a timeout value, and a policy object.
    // the method will execute the delegate up to the given timeout. after that, 
    // the policy will decide what to do:
    public <TResult> TResult perform(
            Action<TResult> actionDelegate,
            long timeBoundInMillis,
            TimeBoundedPolicy policy) {
        
        return executeWorkInSeparateThread(actionDelegate, timeBoundInMillis, policy);
    }
    
    /**
     * Executes the given actions in parallel, returns when all actions are completed or until the
     * given timeout elapses.
     * 
     * @return List with actions results
     */
    public void perform(List<Action<Void>> actions, long timeBoundInMillis) {
        
        List<TimeBoundedThread<Void>> tasks = createTimeBoundedThreads(actions);
        try {
            _executerService.invokeAll(tasks, timeBoundInMillis, TimeUnit.MILLISECONDS);
        } catch (Throwable thrown) {
            _logger.error(String.format(
                    "Got unexpected error while performing the following actions: %s",
                    actions), thrown);
        }
    }
    
    private <TResult> List<TimeBoundedThread<TResult>> createTimeBoundedThreads(
            List<Action<TResult>> actionDelegate) {
        
        List<TimeBoundedThread<TResult>> tasks = new ArrayList<>();
        for (Action<TResult> action : actionDelegate) {
            tasks.add(new TimeBoundedThread<>(action));
        }
        
        return tasks;
    }
    
    private <T> T executeWorkInSeparateThread(
            final Action<T> actionDelegate,
            long timeBoundInMillis,
            TimeBoundedPolicy policy) {
        
        TimeBoundedThread<T> timeBoundedThread = new TimeBoundedThread<>(actionDelegate);
        Future<T> future = _executerService.submit(timeBoundedThread);
        T retVal = loopWhileStillNotFinished(actionDelegate, timeBoundInMillis, future, policy);
        
        return retVal;
    }
    
    private <T> T loopWhileStillNotFinished(
            final Action<T> actionDelegate,
            long timeBoundInMillis,
            Future<T> future,
            TimeBoundedPolicy policy) {
        
        int times = 1;
        while (true) {
            try {
                
                return future.get(timeBoundInMillis, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // was interrupted; should cancel the execution.
                future.cancel(true);
                throw new CancellationException();
            } catch (ExecutionException e) {
                // execution ended with an exception.
                _logger.error(
                        "Catastrophic failure when executing a TimeBoundedThread! Exception details: "
                                + e.toString(),
                        e);
                throw new RuntimeException(e);
            } catch (TimeoutException e) {
                // timed out
                reportAndActAccordingToTimeoutOption(
                        actionDelegate,
                        timeBoundInMillis,
                        future,
                        times,
                        policy);
                times += 1;
            } catch (CancellationException e) {
                // was canceled
                throw e;
            }
        }
    }
    
    private <T> void reportAndActAccordingToTimeoutOption(
            final Action<T> actionDelegate,
            long timeBoundInMillis,
            Future<T> future,
            int times,
            TimeBoundedPolicy policy) {
        
        String message =
                String.format(
                        "%s is still running after %d. TimeoutOption=%s. Attempt number %d.",
                        actionDelegate.toString(),
                        timeBoundInMillis,
                        policy.getName(),
                        times);
        _logger.warning(message);
        
        policy.act(future);
    }
    
    private class TimeBoundedThread<T> implements Callable<T> {
        
        private final Action<T> _actionDelegate;
        
        public TimeBoundedThread(final Action<T> actionDelegate) {
            
            _actionDelegate = actionDelegate;
        }
        
        @Override
        public T call() throws Exception {
            
            long startTimeNanos = System.nanoTime();
            try {
                
                return _actionDelegate.action();
            } finally {
                long endTimeNanos = System.nanoTime();
                long totalTimeNanos = endTimeNanos - startTimeNanos;
                _logger.debug(String.format(
                        "Finished %s; took %d milliseconds.",
                        _actionDelegate.toString(),
                        totalTimeNanos / 1000000));
            }
        }
    }
}
