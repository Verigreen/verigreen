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
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.cache.container.CommitItemContainer;
import com.verigreen.collector.decision.Decision;
import com.verigreen.collector.decision.DecisionHandler;
import com.verigreen.collector.decision.OnFailedByParentHandler;
import com.verigreen.collector.decision.OnFailureHandler;
import com.verigreen.collector.decision.decisionmaker.DecisionMakerFailedItems;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.testcase.CollectorUnitTestCase;
import com.verigreen.common.util.CommitItemFactory;

public class TestDecisionMakerFailedItems extends CollectorUnitTestCase {
    
    @Test
    public void testExecute1Failed() {
        
        Collection<CommitItem> items = new ArrayList<>();
        CommitItem commit = CommitItemFactory.create(VerificationStatus.FAILED);
        items.add(commit);
        List<Decision> decisions = new DecisionMakerFailedItems().execute(items);
        Assert.assertEquals(1, decisions.size());
        Assert.assertEquals(OnFailureHandler.class, decisions.get(0).getHandler().getClass());
        CommitItemContainer commitItemContainer = CollectorApi.getCommitItemContainer();
        commit = commitItemContainer.get(commit.getKey());
        Assert.assertTrue(commit.isDone());
    }
    
    @Test
    public void testExecute1Failed1Running() {
        
        Collection<CommitItem> items = new ArrayList<>();
        CommitItem commit1 = CommitItemFactory.create(VerificationStatus.RUNNING);
        items.add(commit1);
        CommitItem commit2 = CommitItemFactory.create(VerificationStatus.FAILED);
        commit2.setParent(commit1);
        items.add(commit2);
        List<Decision> decisions = new DecisionMakerFailedItems().execute(items);
        Assert.assertEquals(0, decisions.size());
        Assert.assertFalse(commit2.isDone());
    }
    
    @Test
    public void testExecute1Failed2Passed() {
        
        Collection<CommitItem> items = new ArrayList<>();
        CommitItem commit1 = CommitItemFactory.create(VerificationStatus.PASSED);
        items.add(commit1);
        CommitItem commit2 = CommitItemFactory.create(VerificationStatus.PASSED);
        commit2.setParent(commit1);
        items.add(commit2);
        CommitItem commit3 = CommitItemFactory.create(VerificationStatus.FAILED);
        commit3.setParent(commit2);
        items.add(commit3);
        List<Decision> decisions = new DecisionMakerFailedItems().execute(items);
        Assert.assertEquals(1, decisions.size());
        Assert.assertEquals(OnFailureHandler.class, decisions.get(0).getHandler().getClass());
        CommitItemContainer commitItemContainer = CollectorApi.getCommitItemContainer();
        commit3 = commitItemContainer.get(commit3.getKey());
        Assert.assertTrue(commit3.isDone());
    }
    
    @Test
    public void testExecute2Failed1Passed() {
        
        Collection<CommitItem> items = new ArrayList<>();
        CommitItem commit1 = CommitItemFactory.create(VerificationStatus.PASSED);
        items.add(commit1);
        CommitItem commit2 = CommitItemFactory.create(VerificationStatus.FAILED);
        commit2.setParent(commit1);
        items.add(commit2);
        CommitItem commit3 = CommitItemFactory.create(VerificationStatus.FAILED);
        commit3.setParent(commit2);
        items.add(commit3);
        List<Decision> decisions = new DecisionMakerFailedItems().execute(items);
        Assert.assertEquals(1, decisions.size());
        Decision decision = decisions.get(0);
        CommitItemContainer commitItemContainer = CollectorApi.getCommitItemContainer();
        commit1 = commitItemContainer.get(commit1.getKey());
        commit2 = commitItemContainer.get(commit2.getKey());
        commit3 = commitItemContainer.get(commit3.getKey());
        assertDecision(commit1, commit2, decision, OnFailureHandler.class);
        Assert.assertTrue(commit2.isDone());
        assertNotStarted(commit3);
    }
    
