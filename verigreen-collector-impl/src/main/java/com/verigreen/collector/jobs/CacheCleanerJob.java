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
package com.verigreen.collector.jobs;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.common.VerigreenNeededLogic;
import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.concurrency.RuntimeUtils;
import com.verigreen.common.jbosscache.Criteria;

@DisallowConcurrentExecution
public class CacheCleanerJob implements Job {
	
    public void set_daysThreashold(int _daysThreashold) {
		VerigreenNeededLogic.VerigreenMap.put("daysThreashold", String.valueOf(_daysThreashold));
	}

	@Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        
		VerigreenLogger.get().log(
                getClass().getName(),
                RuntimeUtils.getCurrentMethodName(),
                String.format(
                        "Cache Cleaner is running, deleting items %s days old...",
                        VerigreenNeededLogic.VerigreenMap.get("daysThreashold")));
        deleteCommitItems();
    }
    
    private void deleteCommitItems() {
        
        List<CommitItem> toDelete =
                CollectorApi.getCommitItemContainer().findByCriteria(new Criteria<CommitItem>() {
                    
                    @Override
                    public boolean match(CommitItem entity) {
                        //if the item has exceeded the days threshold AND is not in a RUNNING state, it will be deleted
                        return isExceedThreashold(entity.getCreationTime()) && !isRunning(entity.getStatus());
                    }
                });
        
        CollectorApi.getCommitItemContainer().delete(toDelete);
        
        for (CommitItem currItem : toDelete) {

        		CollectorApi.getCommitItemVerifierManager().remove(currItem.getKey());
        	
        }
    }
    
    private boolean isExceedThreashold(Date date) {
        
        Days daysBetweenCreationTimeAndNow = Days.daysBetween(new DateTime(date), DateTime.now());
        
        return daysBetweenCreationTimeAndNow.getDays() >= Integer.parseInt(VerigreenNeededLogic.VerigreenMap.get("daysThreashold"));
    }
    
    private boolean isRunning(VerificationStatus status) 
    {   //checks if an item is running or not    
        if(status.equals(VerificationStatus.RUNNING))
        {
        	return true;
        }
        else
        {
        	return false;
        }
    }
}
