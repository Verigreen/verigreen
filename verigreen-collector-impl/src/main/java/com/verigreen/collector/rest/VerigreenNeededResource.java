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
package com.verigreen.collector.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.verigreen.collector.api.Branches;
import com.verigreen.collector.api.Users;
import com.verigreen.collector.api.VerigreenNeeded;
import com.verigreen.collector.common.VerigreenNeededLogic;
import com.verigreen.collector.spring.CollectorApi;

@Path(VerigreenNeededResource.VERIGREEN_NEEDED_ROOT_PATH)
public class VerigreenNeededResource {
    
    public static final String VERIGREEN_NEEDED_ROOT_PATH = "/verigreen-needed";
    public static final String PROTECTED_BRANCHES_RELATIVE_PATH = "/protected-branches";
    public static final String PERMITTED_USERS_RELATIVE_PATH = "/permitted-users";
	public static final String POST_COMMIT_PASS = "/add-commit";
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @QueryParam("parentBranchName") final String parentBranchName,
            @QueryParam("branchNameToVerify") final String branchNameToVerify,
            @QueryParam("commitId") final String commitId,
            @QueryParam("committer") final String committer) {
        
        return checkIfVerigreenNeeded(parentBranchName, branchNameToVerify, commitId, committer);
    }
    
    @PUT
    @Path(PROTECTED_BRANCHES_RELATIVE_PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putProtected(Branches branches) {
        
        CollectorApi.getVerigreenNeededLogic().setProtectedBranches(branches.getBranches());
        
        return Response.ok().build();
    }
    @PUT
    @Path(PERMITTED_USERS_RELATIVE_PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putPermittedUser(Users users) {
        
        CollectorApi.getVerigreenNeededLogic().setPermittedUsers(users.getUsers());
        
        return Response.ok().build();
    }
    
    private Response checkIfVerigreenNeeded(
            String parentBranchName,
            String branchNameToVerify,
            String commitId,
            String committer) {
        
        Status status = Status.OK;
        VerigreenNeeded verigreenNeeded =
                CollectorApi.getVerigreenNeededLogic().isVerigreenNeeded(
                        parentBranchName,
                        branchNameToVerify,
                        commitId,
                        committer);
        CollectorApi.getLogger().info(verigreenNeeded.toString());
        
        return Response.status(status).entity(verigreenNeeded).build();
    }
    @POST
    @Path(POST_COMMIT_PASS)
    public Response addCommitPassHook(@QueryParam("commitId") final String commitId){
    	StringBuffer commitIdList = new StringBuffer().append(VerigreenNeededLogic.VerigreenMap.get("_passCommit") == null? "" : VerigreenNeededLogic.VerigreenMap.get("_passCommit"));
    	if (commitIdList == null || commitIdList.toString().isEmpty())
    		commitIdList.append(commitId);
    	else{
    		commitIdList.append(", ").append(commitId); 
    	}
    	VerigreenNeededLogic.VerigreenMap.put("_passCommit", commitIdList.toString());
    	return Response.ok().build();
    }
}
