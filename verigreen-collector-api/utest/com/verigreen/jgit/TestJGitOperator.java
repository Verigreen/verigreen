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
package com.verigreen.jgit;

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;
import org.springframework.util.Assert;

import com.verigreen.jgit.JGitOperator;

public class TestJGitOperator {
    
    private final String _repoName = "d:\\alm_test\\.git";
    private final String _commitId = "bfaf8dd00f32d5aa17431661e0b6e605548091df";
    
    @Test
    public void testGetRevCommit() {
    	
        JGitOperator jgitOp = new JGitOperator(_repoName);
        RevCommit revCommit = jgitOp.getRevCommit(_commitId);
        System.out.println("committer =" + revCommit.getCommitterIdent());
        Assert.notNull(revCommit);
    }
    
    @Test
    public void testCreateBranch() {
    	
        JGitOperator jgitOp = new JGitOperator(_repoName);
        String branch = jgitOp.createBranch(_commitId, "kuku_" + System.currentTimeMillis());
        System.out.println("new branch created =" + branch);
        Assert.notNull(branch);
    }
}
