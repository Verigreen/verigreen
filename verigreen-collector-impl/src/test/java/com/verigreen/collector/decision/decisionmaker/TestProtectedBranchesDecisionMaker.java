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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.common.TestCollectorConstants;
import com.verigreen.collector.decision.Decision;
import com.verigreen.collector.decision.decisionmaker.ProtectedBranchesDecisionMaker;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.common.testcase.CollectorUnitTestCase;
import com.verigreen.common.util.CommitItemFactory;

public class TestProtectedBranchesDecisionMaker extends CollectorUnitTestCase {
    
    @Test
    public void testNotStartedItems() {
        
        CommitItem commitItem1 =
                CommitItemFactory.create(
                        VerificationStatus.NOT_STARTED,
                        TestCollectorConstants.PROTECTED_BRANCH_MASTER);
        CommitItem commitItem2 =
                CommitItemFactory.create(
                        VerificationStatus.NOT_STARTED,
                        TestCollectorConstants.PROTECTED_BRANCH_1);
        CommitItem commitItem3 =
                CommitItemFactory.create(
                        VerificationStatus.NOT_STARTED,
                        TestCollectorConstants.PROTECTED_BRANCH_2);
        CommitItem commitItem4 =
                CommitItemFactory.create(
                        VerificationStatus.NOT_STARTED,
                        TestCollectorConstants.PROTECTED_BRANCH_MASTER);
        Map<String, CommitItem> commits = new HashMap<>();
        commits.put(commitItem1.getKey(), commitItem1);
        commits.put(commitItem2.getKey(), commitItem2);
        commits.put(commitItem3.getKey(), commitItem3);
        commits.put(commitItem4.getKey(), commitItem4);
        
        Collection<List<Decision>> decisions = new ProtectedBranchesDecisionMaker().decide();
        
        Assert.assertEquals(3, decisions.size());
        //List<Decision> branch = decisions.iterator().next();
        
    }
}
