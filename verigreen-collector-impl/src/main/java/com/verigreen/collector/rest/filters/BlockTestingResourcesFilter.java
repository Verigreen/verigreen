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
package com.verigreen.collector.rest.filters;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.container.ContainerRequestContext;

import org.springframework.util.StringUtils;

import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.collector.rest.VerigreenNeededResource;
import com.verigreen.collector.rest.exceptions.RestAccessDeniedException;
import com.verigreen.common.concurrency.RuntimeUtils;

/**
 * This filter blocks resources which are used in system test, so that they won't be available in
 * production
 * 
 * @author manashir
 * 
 */
public class BlockTestingResourcesFilter implements javax.ws.rs.container.ContainerRequestFilter {
    
    private static final Set<String> blockedResourcesPaths;
    
    static {
        blockedResourcesPaths = new HashSet<>(1);
        blockedResourcesPaths.add(trim(
                VerigreenNeededResource.VERIGREEN_NEEDED_ROOT_PATH
                        + VerigreenNeededResource.PROTECTED_BRANCHES_RELATIVE_PATH,
                '/'));
    }
    
    //TODO need to make sure that this method is still working
   /* @Override
    public ContainerRequest filter(ContainerRequest containerRequest) {
        
        boolean testsAreRunning = Boolean.valueOf(System.getProperty("tests.running"));
        String relativeUri = trim(containerRequest.getPath(false), '/');
        if (!testsAreRunning && blockedResourcesPaths.contains(relativeUri)) {
            VerigreenLogger.get().log(
                    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format("REST API with relative path '%s' was blocked", relativeUri));
            
            throw new RestAccessDeniedException(String.format(
                    "The resource by path '%s' is restricted",
                    relativeUri));
        }
        
        return containerRequest;
    }*/
    
    private static String trim(String s, char charToTrim) {
        
        String result = StringUtils.trimLeadingCharacter(s, charToTrim);
        result = StringUtils.trimTrailingCharacter(result, charToTrim);
        
        return result;
    }

	@Override
	public void filter(ContainerRequestContext requestContext)
			throws IOException {
		boolean testsAreRunning = Boolean.valueOf(System.getProperty("tests.running"));
        String relativeUri = trim(requestContext.getUriInfo().getPath(), '/');
        if (!testsAreRunning && blockedResourcesPaths.contains(relativeUri)) {
            VerigreenLogger.get().log(
                    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format("REST API with relative path '%s' was blocked", relativeUri));
            
            throw new RestAccessDeniedException(String.format(
                    "The resource by path '%s' is restricted",
                    relativeUri));
        }
        
	}
}
