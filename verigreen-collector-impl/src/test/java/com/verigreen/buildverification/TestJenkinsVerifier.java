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
package com.verigreen.buildverification;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.ImmutableMap;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.verigreen.collector.buildverification.BuildVerifier;
import com.verigreen.collector.buildverification.JenkinsVerifier;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.testcase.CollectorSpringTestCase;
import com.verigreen.common.testcase.IntegrationTest;

@ContextConfiguration(locations = {
        "/Spring/verigreen-collector-jenkins-server-context.xml",
        "/Spring/verigreen-collector-jenkins-verifier-context.xml" })
@Category(IntegrationTest.class)
@Ignore
public class TestJenkinsVerifier extends CollectorSpringTestCase {
	
    @Test
    public void testBuild() throws IOException {
        
    	String jobName = "testing-jenkins-api";
        String parameterNameForJob = "ParamForTesting";
        final ImmutableMap<String, String> params = ImmutableMap.of(parameterNameForJob, "master");
        JenkinsServer jenkninsServer = CollectorApi.getJenkinsServer();
        JobWithDetails job = jenkninsServer.getJob(jobName);
        job.build(params);
    }
    
    @Test
    public void testStopBuildById() throws IOException, InterruptedException {
        
        String jobName = "testing-jenkins-api";
        String parameterNameForJob = "ParamForTesting";
        final ImmutableMap<String, String> params = ImmutableMap.of(parameterNameForJob, "master");
        
        BuildVerifier buildVerifier = CollectorApi.getJenkinsVerifier();
        JenkinsServer jenkninsServer = CollectorApi.getJenkinsServer();
        JobWithDetails job = jenkninsServer.getJob(jobName);
        int nextBuildNumber = job.getNextBuildNumber();
        job.build(params);
        Thread.sleep(5000);
        boolean stopBuildResult =
                ((JenkinsVerifier) buildVerifier).stop(jobName, Integer.toString(nextBuildNumber));
        Assert.assertEquals(true, stopBuildResult);
    }
}
