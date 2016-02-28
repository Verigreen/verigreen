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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.DeleteBranchCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.RebaseCommand.Operation;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevWalkUtils;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;

import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.spring.common.CollectorApi;
import com.verigreen.common.concurrency.RuntimeUtils;
import com.verigreen.common.utils.Pair;
import com.verigreen.restclient.RestClientImpl;

public class JGitOperator implements SourceControlOperator {
    
    private Repository _repo;
    private Git _git;
    private final static String REFS_HEADS 				= "refs/heads/";
    private final static String REFS_REMOTES 			= "refs/remotes/origin/";
    private String commited_By_Collector;
    private String email_Address;

	public JGitOperator(String repositoryPath) {
		
		try {
            // need to verify repo was created successfully - this is not enough
			/*if(!repositoryPath.contains("\\.git") && !repositoryPath.equals("."))
			{
				repositoryPath=repositoryPath.concat("\\.git");
			}*/
            _repo = new FileRepository(repositoryPath);
            _git = new Git(_repo);
 
            } catch (IOException e) {
            throw new RuntimeException(String.format(
                    "Failed creating git repository for path [%s]",
                    repositoryPath), e);
            }
        }


	public void setCommited_By_Collector(String commited_By_Collector) {
		this.commited_By_Collector = commited_By_Collector;
	}
	
	public void setEmail_Address(String email_Address) {
		this.email_Address = email_Address;
	}

	@Override
    public String createBranch(String commitId, String branchName) {
        
        Ref result = null;
        CreateBranchCommand branchCreate = _git.branchCreate();
        branchCreate.setName(branchName);
        branchCreate.setStartPoint(commitId);
        try {
            result = branchCreate.call();
        } catch (Throwable e) {
            throw new RuntimeException(String.format(
                    "Failed creating branch: %s for commit [%s]",
                    branchName,
                    commitId), e);
        }
        
        return result.getName();
    }
    
    public RevCommit getRevCommit(String commitId) {
        
        RevCommit ret = null;
        RevWalk walk = new RevWalk(_repo);
        try {
            ret = walk.parseCommit(_repo.resolve(commitId));
        } catch (Throwable e) {
            throw new RuntimeException(
                    String.format("Failed retrieving commit data [%s]", commitId),
                    e);
        }
        
        return ret;
    }
    
    public Ref getRef(String ref) {
        
        Ref ans = null;
        try {
            ans = _repo.getRef(ref);
        } catch (Throwable e) {
            throw new RuntimeException(String.format("Failed retrieving ref of [%s]", ref), e);
        }
        
        return ans;
    }
    
    @Override
    public String fetch(String localBranchName, String remoteBranchName) {
        
        RefSpec spec = new RefSpec().setSourceDestination(localBranchName, remoteBranchName);
        FetchCommand command = _git.fetch();
        command.setRefSpecs(spec);
        FetchResult result = null;
        try {
            result = command.call();
        } catch (Throwable e) {
            throw new RuntimeException(String.format(
                    "Failed to fetch from [%s] to [%s]",
                    remoteBranchName,
                    localBranchName), e);
        }
        
        return result.getMessages();
    }
    
    /**
     * Update all branches.
     */
    @Override
    public String fetch() {
        
        return fetch(REFS_HEADS + "*", REFS_REMOTES + "*");
    }
    
    @Override
    public String checkout(
            String branchName,
            boolean useBranchNameAsStartPoint,
            boolean createBranch,
            boolean useForce) {
        
        CheckoutCommand command = _git.checkout();
        command.setCreateBranch(createBranch);
        command.setForce(useForce);
        command.setName(branchName);
        if (useBranchNameAsStartPoint) {
            command.setStartPoint(REFS_REMOTES + branchName);
        }
        Ref ref = null;
        try {
            ref = command.call();
        } catch (Throwable e) {
            throw new RuntimeException(
                    String.format("Failed to checkout branch [%s]", branchName),
                    e);
        }
        
        return ref.getName();
    }
    
    @Override
    public String checkout(String branchName, boolean createBranchIfNotExists, boolean useForce) {
        
        return checkout(branchName, false, createBranchIfNotExists, useForce);
    }
    
