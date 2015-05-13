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
package com.verigreen.spring.common;

import com.verigreen.rest.CommitItemRequest;
import com.verigreen.rest.VerigreenNeededRequest;
import com.verigreen.common.concurrency.RuntimeUtils;
import com.verigreen.common.spring.SpringContextHolder;

public class CollectorApi {
    
    public static String getCollectorAddress() {
    	String address = RuntimeUtils.cast(getBean("collectorAddress"));
    	return address;    
    }
    
    public static CommitItemRequest getCommitItemRequest(
            String vgBranchName,
            String protectedBranch,
            String commitId) {
        
        return RuntimeUtils.cast(getBean(
                "commitItemRequest",
                getCollectorAddress(),
                protectedBranch,
                vgBranchName,
                commitId));
    }
    
    public static VerigreenNeededRequest getVerigreenNeededRequest(
            String branchName,
            String vgBranchName,
            String commitId,
            String committer) {
        
        return RuntimeUtils.cast(getBean(
                "verigreenNeededRequest",
                getCollectorAddress(),
                branchName,
                vgBranchName,
                commitId,
                committer));
    }
    
    public static VerigreenNeededRequest getPostVerigreenNeededRequest(String commitId) {
        
        return RuntimeUtils.cast(getBean(
                "postVerigreenNeededRequest",
                getCollectorAddress(),
                commitId));
    }
    
    private static Object getBean(String beanId, Object... constructorArgs) {
        
        return SpringContextHolder.getInstance().getBean(beanId, constructorArgs);
    }
    
    private static Object getBean(String beanId) {
        
        return SpringContextHolder.getInstance().getBean(beanId);
    }
    
}
