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

import com.verigreen.common.command.params.TEQCommandParameters;

public abstract class AbstractTECommand<TTEQCommandParameters extends TEQCommandParameters>
        implements TECommand<TTEQCommandParameters> {
    
    private final String _type;
    private final Priority _priority;
    private final boolean _isCritical;
    
    public AbstractTECommand(String type, Priority priority) {
        
        this(type, priority, false);
    }
    
    public AbstractTECommand(String type, Priority priority, boolean isCritical) {
        
        _type = type;
        _priority = priority;
        _isCritical = isCritical;
    }
    
    @Override
    public String getType() {
        
        return _type;
    }
    
    @Override
    public Priority getPriority() {
        
        return _priority;
    }
    
    @Override
    public boolean isCritical() {
        
        return _isCritical;
    }
    
    @Override
    public int compareTo(TECommand<TTEQCommandParameters> command) {
        
        // works with enum compareTo method - the order of the enum values' declaration matters
        return _priority.compareTo(command.getPriority());
    }
    
    @Override
    public String toString() {
        
        return _type;
    }
}
