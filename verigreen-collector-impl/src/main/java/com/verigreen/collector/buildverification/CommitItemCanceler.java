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
package com.verigreen.collector.buildverification;

import java.util.ArrayList;
import java.util.List;

import com.verigreen.collector.model.CommitItem;

public class CommitItemCanceler {

	private List<CommitItem> commitsToStop = new ArrayList<CommitItem>();
    private static volatile CommitItemCanceler instance = null;
    
	protected CommitItemCanceler() {
	      // Exists only to defeat instantiation.
	}
	
	public static CommitItemCanceler getInstance()
    { 
    	if(instance == null)
    	{
    		synchronized(CommitItemCanceler.class)
    		{ 
    			if(instance == null)
    			{ 
    				instance = new CommitItemCanceler();  
    			}
    		}
    	}
    	return instance; 
    }
    
    public void add(final CommitItem item) {
        
    	commitsToStop.add(item);
    }
    
    public void remove(final CommitItem item) {
        
    	commitsToStop.remove(item);
    }
    
    public List<CommitItem> getCommitItems(){
		return this.commitsToStop;
	}

}
