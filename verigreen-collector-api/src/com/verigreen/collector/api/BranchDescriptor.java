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

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class BranchDescriptor implements Serializable {
    
    private static final long serialVersionUID = 5242921918233803726L;
    
    private String _protectedBranch;
    private String _newBranch;
    private String _commitId;
    private String _committer;

	public String getProtectedBranch() {
        
        return _protectedBranch;
    }
    
    public void setProtectedBranch(String name) {
        
        _protectedBranch = name;
    }
    
    public String getNewBranch() {
        
        return _newBranch;
    }
    
    public void setNewBranch(String name) {
        
        _newBranch = name;
    }
    
    public String getCommitter() {
        
        return _committer;
    }
    
    public void setCommitter(String committer) {
        
        _committer = committer;
    }
    
    public String getCommitId() {
        
        return _commitId;
    }
    
    public void setCommitId(String commitId) {
        
        _commitId = commitId;
    }
    
    @Override
    public int hashCode() {
        
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append((_committer == null) ? 0 : _committer.hashCode());
        builder.append((_newBranch == null) ? 0 : _newBranch.hashCode());
        builder.append((_protectedBranch == null) ? 0 : _protectedBranch.hashCode());
        
        return builder.toHashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if (this == obj)
            return true;
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        BranchDescriptor other = (BranchDescriptor) obj;
        
        return new EqualsBuilder().append(_committer, other._committer).append(_newBranch, _newBranch).append(
                _protectedBranch,
                other._protectedBranch).isEquals();
    }
    
    @Override
    public String toString() {
        
        return String.format(
                "BranchDescriptor [_protectedBranch=%s, _newBranch=%s, _commiter=%s]",
                _protectedBranch,
                _newBranch,
                _committer);
    }
}
