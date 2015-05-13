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
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.decision.Decision;
import com.verigreen.collector.decision.DecisionHandler;
import com.verigreen.collector.decision.OnSuccessByChildHandler;
import com.verigreen.collector.decision.OnSuccessHandler;
import com.verigreen.collector.decision.decisionmaker.DecisionMakerPassedItems;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.testcase.CollectorUnitTestCase;
import com.verigreen.common.util.CommitItemFactory;

public class TestDecisionMakerPassedItems extends CollectorUnitTestCase {
    
    @Test
    public void testExecute1Pass() {
        
        Collection<CommitItem> items = new ArrayList<>();
        items.add(CommitItemFactory.create(VerificationStatus.PASSED));
        List<Decision> decisions = new DecisionMakerPassedItems().execute(items);
        Assert.assertEquals(1, decisions.size());
        Assert.assertEquals(OnSuccessHandler.class, decisions.get(0).getHandler().getClass());
    }
    
    @Test
    public void testExecuteParent() {
        
        Collection<CommitItem> items = new ArrayList<>();
        CommitItem item1 = CommitItemFactory.create(VerificationStatus.PASSED);
        items.add(item1);
        CommitItem item2 = CommitItemFactory.create(VerificationStatus.RUNNING, item1);
        items.add(item2);
        CommitItem item3 = CommitItemFactory.create(VerificationStatus.MERGE_FAILED, item2);
        items.add(item3);
        CommitItem item4 = CommitItemFactory.create(VerificationStatus.GIT_FAILURE, item3);
        item4.setDone(true);
        items.add(item4);
        CommitItem item5 = CommitItemFactory.create(VerificationStatus.FAILED, item4);
        items.add(item5);
        CommitItem item6 = CommitItemFactory.create(VerificationStatus.PASSED, item5);
        items.add(item6);
        List<Decision> decisions = new DecisionMakerPassedItems().execute(items);
        Assert.assertEquals(4, decisions.size());
        assertDecision(item1, decisions.get(0), OnSuccessHandler.class);
        assertDecision(item5, decisions.get(1), OnSuccessByChildHandler.class);
        assertDecision(item2, decisions.get(2), OnSuccessByChildHandler.class);
        assertDecision(item6, decisions.get(3), OnSuccessHandler.class);
    }
    
    private void assertDecision(
            CommitItem item,
            Decision decision,
            Class<? extends DecisionHandler> expectedHandler) {
        
        item = CollectorApi.getCommitItemContainer().get(item.getKey());
        Assert.assertEquals(item.getKey(), decision.getCommitItemId());
        Assert.assertEquals(expectedHandler, decision.getHandler().getClass());
        Assert.assertTrue("Item should be done", item.isDone());
    }
}
