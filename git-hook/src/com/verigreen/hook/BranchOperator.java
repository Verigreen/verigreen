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
package com.verigreen.hook;

import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.revwalk.RevCommit;

import com.verigreen.collector.api.BranchDescriptor;
import com.verigreen.collector.api.VerigreenNeeded;
import com.verigreen.collector.api.VerigreenUtils;
import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.common.spring.GitHookApi;
import com.verigreen.jgit.JGitOperator;
import com.verigreen.spring.common.CollectorApi;
import com.verigreen.common.concurrency.RuntimeUtils;
import com.verigreen.common.utils.StringUtils;
import com.verigreen.restclient.RestClientImpl;
import com.verigreen.restclient.RestClientResponse;
import com.verigreen.restclient.common.RestClientException;

public class BranchOperator {
    
    public int processBranch(String repository, String oldrev, String newrev, String ref) {
        
        int ret = 1;
        String zero = "0000000000000000000000000000000000000000";
        if (newrev.equals(zero) || oldrev.equals(zero)) {
            // branch updates 
            ret = 0;
            VerigreenLogger.get().log(
                    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format(
                            "Verigreen is not needed for branch updates [newrev=%s, oldrev=%s]",
                            newrev,
                            oldrev));
        } else {
            // normal push
            VerigreenLogger.get().log(
                    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format("Normal push - checking if verigreen is needed (%s)", newrev));
            JGitOperator jgitOp = new JGitOperator(".");
            RevCommit commitMetadata = jgitOp.getRevCommit(newrev);
            String shortId = getShortCommitId(commitMetadata,newrev);
            VerigreenNeeded verigreenNeeded =
                    checkIfVerigreenNeeded(
                            ref,
                            shortId,
                            commitMetadata.getCommitterIdent().getEmailAddress());
            if (verigreenNeeded.getVerigreenNeeded()) {
                // new commit - create a new branch for newrev and call collector
                VerigreenLogger.get().log(
                        getClass().getName(),
                        RuntimeUtils.getCurrentMethodName(),
                        "Commit is submitted to collector");
                submitBranch(newrev, ref, jgitOp, commitMetadata, shortId);  
                logToConsole();
            } else {
                if (!verigreenNeeded.getShouldRejectCommit()) {
                    /*in case the commit already PASSED verigreen or the branch isn't protected*/
                    ret = 0;
                }
                VerigreenLogger.get().log(
                        getClass().getName(),
                        RuntimeUtils.getCurrentMethodName(),
                        String.format(
                                "Verigreen not needed because : %s",
                                verigreenNeeded.getReason()));
            }
        }
        
        return ret;
    }
    
    private void logToConsole() {
        
        System.out.println("======================================="
                           + StringUtils.NEW_LINE
                           + StringUtils.NEW_LINE);
        System.out.println("Submitted for verification by Verigreen" + StringUtils.NEW_LINE + StringUtils.NEW_LINE);
        System.out.println("======================================="
                           + StringUtils.NEW_LINE
                           + StringUtils.NEW_LINE);
    }
    
    private VerigreenNeeded checkIfVerigreenNeeded(
            String branchName,
            String newrev,
            String committer) {
        
        VerigreenNeeded ret = null;
        String vgBranchName = VerigreenUtils.getVerigreenBranchName(newrev);
        try {
            RestClientResponse clientResponse =
                    new RestClientImpl().get(CollectorApi.getVerigreenNeededRequest(
                            branchName,
                            vgBranchName,
                            newrev,
                            committer));
            ret = clientResponse.getEntity(VerigreenNeeded.class);
        } catch (RestClientException e) {
            throw new RuntimeException(e);
        }
        
        return ret;
    }
    
    private void submitBranch(
            String newrev,
            String ref,
            JGitOperator jgitOp,
            RevCommit commitMetadata,
            String shortId) {
        String vgBranchName = VerigreenUtils.getVerigreenBranchName(shortId);
        
        try
        {
       		jgitOp.createBranch(newrev, vgBranchName); 
        }
        catch(RuntimeException e){
       		if (!(e.getCause() instanceof RefAlreadyExistsException))
       		{
       			throw e;
       		}
        }
        BranchDescriptor branchData = new BranchDescriptor();
        branchData.setCommitter(commitMetadata.getCommitterIdent().getEmailAddress());
        branchData.setProtectedBranch(ref);
        branchData.setNewBranch(vgBranchName);
        branchData.setCommitId(shortId);
        
        new RestClientImpl().post(GitHookApi.getCreateBranchRequest(branchData));
    }
    
    private String getShortCommitId(RevCommit commitMetadata, String newrev)
    {
    	String removeName = commitMetadata.getCommitterIdent().getName().replace(", ", "_");
    	removeName = removeName.replace(" ", "_");
    	String shortCommitId = newrev.substring(0, 7)+"_"+removeName;
    	
    	return shortCommitId;
    }
    
    
}
