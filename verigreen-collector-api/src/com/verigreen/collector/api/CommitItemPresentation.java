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
package com.verigreen.collector.api;

import java.net.URI;
import java.util.Date;

import com.verigreen.common.utils.LocalMachineCurrentTimeProvider;

public class CommitItemPresentation {
    
    private BranchDescriptor _branchDescriptor;
    private VerificationStatus _status = VerificationStatus.NOT_STARTED;
    private Date _creationTime = new LocalMachineCurrentTimeProvider().getCurrentTime();
    private Date _runTime;
    private Date _endTime;
    private URI _buildUrl;
    private String _parentBranch;
    
    public CommitItemPresentation() {}
    
    public CommitItemPresentation(BranchDescriptor branchDescriptor) {
        
        _branchDescriptor = branchDescriptor;
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
    
    public String getParentBranch() {
        
        return _parentBranch;
    }
    
    public void setParentBranch(String parentBranch) {
        
        _parentBranch = parentBranch;
    }
    
    public Date getEndTime() {
        
        return _endTime;
    }
    
    public void setEndTime(Date endTime) {
        
        _endTime = endTime;
    }

	@Override
    public String toString() {
        
        return String.format(
                "CommitItemPresentation [_branchDescriptor=%s, _status=%s, _creationTime=%s, _runTime=%s, _endTime=%s, _parentBranch=%s]",
                _branchDescriptor,
                _status,
                _creationTime,
                _runTime,
                _endTime,
                _parentBranch);
    }
}