    @Override
    public boolean rebase(String upStreamBranchName) {
        
        RebaseCommand command = _git.rebase();
        RebaseResult result = null;
        try {
            command.setUpstream(upStreamBranchName);
            result = command.call();
            // if there are merge conflicts (rebase interactive) - reset the repository
            if (!result.getStatus().isSuccessful()) {
                _git.rebase().setOperation(Operation.ABORT).call();
            }
        } catch (Throwable e) {
            throw new RuntimeException(String.format(
                    "Failed to rebase with upstream [%s]",
                    upStreamBranchName), e);
        }
        
        return result.getStatus().isSuccessful();
    }
    
    @Override
 public boolean push(String sourceBranch, String destinationBranch) {
        
        PushCommand command = _git.push();
        boolean ret = true;
        RefSpec refSpec = new RefSpec().setSourceDestination(sourceBranch, destinationBranch);
        command.setRefSpecs(refSpec);
        try {
        	List<Ref> remoteBranches = _git.branchList().setListMode(ListMode.REMOTE).call();
            Iterable<PushResult> results = command.call();
            for (PushResult pushResult : results) {
            	Collection<RemoteRefUpdate> resultsCollection = pushResult.getRemoteUpdates();
            	Map<PushResult,RemoteRefUpdate> resultsMap = new HashMap<>();
            	for(RemoteRefUpdate remoteRefUpdate : resultsCollection)
            	{
            		resultsMap.put(pushResult, remoteRefUpdate);
            	}
            	
                RemoteRefUpdate remoteUpdate = pushResult.getRemoteUpdate(destinationBranch);
                if (remoteUpdate != null) {
                    org.eclipse.jgit.transport.RemoteRefUpdate.Status status =
                            remoteUpdate.getStatus();
                    ret =
                            status.equals(org.eclipse.jgit.transport.RemoteRefUpdate.Status.OK)
                                    || status.equals(org.eclipse.jgit.transport.RemoteRefUpdate.Status.UP_TO_DATE);
                }
                
                if(remoteUpdate == null && !remoteBranches.toString().contains(destinationBranch))
                {	
             
                	
                	for(RemoteRefUpdate resultValue : resultsMap.values())
                	{
                		if(resultValue.toString().contains("REJECTED_OTHER_REASON"))
                		{
                			ret = false;
                		}
                	}
                }	
            }
        } catch (Throwable e) {
            throw new RuntimeException(String.format(
                    "Failed to push [%s] into [%s]",
                    sourceBranch,
                    destinationBranch), e);
        }
        
        return ret;
    }
    
    @Override
    public void deleteBranch(String... branchesToDelete) {
        deleteBranch(false,branchesToDelete);
    }
    
    public void deleteBranch(boolean force, String... branchesToDelete) {
    	 DeleteBranchCommand branchDelete = _git.branchDelete();
    	 branchDelete.setForce(force);
         try {
             List<String> call = branchDelete.setBranchNames(branchesToDelete).call();
             call.toString();
             for (String currBranch : branchesToDelete) {
                 push(null, REFS_HEADS + currBranch);
             }
         } catch (Throwable e) {
             throw new RuntimeException(String.format(
                     "Failed to delete branches [%s]",
                     Arrays.toString(branchesToDelete)), e);
         }
    }
    
    @Override
    public Pair<Boolean, String> updateBranchWithParent(
            String branchToUpdate,
            String parentBranch,
            boolean useBranchNameAsStartPoint,
            boolean createBranchIfNotExists,
            boolean useForce) {
        
        /*switch to the parent branch and update it*/
        checkout(parentBranch, false, false);
        fetch();
        boolean isBehind = false;
        //in case permitted user push while job is in running.
       	String branch = parentBranch.split("refs/heads/")[1];
       	try{
       		isBehind = isRefBehind(_repo.getRef(parentBranch), _repo.getRef(REFS_REMOTES + branch));
       		if (!isBehind){
       			/*switch to the branch that needs update and merge with the parent branch*/
       	        checkout(branchToUpdate, useBranchNameAsStartPoint, createBranchIfNotExists, useForce);
       	        return merge(branchToUpdate, parentBranch);
       		}
       		
       	}catch (IOException e) {
       		VerigreenLogger.get().log(
       				getClass().getName(),
       				RuntimeUtils.getCurrentMethodName(),
       				"Failed to merge origin with head");
       	}
		merge(parentBranch, branch);
		
        /*merge with the parent branch*/
        return merge(parentBranch, branchToUpdate, isBehind);
    }
    
