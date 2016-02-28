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
package com.verigreen.collector.spring;

import com.offbytwo.jenkins.JenkinsServer;
import com.verigreen.collector.buildverification.BuildVerifier;
import com.verigreen.collector.buildverification.CommitItemCanceler;
import com.verigreen.collector.buildverification.CommitItemVerifier;
import com.verigreen.collector.buildverification.CommitItemVerifierManager;
import com.verigreen.collector.cache.container.CommitItemContainer;
import com.verigreen.collector.common.EmailSender;
import com.verigreen.collector.common.VerigreenNeededLogic;
import com.verigreen.collector.decision.CheckinDecisionHandler;
import com.verigreen.collector.decision.OnFailedByParentHandler;
import com.verigreen.collector.decision.OnSuccessByChildHandler;
import com.verigreen.collector.jobs.JobScheduler;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.common.concurrency.RuntimeUtils;
import com.verigreen.common.jbosscache.CacheInstance;
import com.verigreen.common.logger.Logger;
import com.verigreen.common.spring.SpringContextHolder;
import com.verigreen.jgit.SourceControlOperator;

public class CollectorApi {

    private CollectorApi() {
    }

    public static CacheInstance getCache() {
        
        return getBean(CacheInstance.class);
    }
    
    public static JenkinsServer getJenkinsServer() {
        
        return getBean(JenkinsServer.class);
    }
    
    public static BuildVerifier getJenkinsVerifier() {
        
        return getBean(BuildVerifier.class);
    }
    
    public static EmailSender getEmailSender() {
        
        return getBean(EmailSender.class);
    }
    
    public static SourceControlOperator getSourceControlOperator() {
        
        return getBean(SourceControlOperator.class);
    }
    
    public static CommitItemContainer getCommitItemContainer() {
        
        return getBean(CommitItemContainer.class);
    }
    
    public static Logger getLogger() {
        
        return getBean(Logger.class);
    }
    
    public static JobScheduler getJobScheduler() {
        
        return getBean(JobScheduler.class);
    }
    
    public static CheckinDecisionHandler getCheckinDecisionHandler(CommitItem commitItem) {
        
        return RuntimeUtils.cast(SpringContextHolder.getInstance().getBean(
                "checkinDecisionHandler",
                commitItem));
    }
    
    public static OnSuccessByChildHandler getOnSuccessByChildHandler(CommitItem commitItem) {
        
        return RuntimeUtils.cast(SpringContextHolder.getInstance().getBean(
                "onSuccessByChildHandler",
                commitItem));
    }
    
    public static OnFailedByParentHandler getOnFailedByParentHandler(CommitItem commitItem) {
        
        return RuntimeUtils.cast(SpringContextHolder.getInstance().getBean(
                "onFailedByParentHandler",
                commitItem));
    }
    
    public static String getVerificationJobName() {
        
    	if (VerigreenNeededLogic.VerigreenMap.get("_jobName") == null){
    		VerigreenNeededLogic.VerigreenMap.put("_jobName", SpringContextHolder.getInstance().getBean("verificationJobName").toString());
    	}
    	return VerigreenNeededLogic.VerigreenMap.get("_jobName");
    }
    
    public static String getBranchParamName() {
        
        return RuntimeUtils.cast(SpringContextHolder.getInstance().getBean("branchParamName"));
    }
    
    public static VerigreenNeededLogic getVerigreenNeededLogic() {
        
        return getBean(VerigreenNeededLogic.class);
    }
    
    public static CommitItemVerifier getCommitItemVerifier() {
        
        return getBean(CommitItemVerifier.class);
    }
    
    public static CommitItemCanceler getCommitItemCanceler() {
        
        return getBean(CommitItemCanceler.class);
    }
    
    public static CommitItemVerifierManager getCommitItemVerifierManager() {
        
        return getBean(CommitItemVerifierManager.class);
    }
    
    private static <T> T getBean(Class<T> beanClass) {
        
        return SpringContextHolder.getInstance().getBean(beanClass);
    }
}
