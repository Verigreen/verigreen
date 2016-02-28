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
package com.verigreen.common.spring;

import com.verigreen.collector.api.BranchDescriptor;
import com.verigreen.rest.CreateBranchRequest;
import com.verigreen.spring.common.CollectorApi;
import com.verigreen.common.concurrency.RuntimeUtils;
import com.verigreen.common.spring.SpringContextHolder;

public class GitHookApi {

    private GitHookApi() {
    }

    public static CreateBranchRequest getCreateBranchRequest(BranchDescriptor branchDescriptor) {
        
        return RuntimeUtils.cast(getBean(
                "createBranchRequest",
                CollectorApi.getCollectorAddress(),
                branchDescriptor));
    }
    
    private static Object getBean(String beanId, Object... constructorArgs) {
        
        return SpringContextHolder.getInstance().getBean(beanId, constructorArgs);
    }
}