    @Override
    public boolean isBranchExist(String branch) {
        
        return getRef(branch) != null;
    }
    
    @Override
    public Pair<Boolean, String> merge(String branchToUpdate, String branchHead) {
        
    	return merge(branchToUpdate, branchHead, false);
        
    }
    
    
    public Pair<Boolean, String> merge(String branchToUpdate, String branchHead, boolean commit) {
        
        Pair<Boolean, String> ret = new Pair<>(false, "");
        MergeCommand command = _git.merge();
        try {
            String refName =
                    !branchHead.contains(REFS_HEADS) ? REFS_REMOTES + branchHead : branchHead;
            command.include(_repo.getRef(refName));
            command.setCommit(commit);
            MergeResult mergeResult = command.call();
            ret = checkResult(branchToUpdate, branchHead, ret, mergeResult);
        } catch (Throwable e) {
            VerigreenLogger.get().log(
                    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format(
                            "Failed to update branch [%s] with parent branch [%s]",
                            branchToUpdate,
                            branchHead));
        }
        
        return ret;
    }
    
    @Override
    public String getPathOfLocalRepository() {
        
        return _repo.getDirectory().getPath();
    }
    
    @Override
    public void add(String itemToAdd) {
        
        AddCommand command = _git.add();
        command.addFilepattern(itemToAdd);
        try {
            command.call();
        } catch (Throwable e) {
            throw new RuntimeException(String.format("Failed to add  [%s]", itemToAdd), e);
        }
        
    }
    
    @Override
    public String commit(String author, String email, String message) {
        
        RevCommit revCommit = null;
        CommitCommand command = _git.commit();
        command.setCommitter(author, email);
        command.setMessage(message);
        command.setAll(true);
        try {
            revCommit = command.call();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to commit", e);
        }
        
        return revCommit.getId().getName();
    }
    
    @Override
    public boolean isThereAnyDifs() {
        
        boolean ans = false;
        try {
            List<DiffEntry> list = _git.diff().call();
            if (list.size() > 0) {
                ans = true;
            }
        } catch (GitAPIException e) {
            throw new RuntimeException("Failed to get diffs", e);
        }
        
        return ans;
    }
    
    public void reset(String refToResetTo) {
        
        ResetCommand command = _git.reset();
        command.setRef(refToResetTo);
        command.setMode(ResetType.HARD);
        try {
            command.call();
        } catch (Throwable e) {
            throw new RuntimeException(String.format("Failed to reset to [%s]", refToResetTo), e);
        }
    }
    
    @Override
    public boolean isBranchContainsCommit(String branchName, String commitId) {
        
        boolean ans = false;
        RevWalk walk = new RevWalk(_repo);
        RevCommit commit;
        Ref ref;
        try {
            commit = walk.parseCommit(_repo.resolve(commitId + "^0"));
            ref = _repo.getRef(branchName);
            if (walk.isMergedInto(commit, walk.parseCommit(ref.getObjectId()))) {
                ans = true;
            }
            walk.dispose();
        } catch (Throwable e) {
            throw new RuntimeException(String.format(
                    "Failed to check if commit [%s] is part of branch[%s]",
                    commitId,
                    branchName), e);
        }
        
        return ans;
    }
    
