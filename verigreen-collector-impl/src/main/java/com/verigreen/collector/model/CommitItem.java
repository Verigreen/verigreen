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
package com.verigreen.collector.model;

import java.net.URI;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.verigreen.collector.api.BranchDescriptor;
import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.observer.Observer;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.jbosscache.entity.UUIDEntity;
import com.verigreen.common.utils.LocalMachineCurrentTimeProvider;
@Component
public class CommitItem extends UUIDEntity implements Comparable<CommitItem>, Observer {
    
    private static final long serialVersionUID = -500692221789722476L;
    
    public static final CommitItem EMPTY = new CommitItem();
    
    // linked list
    private CommitItem _parent;
    private CommitItem _child;
    
    private BranchDescriptor _branchDescriptor;
    private String _mergedBranchName;
    private VerificationStatus _status = VerificationStatus.NOT_STARTED;
    private Date _creationTime = new LocalMachineCurrentTimeProvider().getCurrentTime();
    private Date _runTime;
    private Date _endTime;
    private URI _buildUrl;
    private int _buildNumber;
    private boolean _isDone = false;
    private String _childCommit = "";
    private int _timeoutCounter = 0;
	private int _retriableCounter = 0; 
    private int _buildNumberToStop = 0;
	private boolean _triggeredAttempt = false;
    
    public CommitItem() {}
    
    public CommitItem(BranchDescriptor branchDescriptor) {
        
        _branchDescriptor = branchDescriptor;
        setMergedBranchName(branchDescriptor.getNewBranch() + "_new_" + System.currentTimeMillis());
    }
    
    public static boolean isEmpty(CommitItem value) {
        
        return EMPTY.equals(value);
    }
    public int getBuildNumberToStop() {
		return _buildNumberToStop;
	}

	public void setBuildNumberToStop(int _buildNumberToStop) {
		this._buildNumberToStop = _buildNumberToStop;
	}
    public int getTimeoutCounter() {
		return _timeoutCounter;
	}

	public void setTimeoutCounter(int timeoutCounter) {
		this._timeoutCounter = timeoutCounter;
	}

	public int getRetriableCounter() {
		return _retriableCounter;
	}

	public void setRetriableCounter(int retriableCounter) {
		this._retriableCounter = retriableCounter;
	}
    
    public VerificationStatus getStatus() {
        
        return _status;
    }
    
    public Date getRunTime() {
        
        return _runTime;
    }
    
    public void setRunTime(Date runTime) {
        
        _runTime = runTime;
    }
    
    public void setCreationTime(Date creationTime) {
        
        _creationTime = creationTime;
    }
    
    public Date getCreationTime() {
        
        return _creationTime;
    }
    
    public void setStatus(VerificationStatus status) {
        
        _status = status;
    }
    
    public String getMergedBranchName() {
        
        return _mergedBranchName;
    }
    
    public void setMergedBranchName(String mergedBranchName) {
        
        _mergedBranchName = mergedBranchName;
    }
    
    public BranchDescriptor getBranchDescriptor() {
        
        return _branchDescriptor;
    }
    
    public void setBranchDescriptor(BranchDescriptor branchDescriptor) {
        
        _branchDescriptor = branchDescriptor;
    }
    
    public URI getBuildUrl() {
        
        return _buildUrl;
    }
    
    public void setBuildUrl(URI buildUrl) {
        
        _buildUrl = buildUrl;
    }
    
    public void setDone(boolean value) {
        
        _isDone = value;
    }
    
    public boolean isDone() {
        
        return _isDone;
    }
    
    public CommitItem getParent() {
        
        CommitItem ret = null;
        
        if (_parent != null) {
            ret = CollectorApi.getCommitItemContainer().get(_parent.getKey());
        }
        
        return ret;
    }
    
    public void setParent(CommitItem parent) {
        
        _parent = parent;
        if (parent != null) {
            parent.setChild(this);
            CollectorApi.getCommitItemContainer().save(parent);
        }
    }
    
    public void setChild(CommitItem child) {
        
        _child = child;
    }
    
    public CommitItem getChild() {
        
        CommitItem ret = null;
        
        if (_child != null) {
            ret = CollectorApi.getCommitItemContainer().get(_child.getKey());
        }
        
        return ret;
    }
    
    public String getChildCommit() {
        
        return _childCommit;
    }
    
    public void setChildCommit(String childCommit) {
        
        _childCommit = childCommit;
    }
    
    public int getBuildNumber() {
        
        return _buildNumber;
    }
    
    public void setBuildNumber(int buildNumber) {
        
        _buildNumber = buildNumber;
    }
    
    public Date getEndTime() {
        
        return _endTime;
    }
    
    public void setEndTime(Date endTime) {
        
        _endTime = endTime;
    }

	@Override
    public int compareTo(CommitItem item) {
        
        return _creationTime.compareTo(item.getCreationTime());
    }
	
    
	@Override
	public void update(VerificationStatus status) {
		
		this.setStatus(status);
		
	}
	
	public boolean isTriggeredAttempt() {
		return _triggeredAttempt;
	}
	
	public void setTriggeredAttempt(boolean triggeredAttempt) {
		this._triggeredAttempt = triggeredAttempt;
	}
	
    @Override
    public String toString() {
        
        return String.format(
                "CommitItem [\n\t_branchDescriptor=%s,\n\t_mergedBranchName=%s, _status=%s, _creationTime=%s, _runTime=%s, _endTime=%s, _buildUrl=%s, _isDone=%s, _parent=%s, _child=%s, "
                + "_timeoutCounter=%s, _retriableCounter=%s, _triggeredAttempt=%s, _buildNumberToStop=%s] ",
                _branchDescriptor,
                _mergedBranchName,
                _status,
                _creationTime,
                _runTime,
                _endTime,
                _buildNumber,
                _isDone,
                _parent != null ? _parent.getKey() : null,
                _child != null ? _child.getKey() : null,
        		_timeoutCounter,
        		_retriableCounter,
        		_triggeredAttempt,
        		_buildNumberToStop);
        		
    }

	
}
