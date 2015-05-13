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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.cache.container.CommitItemContainer;
import com.verigreen.collector.decision.CheckinDecisionHandler;
import com.verigreen.collector.decision.Decision;
import com.verigreen.collector.decision.DecisionHandler;
import com.verigreen.collector.decision.decisionmaker.DecisionMakerNotStartedItems;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.testcase.CollectorUnitTestCase;
import com.verigreen.common.util.CommitItemFactory;

public class TestDecisionMakerNotStartedItems extends CollectorUnitTestCase {
    
    @Test
    public void testExecute1NotStarted() {
        
        List<CommitItem> items = new ArrayList<>();
        items.add(CommitItemFactory.create(VerificationStatus.NOT_STARTED));
        List<Decision> decisions = new DecisionMakerNotStartedItems().execute(items);
        Assert.assertEquals(1, decisions.size());
        Assert.assertEquals(CheckinDecisionHandler.class, decisions.get(0).getHandler().getClass());
    }
    
    @Test
    public void testExecute1NotStarted1Running() {
        
        List<CommitItem> items = new ArrayList<>();
        CommitItem commit1 = CommitItemFactory.create(VerificationStatus.RUNNING);
        items.add(commit1);
        CommitItem commit2 = CommitItemFactory.create(VerificationStatus.NOT_STARTED);
        items.add(commit2);
        List<Decision> decisions = new DecisionMakerNotStartedItems().execute(items);
        Assert.assertEquals(1, decisions.size());
        Decision decision = decisions.get(0);
        Assert.assertEquals(CheckinDecisionHandler.class, decision.getHandler().getClass());
        Assert.assertEquals(commit2.getKey(), decision.getCommitItemId());
        CommitItemContainer commitItemContainer = CollectorApi.getCommitItemContainer();
        commit1 = commitItemContainer.get(commit1.getKey());
        commit2 = commitItemContainer.get(commit2.getKey());
        Assert.assertEquals(commit1, commit2.getParent());
    }
    
    @Test
    public void testExecute1NotStarted1Running1Failed() {
        
        List<CommitItem> items = new ArrayList<>();
        CommitItem commit1 = CommitItemFactory.create(VerificationStatus.RUNNING);
        items.add(commit1);
        CommitItem commit2 = CommitItemFactory.create(VerificationStatus.FAILED);
        commit2.setParent(commit1);
        items.add(commit2);
        CommitItem commit3 = CommitItemFactory.create(VerificationStatus.NOT_STARTED);
        items.add(commit3);
        List<Decision> decisions = new DecisionMakerNotStartedItems().execute(items);
        // commit3 is pending - waiting for commit1
        Assert.assertEquals(0, decisions.size());
    }
    
    @Test
    public void testExecute1NotStarted1Passed1Failed() {
        
        List<CommitItem> items = new ArrayList<>();
        CommitItem commit1 = CommitItemFactory.create(VerificationStatus.PASSED);
        items.add(commit1);
        CommitItem commit2 = CommitItemFactory.create(VerificationStatus.FAILED);
        commit2.setDone(true);
        commit2.setParent(commit1);
        items.add(commit2);
        CommitItem commit3 = CommitItemFactory.create(VerificationStatus.NOT_STARTED);
        items.add(commit3);
        List<Decision> decisions = new DecisionMakerNotStartedItems().execute(items);
        Assert.assertEquals(1, decisions.size());
        Decision decision = decisions.get(0);
        Assert.assertEquals(CheckinDecisionHandler.class, decision.getHandler().getClass());
        Assert.assertEquals(commit3.getKey(), decision.getCommitItemId());
        CommitItemContainer commitItemContainer = CollectorApi.getCommitItemContainer();
        commit1 = commitItemContainer.get(commit1.getKey());
        commit3 = commitItemContainer.get(commit3.getKey());
        Assert.assertEquals(commit1, commit3.getParent());
    }
    
    @Test
    public void testExecute1NotStarted1Failed() {
        
        List<CommitItem> items = new ArrayList<>();
        CommitItem commit1 = CommitItemFactory.create(VerificationStatus.FAILED);
        commit1.setDone(true);
        items.add(commit1);
        CommitItem commit2 = CommitItemFactory.create(VerificationStatus.NOT_STARTED);
        items.add(commit2);
        List<Decision> decisions = new DecisionMakerNotStartedItems().execute(items);
        Assert.assertEquals(1, decisions.size());
        Decision decision = decisions.get(0);
        Assert.assertEquals(CheckinDecisionHandler.class, decision.getHandler().getClass());
        Assert.assertEquals(commit2.getKey(), decision.getCommitItemId());
        Assert.assertNull(commit2.getParent());
    }
    
    @Test
    public void testExecute1NotStarted2Running1Passed() {
        
        CommitItem commit1 = CommitItemFactory.create(VerificationStatus.PASSED);
        commit1.setDone(true);
        CommitItem commit2 = CommitItemFactory.create(VerificationStatus.RUNNING);
        commit2.setParent(commit1);
        CommitItem commit3 = CommitItemFactory.create(VerificationStatus.RUNNING);
        commit3.setParent(commit2);
        CommitItem commit4 = CommitItemFactory.create(VerificationStatus.NOT_STARTED);
        List<Decision> decisions =
                new DecisionMakerNotStartedItems().execute(Arrays.asList(
                        commit1,
                        commit2,
                        commit3,
                        commit4));
        Assert.assertEquals(1, decisions.size());
        Decision decision = decisions.get(0);
        Assert.assertEquals(CheckinDecisionHandler.class, decision.getHandler().getClass());
        Assert.assertEquals(commit4.getKey(), decision.getCommitItemId());
        CommitItemContainer commitItemContainer = CollectorApi.getCommitItemContainer();
        commit3 = commitItemContainer.get(commit3.getKey());
        commit4 = commitItemContainer.get(commit4.getKey());
        Assert.assertEquals(commit3, commit4.getParent());
    }
    
    @Test
    public void test3NotStarted1Passed() {
        
        CommitItem commitItem1 = CommitItemFactory.create(VerificationStatus.PASSED);
        CommitItem commitItem2 = CommitItemFactory.create(VerificationStatus.NOT_STARTED);
        CommitItem commitItem3 = CommitItemFactory.create(VerificationStatus.NOT_STARTED);
        CommitItem commitItem4 = CommitItemFactory.create(VerificationStatus.NOT_STARTED);
        List<Decision> decisions =
                new DecisionMakerNotStartedItems().execute(Arrays.asList(
                        commitItem1,
                        commitItem2,
                        commitItem3,
                        commitItem4));
        Assert.assertEquals(3, decisions.size());
        assertDecision(
                commitItem2,
                decisions.get(0),
                CheckinDecisionHandler.class,
                CollectorApi.getCommitItemContainer().get(commitItem1.getKey()));
        assertDecision(commitItem3, decisions.get(1), CheckinDecisionHandler.class, commitItem2);
        assertDecision(commitItem4, decisions.get(2), CheckinDecisionHandler.class, commitItem3);
    }
    
    private void assertDecision(
            CommitItem commitItem,
            Decision decision,
            Class<? extends DecisionHandler> handler,
            CommitItem parent) {
        
        commitItem = CollectorApi.getCommitItemContainer().get(commitItem.getKey());
        Assert.assertEquals(commitItem.getKey(), decision.getCommitItemId());
        Assert.assertEquals(handler, decision.getHandler().getClass());
        Assert.assertEquals(parent, commitItem.getParent());
    }
}