    private Pair<Boolean, String> checkResult(
            String branchToUpdate,
            String branchHead,
            Pair<Boolean, String> ret,
            MergeResult mergeResult) throws IOException {
        
        if (mergeResult.getMergeStatus().equals(MergeStatus.CONFLICTING)
            || mergeResult.getMergeStatus().equals(MergeStatus.FAILED)) {
            VerigreenLogger.get().log(
                    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format(
                            "Merge conflicts for parent_branch:%s into:%s. rejecting commit",
                            branchHead,
                            branchToUpdate));
            reset(_repo.getRef(branchToUpdate).getName());
        } else if (mergeResult.getMergeStatus().equals(MergeStatus.ALREADY_UP_TO_DATE)
                   || mergeResult.getMergeStatus().equals(MergeStatus.FAST_FORWARD)) {
            VerigreenLogger.get().log(
                    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format(
                            "Merge not needed for parent_branch:%s into:%s",
                            branchHead,
                            branchToUpdate));
            ret = new Pair<>(true, "");
        } else if (mergeResult.getMergeStatus().equals(MergeStatus.MERGED_NOT_COMMITTED)) {
            String autoMergeMessage = createMessageAutoCommit(mergeResult);
			String commitId = commit(commited_By_Collector, email_Address, autoMergeMessage );
			String adjustCommitId = commitId.substring(0,7) + "_" + commited_By_Collector;
            VerigreenLogger.get().log(
                    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format(
                            "Verigreen merge for parent_branch:%s into:%s was not committed. Performing auto commit [%s]",
                            branchHead,
                            branchToUpdate,
                            adjustCommitId));
            ret = new Pair<>(true, adjustCommitId);
        }else if (mergeResult.getMergeStatus().equals(MergeStatus.MERGED)) {
         VerigreenLogger.get().log(
                 getClass().getName(),
                 RuntimeUtils.getCurrentMethodName(),
                 "Merge was made after diverted branch with auto commit");
         ret = new Pair<>(true, "");
         new RestClientImpl().post(CollectorApi.getPostVerigreenNeededRequest(mergeResult.getNewHead().getName().substring(0, 7)));
     }
        return ret;
    }
    
    private String createMessageAutoCommit(MergeResult mergeResult) {
		StringBuffer message = new StringBuffer("Auto merge commit between:");
		
		 for (Object commit : mergeResult.getMergedCommits()) {
			 message.append(" ").append(((RevCommit)commit).getName().substring(0, 7));//no check for null
		}
		
		return message.toString();
	}

	@Override
    public String getLocalBranchesRoot() {
        return REFS_HEADS;
    }
    
    @Override
    public String getRemoteBranchesRoot() {
        return REFS_REMOTES;
    }

	@Override
	public List<String> retreiveBranches() {
		List <String> receivedListAsString = new ArrayList<>();
		List <Ref> receivedList;
		try {
			receivedList = _git.branchList().setListMode(ListMode.ALL).call();
			for (Ref ref : receivedList) {
				receivedListAsString.add(ref.getName());
			}
		} catch (GitAPIException e) {
			VerigreenLogger.get().log(
                    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(), "Failed to receive branches list");
			return null;
		}
		return receivedListAsString;
	}
	
	/**
	 * Returning branches list that start with "vg_"
	 * 
	 * @param list
	 * @return
	 */
	public List <String> getBranchesList(List<String> list){
		if (list == null)
			return null;
		List <String> relevantList = new ArrayList<>();
		for (String branch : list) {
			if (branch.startsWith(REFS_HEADS + "vg_") || branch.startsWith(REFS_REMOTES + "vg_")){
				relevantList.add(branch.split("/")[branch.split("/").length-1]);
			}
		}
		return relevantList;
		
	}
	
	boolean isRefBehind( Ref behind, Ref tracking ) throws IOException {
	    RevWalk walk = new RevWalk( _git.getRepository() );
	    try {
	      RevCommit behindCommit = walk.parseCommit( behind.getObjectId() );
	      RevCommit trackingCommit = walk.parseCommit( tracking.getObjectId() );
	      walk.setRevFilter( RevFilter.MERGE_BASE );
	      walk.markStart( behindCommit );
	      walk.markStart( trackingCommit );
	      RevCommit mergeBase = walk.next();
	      walk.reset();
	      walk.setRevFilter( RevFilter.ALL );
	      int aheadCount = RevWalkUtils.count( walk, behindCommit, mergeBase );
	      int behindCount = RevWalkUtils.count( walk, trackingCommit, mergeBase );
	      
	      return behindCount > aheadCount ? true:false;
	    } catch (Throwable e) {
            throw new RuntimeException(String.format(
                    "Failed to check if [%s] behind [%s]",
                    behind,
                    tracking), e);
        } 
	    finally {
	      walk.dispose();
	    }
	  }
}
