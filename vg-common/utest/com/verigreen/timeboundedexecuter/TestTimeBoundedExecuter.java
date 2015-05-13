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
package com.verigreen.timeboundedexecuter;

import java.util.concurrent.CancellationException;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.verigreen.common.concurrency.timeboundedexecuter.AbandonPolicy;
import com.verigreen.common.concurrency.timeboundedexecuter.AbandonedException;
import com.verigreen.common.concurrency.timeboundedexecuter.CancelPolicy;
import com.verigreen.common.concurrency.timeboundedexecuter.KeepWaitingPolicy;
import com.verigreen.common.concurrency.timeboundedexecuter.TimeBoundedExecuter;
import com.verigreen.common.utils.Action;
import com.verigreen.common.spring.SpringTestCase;

@ContextConfiguration(locations = {
        "/Spring/qcext-pclab-timeboundedexecuter-context.xml",
        "/Spring/qcext-pclab-common-context.xml" })
public class TestTimeBoundedExecuter extends SpringTestCase {
    
    @Autowired
    private TimeBoundedExecuter _timeBoundedExecuter;
    private static final int LONG_TIMEOUT = 500000;
    private static final int SHORT_TIMEOUT = 100;
    private static final int SLOW_PERFORM_TIME = 500;
    
    class InterruptingPerformer implements Action<Void> {
        
        private final Thread thread;
        
        public InterruptingPerformer(Thread thread) {
            
            this.thread = thread;
        }
        
        @Override
        public Void action() {
            
            thread.interrupt();
            
            return null;
        }
    }
    
    class SlowPerformer implements Action<Void> {
        
        private final int millisToSleep;
        private boolean done = false;
        
        public boolean isDone() {
            
            return done;
        }
        
        public SlowPerformer(int millisToSleep) {
            
            this.millisToSleep = millisToSleep;
        }
        
        @Override
        public Void action() {
            
            try {
                Thread.sleep(millisToSleep);
            } catch (InterruptedException e) {
                
                return null;
            }
            this.done = true;
            
            return null;
        }
        
        @Override
        public String toString() {
            
            return this.getClass().getName();
        }
    }
    
    class ExceptionThrowingPerformer implements Action<Integer> {
        
        @Override
        public Integer action() {
            
            throw new RuntimeException("Thrown by ExceptionThrowingPerformer.");
        }
        
        @Override
        public String toString() {
            
            return this.getClass().getName();
        }
    }
    
    @Test
    public void testKeepWaiting() {
        // interrupt  
        SlowPerformer slowPerformer = new SlowPerformer(SLOW_PERFORM_TIME);
        _timeBoundedExecuter.perform(slowPerformer, SHORT_TIMEOUT, new KeepWaitingPolicy<Void>());
        Assert.assertTrue(slowPerformer.isDone());
    }
    
    @Test
    public void testAbandon() {
        // interrupt  
        SlowPerformer slowPerformer = new SlowPerformer(SLOW_PERFORM_TIME);
        boolean abandoned = false;
        try {
            _timeBoundedExecuter.perform(slowPerformer, SHORT_TIMEOUT, new AbandonPolicy());
        } catch (AbandonedException e) {
            abandoned = true;
        }
        Assert.assertTrue(abandoned);
    }
    
    @Test
    public void testCancel() {
        // interrupt  
        SlowPerformer slowPerformer = new SlowPerformer(SLOW_PERFORM_TIME);
        boolean canceled = false;
        try {
            _timeBoundedExecuter.perform(slowPerformer, SHORT_TIMEOUT, new CancelPolicy());
        } catch (CancellationException ex) {
            canceled = true;
        }
        Assert.assertTrue(canceled);
        Assert.assertFalse(slowPerformer.isDone());
    }
    
    @Test
    public void testExceptionThrown() {
        
        boolean thrown = false;
        try {
            _timeBoundedExecuter.perform(
                    new ExceptionThrowingPerformer(),
                    LONG_TIMEOUT,
                    new KeepWaitingPolicy<Void>());
        } catch (Exception e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }
    
    @Test
    public void testInterrupt() {
        // interrupt  
        InterruptingPerformer interruptingPerformer =
                new InterruptingPerformer(Thread.currentThread());
        boolean canceled = false;
        try {
            _timeBoundedExecuter.perform(
                    interruptingPerformer,
                    LONG_TIMEOUT,
                    new KeepWaitingPolicy<Void>());
        } catch (CancellationException ex) {
            canceled = true;
        }
        Assert.assertTrue(canceled);
    }
}
