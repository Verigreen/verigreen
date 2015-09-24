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
package com.verigreen.collector.decision;

import org.eclipse.jgit.lib.Constants;

import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.buildverification.CommitItemVerifier;
import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.concurrency.RuntimeUtils;
import com.verigreen.common.utils.LocalMachineCurrentTimeProvider;
import com.verigreen.common.utils.Pair;
import com.verigreen.jgit.SourceControlOperator;

public class CheckinDecisionHandler extends DecisionHandler {
	
	private CommitItemVerifier verifier = CommitItemVerifier.getInstance();

	public CheckinDecisionHandler(CommitItem commitItem) {

		super(commitItem);
	}

	@Override
	protected void doHandle() {

		boolean updated = updateBranch();
		if (updated) {
			VerigreenLogger.get().log(
					getClass().getName(),
					RuntimeUtils.getCurrentMethodName(),
					String.format(
							"Update branch (%s) succeeded, going to verify...",
							_commitItem));
			updateCommitItemToRunning();
			CollectorApi.getCommitItemVerifierManager().add(_commitItem.getKey(), verifier);
			verifier.verify(_commitItem);
		}
	}

	private boolean updateBranch() {

		boolean ret = false;
		try {
			ret = doUpdateBranch();
		} catch (Exception e) {
			VerigreenLogger.get().error(
					getClass().getName(),
					RuntimeUtils.getCurrentMethodName(),
					String.format("Failed updating branch [%s]", _commitItem
							.getBranchDescriptor().getNewBranch()), e);
			_commitItem.setStatus(VerificationStatus.GIT_FAILURE);
		}
		CollectorApi.getCommitItemContainer().save(_commitItem);

		return ret;
	}

	private boolean doUpdateBranch() {

		VerigreenLogger.get().log(
				getClass().getName(),
				RuntimeUtils.getCurrentMethodName(),
				String.format(
						"Going to merge branch with latest changes... (%s)",
						_commitItem.getBranchDescriptor().getNewBranch()));

		return createMergedBranch();
	}

	private boolean createMergedBranch() {

		String mergedBranchName = _commitItem.getMergedBranchName();
		// create new branch that will be merged with parent (master/parent
		// commit)
		SourceControlOperator sourceControlOperator = CollectorApi
				.getSourceControlOperator();
		if (sourceControlOperator.isBranchExist(mergedBranchName)) {
			String newName = _commitItem.getBranchDescriptor().getNewBranch()
					+ "_new_" + System.currentTimeMillis();
			_commitItem.setMergedBranchName(newName);
			mergedBranchName = newName;
		}
		updateProtectedBranch(sourceControlOperator);
		Pair<Boolean, String> result = createBranchAndMergeWithParent(
				mergedBranchName, sourceControlOperator);
		boolean ret = result.getFirst();
		if (!ret) {
			_commitItem.setStatus(VerificationStatus.MERGE_FAILED);
		} else {
			// save the automatic commit so the hook will pass it
			_commitItem.setChildCommit(result.getSecond());

			CollectorApi.getCommitItemContainer().save(_commitItem);
			ret = sourceControlOperator
					.push(mergedBranchName, mergedBranchName);
			if (!ret) {
				_commitItem.setStatus(VerificationStatus.GIT_FAILURE);
			}
		
		}

		return ret;
	}

	private Pair<Boolean, String> createBranchAndMergeWithParent(
			String mergedBranchName, SourceControlOperator sourceControlOperator) {

		String parent = _commitItem.getParent() == null ? Constants.HEAD
				: _commitItem.getParent().getMergedBranchName();
		sourceControlOperator.createBranch(parent, mergedBranchName);
		// switch to the new branch and merge latest commit
		sourceControlOperator.checkout(mergedBranchName, false, false); // false

		return sourceControlOperator.merge(mergedBranchName, _commitItem
				.getBranchDescriptor().getNewBranch());
	}

	private void updateProtectedBranch(
			SourceControlOperator sourceControlOperator) {

		sourceControlOperator.fetch();
		boolean createNewLocalBranch = !sourceControlOperator
				.isBranchExist(_commitItem.getBranchDescriptor()
						.getProtectedBranch());
		sourceControlOperator.checkout(_commitItem.getBranchDescriptor()
				.getProtectedBranch(), createNewLocalBranch, false); // was false
	}

	private void updateCommitItemToRunning() {

		_commitItem.setStatus(VerificationStatus.RUNNING);
		_commitItem.setRunTime(new LocalMachineCurrentTimeProvider()
				.getCurrentTime());
		CollectorApi.getCommitItemContainer().save(_commitItem);
	}
}