    @Test
    public void testExecute3Failed1Passed() {
        
        Collection<CommitItem> items = new ArrayList<>();
        CommitItem commit1 = CommitItemFactory.create(VerificationStatus.PASSED);
        items.add(commit1);
        CommitItem commit2 = CommitItemFactory.create(VerificationStatus.FAILED);
        commit2.setParent(commit1);
        items.add(commit2);
        CommitItem commit3 = CommitItemFactory.create(VerificationStatus.FAILED);
        commit3.setParent(commit2);
        items.add(commit3);
        CommitItem commit4 = CommitItemFactory.create(VerificationStatus.FAILED);
        commit4.setParent(commit3);
        items.add(commit4);
        
        List<Decision> decisions = new DecisionMakerFailedItems().execute(items);
        Assert.assertEquals(1, decisions.size());
        Decision decision = decisions.get(0);
        CommitItemContainer commitItemContainer = CollectorApi.getCommitItemContainer();
        commit1 = commitItemContainer.get(commit1.getKey());
        commit2 = commitItemContainer.get(commit2.getKey());
        commit3 = commitItemContainer.get(commit3.getKey());
        commit4 = commitItemContainer.get(commit4.getKey());
        assertDecision(commit1, commit2, decision, OnFailureHandler.class);
        assertNotStarted(commit3);
        assertNotStarted(commit4);
    }
    
    public void testExecute2FailedChildPassed() {
        
        Collection<CommitItem> items = new ArrayList<>();
        CommitItem commit1 = CommitItemFactory.create(VerificationStatus.FAILED);
        items.add(commit1);
        CommitItem commit2 = CommitItemFactory.create(VerificationStatus.FAILED);
        commit2.setParent(commit1);
        items.add(commit2);
        CommitItem commit3 = CommitItemFactory.create(VerificationStatus.PASSED);
        commit3.setParent(commit2);
        items.add(commit3);
        
        List<Decision> decisions = new DecisionMakerFailedItems().execute(items);
        Assert.assertEquals(2, decisions.size());
        
        assertDecision(null, commit1, decisions.get(0), OnFailureHandler.class);
        assertDecision(commit1, commit2, decisions.get(1), OnFailureHandler.class);
        commit3 = CollectorApi.getCommitItemContainer().get(commit3.getKey());
        Assert.assertEquals(VerificationStatus.PASSED, commit3.getStatus());
        Assert.assertEquals(commit2, commit3.getParent());
    }
    
    @Test
    public void test1Failed2Running() {
        
        CommitItem commitItem1 = CommitItemFactory.create(VerificationStatus.FAILED);
        CommitItem commitItem2 = CommitItemFactory.create(VerificationStatus.RUNNING, commitItem1);
        CommitItem commitItem3 = CommitItemFactory.create(VerificationStatus.RUNNING, commitItem2);
        List<Decision> decisions =
                new DecisionMakerFailedItems().execute(Arrays.asList(
                        commitItem1,
                        commitItem2,
                        commitItem3));
        Assert.assertEquals(3, decisions.size());
        CommitItemContainer commitItemContainer = CollectorApi.getCommitItemContainer();
        commitItem1 = commitItemContainer.get(commitItem1.getKey());
        commitItem2 = commitItemContainer.get(commitItem2.getKey());
        commitItem3 = commitItemContainer.get(commitItem3.getKey());
        assertDecision(null, commitItem1, decisions.get(0), OnFailureHandler.class);
        assertDecision(null, commitItem2, decisions.get(1), OnFailedByParentHandler.class);
        assertDecision(null, commitItem3, decisions.get(2), OnFailedByParentHandler.class);
        commitItem2 = CollectorApi.getCommitItemContainer().get(commitItem2.getKey());
        Assert.assertEquals(VerificationStatus.NOT_STARTED, commitItem2.getStatus());
        Assert.assertEquals(null, commitItem2.getParent());
        commitItem3 = CollectorApi.getCommitItemContainer().get(commitItem2.getKey());
        Assert.assertEquals(VerificationStatus.NOT_STARTED, commitItem3.getStatus());
        Assert.assertEquals(null, commitItem3.getParent());
    }
    
    private void assertNotStarted(CommitItem commit) {
        
        Assert.assertEquals(VerificationStatus.NOT_STARTED, commit.getStatus());
        Assert.assertNull(commit.getParent());
        Assert.assertNull(commit.getChild());
    }
    
    private void assertDecision(
            CommitItem parent,
            CommitItem expectedItem,
            Decision decision,
            Class<? extends DecisionHandler> handler) {
        
        Assert.assertEquals(handler, decision.getHandler().getClass());
        Assert.assertEquals(expectedItem.getKey(), decision.getCommitItemId());
        Assert.assertEquals(parent, expectedItem.getParent());
    }
}
