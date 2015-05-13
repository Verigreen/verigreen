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
package com.verigreen.rest;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import com.verigreen.common.spring.SpringTestCase;
import com.verigreen.collector.api.BranchDescriptor;
import com.verigreen.common.spring.GitHookApi;
import com.verigreen.rest.CreateBranchRequest;
import com.verigreen.spring.common.CollectorApi;

@ContextConfiguration(locations = { "/Spring/git-hook-rest-context.xml" })
public class TestSubmitBranchRequest extends SpringTestCase {
    
    @Test
    public void testCreateSubmitBranchRequest() {
        
        BranchDescriptor branchData = new BranchDescriptor();
        branchData.setCommitter("test@email.com");
        branchData.setProtectedBranch("protected-branch");
        branchData.setNewBranch("test-branch");
        CreateBranchRequest submitBranchRequest = GitHookApi.getCreateBranchRequest(branchData);
        Assert.assertNotNull(submitBranchRequest);
        Assert.assertEquals(submitBranchRequest.getEntity(), branchData);
        Assert.assertEquals(submitBranchRequest.getUri(), CollectorApi.getCollectorAddress()
                                                          + "/branches");
    }
}
