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
package com.verigreen.collector.decision.decisionmaker;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.decision.CheckinDecisionHandler;
import com.verigreen.collector.decision.Decision;
import com.verigreen.collector.decision.DecisionHandler;
import com.verigreen.collector.decision.OnFailedByParentHandler;
import com.verigreen.collector.decision.OnFailureHandler;
import com.verigreen.collector.decision.OnSuccessHandler;
import com.verigreen.collector.decision.decisionmaker.DecisionMaker;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.testcase.CollectorUnitTestCase;
import com.verigreen.common.util.CommitItemFactory;

public class TestDecisionMaker extends CollectorUnitTestCase {
    
    @Test
    public void test1NotStartedItem() {
        
        CommitItem commitItem = CommitItemFactory.create(VerificationStatus.NOT_STARTED);
        List<Decision> decisions = new DecisionMaker().decide(Arrays.asList(commitItem));
        Assert.assertEquals(1, decisions.size());
        assertDecision(commitItem, decisions.get(0), CheckinDecisionHandler.class);
    }
    
    @Test
    public void test2NotStarted() {
        
        CommitItem commitItem1 = CommitItemFactory.create(VerificationStatus.NOT_STARTED);
        CommitItem commitItem2 = CommitItemFactory.create(VerificationStatus.NOT_STARTED);
        List<Decision> decisions =
                new DecisionMaker().decide(Arrays.asList(commitItem1, commitItem2));
        Assert.assertEquals(2, decisions.size());
        assertDecision(commitItem1, decisions.get(0), CheckinDecisionHandler.class);
        assertDecision(commitItem2, decisions.get(1), CheckinDecisionHandler.class);
    }
    
    @Test
    public void test1NotStarted1Running() {
        
        CommitItem commitItem1 = CommitItemFactory.create(VerificationStatus.NOT_STARTED);
        CommitItem commitItem2 = CommitItemFactory.create(VerificationStatus.RUNNING, commitItem1);
        List<Decision> decisions =
                new DecisionMaker().decide(Arrays.asList(commitItem1, commitItem2));
        Assert.assertEquals(1, decisions.size());
        assertDecision(commitItem1, decisions.get(0), CheckinDecisionHandler.class);
    }
    
    @Test
    public void test1Passed() {
        
        CommitItem commitItem1 = CommitItemFactory.create(VerificationStatus.PASSED);
        List<Decision> decisions = new DecisionMaker().decide(Arrays.asList(commitItem1));
        Assert.assertEquals(1, decisions.size());
        assertDecision(commitItem1, decisions.get(0), OnSuccessHandler.class);
        Assert.assertTrue(commitItem1.isDone());
    }
    
    @Test
    public void test1Failed() {
        
        CommitItem commitItem1 = CommitItemFactory.create(VerificationStatus.FAILED);
        List<Decision> decisions = new DecisionMaker().decide(Arrays.asList(commitItem1));
        Assert.assertEquals(1, decisions.size());
        assertDecision(commitItem1, decisions.get(0), OnFailureHandler.class);
        commitItem1 = CollectorApi.getCommitItemContainer().get(commitItem1.getKey());
        Assert.assertTrue(commitItem1.isDone());
    }
    
    @Test
    public void test1Failed1NotStarted() {
        
        CommitItem commitItem1 = CommitItemFactory.create(VerificationStatus.FAILED);
        CommitItem commitItem2 =
                CommitItemFactory.create(VerificationStatus.NOT_STARTED, commitItem1);
        List<Decision> decisions =
                new DecisionMaker().decide(Arrays.asList(commitItem1, commitItem2));
        Assert.assertEquals(2, decisions.size());
        assertDecision(commitItem1, decisions.get(0), OnFailureHandler.class);
        commitItem1 = CollectorApi.getCommitItemContainer().get(commitItem1.getKey());
        Assert.assertTrue(commitItem1.isDone());
        assertDecision(commitItem2, decisions.get(1), CheckinDecisionHandler.class);
    }
    
