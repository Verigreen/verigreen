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
import java.net.URI;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import com.google.common.collect.ImmutableMap;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildResult;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.common.CollectorException;
import com.verigreen.collector.common.VerigreenNeededLogic;
import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.concurrency.RuntimeUtils;
import com.verigreen.common.utils.LocalMachineCurrentTimeProvider;
import com.verigreen.common.utils.RetriableOperationExecutor;
import com.verigreen.common.utils.StringUtils;
import com.verigreen.common.utils.RetriableOperationExecutor.RetriableOperation;

public class JenkinsVerifier implements BuildVerifier {
    
    private Build _build;
    private int _timeoutForBuildInSeconds;
    private int _sleepTimeForNextBuildNumberInSeconds;
    private int _sleepTimeForBuildProcessInSeconds;
    
    private int DEFAULT_COUNT;
    private int INITIAL_SLEEP_MILLIS;
    private int MAX_SLEEP_TIME;
	
    public int getDEFAULT_COUNT() {
		return DEFAULT_COUNT;
	}

	public void setDEFAULT_COUNT(int dEFAULT_COUNT) {
		DEFAULT_COUNT = dEFAULT_COUNT;
	}

	public int getINITIAL_SLEEP_MILLIS() {
		return INITIAL_SLEEP_MILLIS;
	}

	public void setINITIAL_SLEEP_MILLIS(int iNITIAL_SLEEP_MILLIS) {
		INITIAL_SLEEP_MILLIS = iNITIAL_SLEEP_MILLIS;
	}

	public int getMAX_SLEEP_TIME() {
		return MAX_SLEEP_TIME;
	}

	public void setMAX_SLEEP_TIME(int mAX_SLEEP_TIME) {
		MAX_SLEEP_TIME = mAX_SLEEP_TIME;
	}
    private CommitItem getCurrentCommitItem(String branchName)
    {
    	List<CommitItem> all = CollectorApi.getCommitItemContainer().getAll();
    	for(CommitItem commitItem : all)
    	{
    		String check = "origin/"+commitItem.getMergedBranchName();
    		if(check.equals(branchName))
    		{
    			return commitItem;
    		}
    	}
		return null;
    }
    @Override
    public BuildVerificationResult BuildAndVerify(
            String jobName,
            String parameterNameForJob,
            String branchName,
            BuildDataCallback callback) {
        
        VerigreenLogger.get().log(
                getClass().getName(),
                RuntimeUtils.getCurrentMethodName(),
                String.format("Triggering job [%s] for branch [%s]", jobName, branchName));
        Map<String, Job> jobs = null;
        BuildVerificationResult ret =
                new BuildVerificationResult(0, null, VerificationStatus.TRIGGER_FAILED);
        try {
            jobs = getJobsWithRetry();
            
            Job job2Verify = jobs.get(jobName.toLowerCase());
            Map<String,String> commitParams = VerigreenNeededLogic.checkJenkinsMode(getCurrentCommitItem(branchName));

            ImmutableMap.Builder<String, String> finalJenkinsParams =
                    ImmutableMap.<String, String>builder()
                .put(parameterNameForJob, branchName);
            for(String key : commitParams.keySet())
            {
            	finalJenkinsParams.put(key,commitParams.get(key));
            }
             final ImmutableMap<String, String> params = finalJenkinsParams.build();

            boolean started =
                    triggerBuildAndWaitToStart(job2Verify, params, parameterNameForJob, branchName);
            if (started) {
                if (callback != null) {
                    callback.buildStarted(new URI(_build.getUrl()), _build.getNumber());
                }
                waitForCompletion(job2Verify, branchName);
                BuildWithDetails buildDetails = getDetailsWithRetry(_build);
                boolean isStillBuilding = buildDetails.isBuilding();
                ret =
                        new BuildVerificationResult(buildDetails.getNumber(), new URI(
                                buildDetails.getUrl()), getBuildResult(
                                buildDetails,
                                isStillBuilding));
                VerigreenLogger.get().log(
                        getClass().getName(),
                        RuntimeUtils.getCurrentMethodName(),
                        String.format(
                                "Build (%d) for branch (%s) has finished with %s",
                                _build.getNumber(),
                                branchName,
                                ret.getStatus()));
            }
        } catch (Throwable e) {
            VerigreenLogger.get().error(
                    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format(
                            "Failed to trigger build for job [%s] with branch [%s]",
                            jobName,
                            branchName),
                    e);
        }
        
        return ret;
    }
    
