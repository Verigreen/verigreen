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
package com.verigreen.collector.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import junit.framework.Assert;

import org.junit.Test;

import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.common.CommitItemUtils;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.common.testcase.CollectorSpringTestCase;
import com.verigreen.common.util.CommitItemFactory;
import com.verigreen.common.utils.CollectionUtils;

public class TestCommitItemUtils extends CollectorSpringTestCase {
    
    @Test
    public void testFilterItemsNull() {
        
        Collection<CommitItem> items = CommitItemUtils.filterItems(null, VerificationStatus.PASSED);
        Assert.assertTrue(CollectionUtils.isNullOrEmpty(items));
    }
    
    @Test
    public void testFilterItemsEmpty() {
        
        Collection<CommitItem> items =
                CommitItemUtils.filterItems(
                        Collections.<CommitItem> emptyList(),
                        VerificationStatus.PASSED);
        Assert.assertTrue(CollectionUtils.isNullOrEmpty(items));
    }
    
    @Test
    public void testFilterItemsAllPassed() {
        
        Collection<CommitItem> items =
                CommitItemUtils.filterItems(
                        Arrays.asList(
                                CommitItemFactory.create(VerificationStatus.PASSED),
                                CommitItemFactory.create(VerificationStatus.PASSED)),
                        VerificationStatus.PASSED);
        verify(items, 2);
    }
    
    @Test
    public void testFilterItemsPassedFailed() {
        
        Collection<CommitItem> items =
                CommitItemUtils.filterItems(
                        Arrays.asList(
                                CommitItemFactory.create(VerificationStatus.PASSED),
                                CommitItemFactory.create(VerificationStatus.FAILED),
                                CommitItemFactory.create(VerificationStatus.GIT_FAILURE),
                                CommitItemFactory.create(VerificationStatus.PASSED)),
                        VerificationStatus.PASSED);
        verify(items, 2);
    }
    
    @Test
    public void testFilterItemsPassedAndEmpty() {
        
        Collection<CommitItem> items =
                CommitItemUtils.filterItems(Arrays.asList(
                        CommitItemFactory.create(VerificationStatus.PASSED),
                        CommitItem.EMPTY,
                        null), VerificationStatus.PASSED);
        verify(items, 1);
    }
    
    private void verify(Collection<CommitItem> items, int count) {
        
        Assert.assertEquals(count, items.size());
        for (CommitItem currItem : items) {
            Assert.assertEquals(VerificationStatus.PASSED, currItem.getStatus());
        }
    }
}
