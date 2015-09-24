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

import java.io.IOException;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildResult;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.verigreen.collector.common.VerigreenNeededLogic;
import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.concurrency.RuntimeUtils;

public class JenkinsVerifier implements BuildVerifier {

    public static Job job2Verify = getJobToVerify();
    
    static JenkinsUpdater jenkinsUpdater = JenkinsUpdater.getInstance(); 
	
	private static int getJobRetryCounter()
	{
		int jobRetry = Integer.parseInt(VerigreenNeededLogic.properties.getProperty("job.retry.counter"));
		return jobRetry;
	}
	
	public static Job getJobToVerify()
	{
		Job jobToVerify =  null;
		int jobRetries = getJobRetryCounter();
		int retries = 1;
		while(retries <= jobRetries)
		{
		try {
			VerigreenLogger.get().log(
	                JenkinsVerifier.class.getName(),
	                RuntimeUtils.getCurrentMethodName(),
	                String.format(
	                        "Attempting to retrieve job for verification...", retries));
			jobToVerify = CollectorApi.getJenkinsServer().getJob((CollectorApi.getVerificationJobName().toLowerCase()));	
			if(jobToVerify != null)
			{
				VerigreenLogger.get().log(
						JenkinsVerifier.class.getName(),
						RuntimeUtils.getCurrentMethodName(),
						String.format(
								"Job for verification was retrieved successfully after [%s] retries", retries));
				break;
			}
			else
			{
				VerigreenLogger.get().log(
						JenkinsVerifier.class.getName(),
						RuntimeUtils.getCurrentMethodName(),
						String.format(
								"Failed to retrieve job for verification. Retrying..."));
				retries++;
			}
		}
		catch (IOException e) 
		{		
			VerigreenLogger.get().error(
                    JenkinsVerifier.class.getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format(
                            "Failed get job for verification"),e);
		}
		}
		if(jobToVerify == null)
		{
			VerigreenLogger.get().error(
                    JenkinsVerifier.class.getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format(
                            "Failed get job for verification after [%s] retries", retries - 1));
			CollectorApi.getVerigreenNeededLogic().sendEmailNotification("Failed get job for verification", "<span style='font-family:Metric;'>Failed get job for verification: "+CollectorApi.getVerificationJobName()+". Please contact your DevOps engineer, there might be a load on Jenkins that prevents creating new verification jobs.</span>", new String[] { VerigreenNeededLogic.properties.getProperty("email.address") }, VerigreenNeededLogic.getSignature());
		}
		return jobToVerify;
	}

    public static void triggerJob(CommitItem commitItem) {
    	
    	String branchName = commitItem.getMergedBranchName();

		try {
	         VerigreenLogger.get().log(JenkinsVerifier.class.getName(),
	        		 RuntimeUtils.getCurrentMethodName(),
	        		 String.format("Triggering job [%s] for branch [%s]", CollectorApi.getVerificationJobName(), branchName));
			 Map<String,String> commitParams = VerigreenNeededLogic.checkJenkinsMode(commitItem);
			 commitItem.setTriggeredAttempt(true);
			 jenkinsUpdater.register(commitItem);
			 CollectorApi.getCommitItemContainer().save(commitItem);
			 ImmutableMap.Builder<String, String> finalJenkinsParams = ImmutableMap.<String, String>builder().put("token",VerigreenNeededLogic.properties.getProperty("jenkins.password"));
			 finalJenkinsParams.put(CollectorApi.getBranchParamName(), branchName);
	         for(String key : commitParams.keySet())
	         {
	         	finalJenkinsParams.put(key,commitParams.get(key));
	         }
	          final ImmutableMap<String, String> params = finalJenkinsParams.build();
	          job2Verify.build(params);
	          
	         
		} catch (IOException e) {
		
			VerigreenLogger.get().error(
                    JenkinsVerifier.class.getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format(
                            "Failed to trigger build for job [%s] with branch [%s]",
                            CollectorApi.getVerificationJobName(),
                            branchName),e);
		} 
    }
	 

    public static String getBuildUrl(int buildNumber) {

    	String buildUrl = null;
		buildUrl = job2Verify.getUrl()+Integer.toString(buildNumber)+"/";
    	
    	return buildUrl;
    }
    
    @Override
    public boolean stop(String jobName, String buildIdToStop) {
        //TODO: Remove unnecessary calls to Jenkins for stopping a Build. Try to minimize the number of calls.   
        boolean ans = false;
        try {
            VerigreenLogger.get().log(
                    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format("Stopping build (%s)", buildIdToStop));
            JobWithDetails job = CollectorApi.getJenkinsServer().getJob(jobName);
            Build buildToStop = job.getBuildByNumber(Integer.parseInt(buildIdToStop));
            if (buildIdToStop != null) {
                buildToStop.Stop();
                ans = buildToStop.details().getResult().equals(BuildResult.ABORTED);
            } else {
                VerigreenLogger.get().error(
                        getClass().getName(),
                        RuntimeUtils.getCurrentMethodName(),
                        String.format(
                                "There is no build number [%s] for job [%s]",
                                buildIdToStop,
                                jobName));
            }
        } catch (Throwable e) {
            VerigreenLogger.get().error(
                    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format("Failed to stop build [%s] for job [%s]", buildIdToStop, jobName),
                    e);
        }
        
        return ans;
    }

    //added to HashMap
	public void setJobName(String jobName) {
		VerigreenNeededLogic.VerigreenMap.put("_jobName", jobName);
	}  

}