    @Override
    public boolean stop(String jobName, String buildIdToStop) {
        
        boolean ans = false;
        JenkinsServer jenkinsServer = CollectorApi.getJenkinsServer();
        try {
            VerigreenLogger.get().log(
                    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format("Stopping build (%s)", buildIdToStop));
            JobWithDetails job = jenkinsServer.getJob(jobName);
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
    
    public void setTimeoutForBuildInSeconds(int timeoutForBuild) {
        
        _timeoutForBuildInSeconds = timeoutForBuild;
    }
    
    public void setSleepTimeForNextBuildNumberInSeconds(int sleepTimeForNextBuildNumberInSeconds) {
        
        this._sleepTimeForNextBuildNumberInSeconds = sleepTimeForNextBuildNumberInSeconds;
    }
    
    public void setSleepTimeForBuildProcessInSeconds(int sleepTimeForBuildProcessInSeconds) {
        
        this._sleepTimeForBuildProcessInSeconds = sleepTimeForBuildProcessInSeconds;
    }

    //added to HashMap
	public void setJobName(String jobName) {
		VerigreenNeededLogic.VerigreenMap.put("_jobName", jobName);
	}

    private void waitForCompletion(Job job2Verify, String branchName) throws IOException,
            InterruptedException {
        
        VerigreenLogger.get().log(
                getClass().getName(),
                RuntimeUtils.getCurrentMethodName(),
                String.format(
                        "Build (%d) for branch (%s) has started",
                        _build.getNumber(),
                        branchName));
        long timeout = System.currentTimeMillis() + _timeoutForBuildInSeconds * 1000;
        
        while (isStillBuilding() && !hasTimedOut(timeout)) {
            VerigreenLogger.get().log(
                    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format(
                            "Build (%d) for branch (%s) is still running",
                            _build.getNumber(),
                            branchName));
            Thread.sleep(_sleepTimeForBuildProcessInSeconds * 1000);
        }
    }
    
    private boolean isStillBuilding() throws IOException {
        
        return getDetailsWithRetry(_build).isBuilding();
    }
    
    private boolean hasTimedOut(long timeout) {
        
        return System.currentTimeMillis() > timeout;
    }
    
    private boolean triggerBuildAndWaitToStart(
            Job job2Verify,
            Map<String, String> parameters,
            String parameterNameForJob,
            String branchName) throws IOException, InterruptedException {
        
        boolean ret = false;
        buildWithRetry(job2Verify, parameters);
        long timeout = System.currentTimeMillis() + _timeoutForBuildInSeconds * 1000;
        
        while (!hasStarted(job2Verify, parameterNameForJob, branchName) && !hasTimedOut(timeout)) {
            Thread.sleep(_sleepTimeForNextBuildNumberInSeconds * 1000);
        }
        ret = !hasTimedOut(timeout);
        
        return ret;
    }
    
    private boolean hasStarted(Job job2Verify, String parameterNameForJob, String branchName) {
        
        try {
            _build =
                    getBuild(
                            getJobDetailsWithRetry(job2Verify).getBuilds(),
                            parameterNameForJob,
                            branchName);
            
            return _build != null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private JobWithDetails getJobDetailsWithRetry(final Job job2Verify) throws IOException {
        
        return RetriableOperationExecutor.execute(new RetriableOperation<JobWithDetails>() {
            
            @Override
            public JobWithDetails execute() {
                
                try {
                    return job2Verify.details();
                } catch (IOException e) {
                    throw new CollectorException(e);
                }
            }
        }, INITIAL_SLEEP_MILLIS, MAX_SLEEP_TIME, DEFAULT_COUNT, CollectorException.class);
    }
    
    private Build getBuild(List<Build> builds, String parameterNameForJob, String branchName) {
        
        Build ret = null;
        for (final Build currBuild : builds) {
            BuildWithDetails details = getDetailsWithRetry(currBuild);
            String branch = details.getParameters().get(parameterNameForJob);
            if (!StringUtils.isNullOrEmpty(branch) && branch.equals(branchName)) {
                ret = currBuild;
                break;
            }
            // the builds are sorted in descending order - newer are first, no need to go over past builds
            if (isInThePast(details)) {
                break;
            }
        }
        
        return ret;
    }
    
    private BuildWithDetails getDetailsWithRetry(final Build currBuild) {
        
        return RetriableOperationExecutor.execute(new RetriableOperation<BuildWithDetails>() {
            
            @Override
            public BuildWithDetails execute() {
                
                try {
                    return currBuild.details();
                } catch (IOException e) {
                    throw new CollectorException(e);
                }
            }
        }, INITIAL_SLEEP_MILLIS, MAX_SLEEP_TIME, DEFAULT_COUNT, CollectorException.class);
    }
    
    private Map<String, Job> getJobsWithRetry() throws IOException {
        
        return RetriableOperationExecutor.execute(new RetriableOperation<Map<String, Job>>() {
            
            @Override
            public Map<String, Job> execute() {
                
                try {
                    return CollectorApi.getJenkinsServer().getJobs();
                } catch (IOException e) {
                    throw new CollectorException(e);
                }
            }
        }, INITIAL_SLEEP_MILLIS, MAX_SLEEP_TIME, DEFAULT_COUNT, CollectorException.class);
    }
    
    private void buildWithRetry(final Job job2Verify, final Map<String, String> parameters) {
        
        RetriableOperationExecutor.execute(new RetriableOperation<Void>() {
            
            @Override
            public Void execute() {
                
                try {
                    job2Verify.build(parameters);
                } catch (IOException e) {
                    throw new CollectorException(e);
                }
                
                return null;
            }
        }, INITIAL_SLEEP_MILLIS, MAX_SLEEP_TIME, DEFAULT_COUNT, CollectorException.class);
    }
    
    private boolean isInThePast(BuildWithDetails details) {
        
        Timestamp currentTime = new LocalMachineCurrentTimeProvider().getCurrentTime();
        long buildStartTime = details.getTimestamp();
        
        return Seconds.secondsBetween(new DateTime(buildStartTime), new DateTime(currentTime)).getSeconds() > _timeoutForBuildInSeconds;
    }
    
    private VerificationStatus getBuildResult(BuildWithDetails buildDetails, boolean isStillBuilding) {
        
        return buildDetails.getResult() == com.offbytwo.jenkins.model.BuildResult.SUCCESS
                ? VerificationStatus.PASSED
                : isStillBuilding ? VerificationStatus.TIMEOUT : VerificationStatus.FAILED;
    }
}

