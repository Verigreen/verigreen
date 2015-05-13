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
package com.verigreen.common.testcase;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;
import org.springframework.test.context.ContextConfiguration;

import com.verigreen.common.concurrency.SynchronizeableThreadPoolExecutor;
import com.verigreen.common.spring.SpringTestCase;
import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.concurrency.ExecutorServiceFactory;
import com.verigreen.common.concurrency.Locker;
import com.verigreen.common.concurrency.RuntimeUtils;

@ContextConfiguration(locations = {
        "/Spring/verigreen-collector-jbosscache-test-context.xml",
        "/Spring/verigreen-collector-container-context.xml",
        "/Spring/verigreen-collector-common-context.xml",
        "/Spring/verigreen-collector-jenkins-server-context.xml",
        "/Spring/verigreen-collector-jenkins-verifier-context.xml",
        "/Spring/verigreen-collector-handlers-context.xml",
        "/Spring/verigreen-collector-queue-test-context.xml" })
@Ignore
public class CollectorSpringTestCase extends SpringTestCase {
    
    @Rule
    public MethodRule watchman = new TestWatchman() {
        
        @Override
        public void starting(FrameworkMethod method) {
            
            VerigreenLogger.get().log(
                    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format("Starting test: %s()", method.getName()));
        }
    };
    
    @BeforeClass
    public static void setupClass() {
        
        ExecutorServiceFactory.setCachedThreadPoolExecutor(new SynchronizeableThreadPoolExecutor());
    }
    
    @Before
    public void setUp() {
        
        CollectorApi.getCache().start();
    }
    
    @After
    public void teardown() {
        
        CollectorApi.getCache().destroy();
        Locker.getInstance().reset();
    }
}
