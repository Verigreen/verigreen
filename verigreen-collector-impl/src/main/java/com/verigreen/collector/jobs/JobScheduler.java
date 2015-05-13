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

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.verigreen.collector.common.log4j.VerigreenLogger;

public class JobScheduler {
    
    private Scheduler _scheduler;
    private int _rhf;
    private int _rsf;
    
    private JobScheduler() {}
    
    private JobScheduler(int rhf, int rsf) {
    	_rhf 	= rhf;
    	_rsf 	= rsf;
    }
    
    public void start() {
        
        StdSchedulerFactory sf = new StdSchedulerFactory();
        try {
            _scheduler = sf.getScheduler();
            _scheduler.start();
            scheduleJobs();
        } catch (SchedulerException e) {
            new RuntimeException("Failed starting job scheduler", e);
        }
    }
    
    public void shutdown() {
        
        if (_scheduler != null) {
            try {
                _scheduler.shutdown();
            } catch (SchedulerException e) {
                VerigreenLogger.get().error("Failed shuting down scheduler", e);
            }
        }
    }
    
    private void scheduleJobs() throws SchedulerException {
        scheduleJob(CacheCleanerJob.class, SimpleScheduleBuilder.repeatHourlyForever(_rhf));
        scheduleJob(BranchCleanerJob.class, SimpleScheduleBuilder.repeatHourlyForever(_rhf));
        scheduleJob(ConsumerJob.class, SimpleScheduleBuilder.repeatSecondlyForever(_rsf));
    }
    
    private void scheduleJob(
    
    Class<? extends Job> jobClass, SimpleScheduleBuilder simpleScheduleBuilder)
            throws SchedulerException {
        
        JobDetail job = JobBuilder.newJob(jobClass).withIdentity(jobClass.getSimpleName()).build();
        Trigger trigger =
                TriggerBuilder.newTrigger().startNow().withSchedule(simpleScheduleBuilder).build();
        _scheduler.scheduleJob(job, trigger);
    }
}