    @Test
    public void test1Failed1NotStarted1Running() {
        
        CommitItem commitItem1 = CommitItemFactory.create(VerificationStatus.FAILED);
        CommitItem commitItem2 = CommitItemFactory.create(VerificationStatus.RUNNING, commitItem1);
        CommitItem commitItem3 = CommitItemFactory.create(VerificationStatus.NOT_STARTED);
        List<Decision> decisions =
                new DecisionMaker().decide(Arrays.asList(
                        commitItem1,
                        commitItem2,
                        commitItem3));
        Assert.assertEquals(4, decisions.size());
        assertDecision(commitItem1, decisions.get(0), OnFailureHandler.class);
        commitItem1 = CollectorApi.getCommitItemContainer().get(commitItem1.getKey());
        Assert.assertTrue(commitItem1.isDone());
        assertDecision(commitItem2, decisions.get(1), OnFailedByParentHandler.class);
        assertDecision(commitItem2, decisions.get(2), CheckinDecisionHandler.class);
        commitItem2 = CollectorApi.getCommitItemContainer().get(commitItem2.getKey());
        Assert.assertEquals(VerificationStatus.NOT_STARTED, commitItem2.getStatus());
        Assert.assertEquals(null, commitItem2.getParent());
        Assert.assertFalse(commitItem2.isDone());
        assertDecision(commitItem3, decisions.get(3), CheckinDecisionHandler.class);
        commitItem3 = CollectorApi.getCommitItemContainer().get(commitItem3.getKey());
        Assert.assertFalse(commitItem3.isDone());
    }
    
    @Test
    public void test1Passed1NotStarted1Running() {
        
        CommitItem commitItem1 = CommitItemFactory.create(VerificationStatus.PASSED);
        CommitItem commitItem2 = CommitItemFactory.create(VerificationStatus.RUNNING, commitItem1);
        CommitItem commitItem3 = CommitItemFactory.create(VerificationStatus.NOT_STARTED);
        List<Decision> decisions =
                new DecisionMaker().decide(Arrays.asList(
                        commitItem1,
                        commitItem2,
                        commitItem3));
        Assert.assertEquals(2, decisions.size());
        assertDecision(commitItem1, decisions.get(0), OnSuccessHandler.class);
        Assert.assertTrue(commitItem1.isDone());
        assertDecision(commitItem3, decisions.get(1), CheckinDecisionHandler.class);
        commitItem2 = CollectorApi.getCommitItemContainer().get(commitItem2.getKey());
        commitItem3 = CollectorApi.getCommitItemContainer().get(commitItem3.getKey());
        Assert.assertEquals(commitItem2, commitItem3.getParent());
    }
    
    @Test
    public void test1Passed1NotStarted1Running1Failed() {
        
        CommitItem commit1 = CommitItemFactory.create(VerificationStatus.PASSED);
        CommitItem commit2 = CommitItemFactory.create(VerificationStatus.RUNNING);
        commit2.setParent(commit1);
        CommitItem commit3 = CommitItemFactory.create(VerificationStatus.FAILED);
        commit3.setParent(commit2);
        CommitItem commit4 = CommitItemFactory.create(VerificationStatus.NOT_STARTED);
        List<Decision> decisions =
                new DecisionMaker().decide(Arrays.asList(
                        commit1,
                        commit2,
                        commit3,
                        commit4));
        // commit3 & commit4 are pending - waiting for commit2
        Assert.assertEquals(1, decisions.size());
        assertDecision(commit1, decisions.get(0), OnSuccessHandler.class);
        Assert.assertTrue(commit1.isDone());
        Assert.assertFalse(commit3.isDone());
    }
    
    private void assertDecision(
            CommitItem commitItem,
            Decision decision,
            Class<? extends DecisionHandler> handler) {
        
        Assert.assertEquals(commitItem.getKey(), decision.getCommitItemId());
        Assert.assertEquals(handler, decision.getHandler().getClass());
    }
}
