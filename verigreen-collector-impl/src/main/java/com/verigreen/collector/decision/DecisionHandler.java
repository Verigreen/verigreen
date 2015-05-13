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
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.utils.LocalMachineCurrentTimeProvider;

public abstract class DecisionHandler {
    
    protected CommitItem _commitItem;
    
    public DecisionHandler(CommitItem commitItem) {
        
        _commitItem = commitItem;
    }

    public void handle() {
        
        doHandle();
        if (_commitItem.isDone()) {
            _commitItem.setEndTime(new LocalMachineCurrentTimeProvider().getCurrentTime());
            CollectorApi.getCommitItemContainer().save(_commitItem);
        }
    }
    protected abstract void doHandle();

}
