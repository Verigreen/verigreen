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
package com.verigreen.common.command;

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.verigreen.common.command.params.TEQCommandParameters;
import com.verigreen.common.concurrency.RuntimeUtils;

public class CommandPack<TTEQCommandParameters extends TEQCommandParameters> {
    
    private final TECommand<TTEQCommandParameters> _command;
    private final TTEQCommandParameters _params;
    private final CommandResult _error = new CommandResult();
    
    public CommandPack(TECommand<TTEQCommandParameters> command, TTEQCommandParameters params) {
        
        _command = command;
        _params = params;
    }
    
    public TECommand<TTEQCommandParameters> getCommand() {
        
        return _command;
    }
    
    public TTEQCommandParameters getParameters() {
        
        return _params;
    }
    
    public void setError(String error) {
        
        _error.setError(error);
    }
    
    public String getError() {
        
        return _error.getErrorDeiscription();
    }
    
    @Override
    public int hashCode() {
        
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCommand().getType());
        builder.append(getParameters().hashCode());
        
        return builder.toHashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if (super.equals(obj)) {
            
            return true;
        }
        if (obj == null) {
            
            return false;
        }
        if (getClass() != obj.getClass()) {
            
            return false;
        }
        final CommandPack<TTEQCommandParameters> other = RuntimeUtils.cast(obj);
        if (getCommand() == null
            || other.getCommand() == null
            || getParameters() == null
            || other.getParameters() == null) {
            
            return false;
        }
        if (!getCommand().getType().equals(other.getCommand().getType())) {
            
            return false;
        }
        
        return getParameters().equals(other.getParameters());
    }
    
    @Override
    public String toString() {
        
        return String.format("CommandPack [command=%s, params=%s]", _command, _params);
    }
}
