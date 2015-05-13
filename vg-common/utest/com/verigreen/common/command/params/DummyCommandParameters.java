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
package com.verigreen.common.command.params;

import com.verigreen.common.command.params.TEQCommandParameters;

public class DummyCommandParameters implements TEQCommandParameters {
    
    private final int _id;
    private final String _name;
    private final boolean _flag;
    
    public DummyCommandParameters(int id, String name, boolean flag) {
        
        _id = id;
        _name = name;
        _flag = flag;
    }
    
    public int getId() {
        
        return _id;
    }
    
    public String getName() {
        
        return _name;
    }
    
    public boolean isFlag() {
        
        return _flag;
    }
    
    @Override
    public int hashCode() {
        
        final int prime = 31;
        int result = 1;
        result = prime * result + (_flag ? 1231 : 1237);
        result = prime * result + _id;
        result = prime * result + ((_name == null) ? 0 : _name.hashCode());
        
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DummyCommandParameters other = (DummyCommandParameters) obj;
        if (_flag != other._flag)
            return false;
        if (_id != other._id)
            return false;
        if (_name == null) {
            if (other._name != null)
                return false;
        } else if (!_name.equals(other._name))
            return false;
        return true;
    }
}
