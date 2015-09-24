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
package com.verigreen.collector.core;

import java.util.Collection;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.verigreen.collector.buildverification.JenkinsUpdater;
import com.verigreen.collector.common.CommitItemUtils;
import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.concurrency.RuntimeUtils;

public class ServerListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        
        CollectorApi.getCache().start();
        CollectorApi.getJobScheduler().start();
        loadCacheToUpdateList();
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        
        CollectorApi.getJobScheduler().shutdown();
        CollectorApi.getCache().destroy();
    }
    
    /**
     * In case collector was restarted while there were running processes.
     * This method will update the cache to the JenkinsUpdater list so the running processes will continue the flow.  
     */
    private void loadCacheToUpdateList() {
		JenkinsUpdater jenkinsUpdater = JenkinsUpdater.getInstance();
		Collection<CommitItem> running = CommitItemUtils.getRunning();
		
		if (running.size() > 0){
			VerigreenLogger.get().log(
                    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format("Updating JenkinsUpdater with %d running processes from cache", running.size()));
		}
		for (CommitItem commitItem : running) {
			jenkinsUpdater.getObservers().add(CollectorApi.getCommitItemContainer().get(commitItem.getKey()));
		}
	}
}
