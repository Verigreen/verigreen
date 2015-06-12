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

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONException;

import com.verigreen.collector.api.BranchDescriptor;
import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.common.CommitItemUtils;
import com.verigreen.collector.common.VerigreenNeededLogic;
import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.concurrency.RuntimeUtils;


@Path("/branches")
public class BranchResource {

	@POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response post(BranchDescriptor branch, @QueryParam("password") String password) {
    	CommitItem commitItem = null;
    	if (password == null || password.isEmpty()){
    		commitItem = findCommitItem(branch);
	    	if (commitItem == null){
	    		commitItem = new CommitItem(branch);
	    	}
	    	CollectorApi.getCommitItemContainer().save(commitItem);
	        VerigreenLogger.get().log(
	                getClass().getName(),
	                RuntimeUtils.getCurrentMethodName(),
	                String.format("CommitItem created %s", commitItem));
	        
	        return Response.status(Status.CREATED).entity(branch.toString()).build();
    	}
    	else{
    		if (validatePassword(password)){
        		commitItem = retrieveCommitItem(branch);
        		if (commitItem != null){
        			commitItem.setStatus(VerificationStatus.FORCING_PUSH);
        			commitItem.setDone(false);
        			CollectorApi.getCommitItemContainer().save(commitItem);
        			VerigreenLogger.get().log(
        	                getClass().getName(),
        	                RuntimeUtils.getCurrentMethodName(),
        	                String.format("Forcing push that got failed %s", commitItem));
        			return Response.status(Status.CREATED).entity(branch.toString()).build();
        		}
        	}
        	return Response.status(Status.UNAUTHORIZED).entity(branch.toString()).build();
    	}
    }
    
    private boolean validatePassword(String password) {
    	String keptPassword = VerigreenNeededLogic.VerigreenMap.get("_hashedPassword");
    	if (keptPassword  == null)
    		return false;
    	String hashedPassword = getHashedPassword(password);
    	if (keptPassword.equals(hashedPassword))
    		return true;
    	return false;
	}

	private CommitItem findCommitItem(BranchDescriptor branch){
    	CommitItem localCommitItem = retrieveCommitItem(branch);
        if (localCommitItem != null){
	        localCommitItem.setStatus(VerificationStatus.NOT_STARTED);
	        localCommitItem.setDone(false);
        }
        return localCommitItem;
    }

	/**
	 * @param branch
	 * @return
	 */
	private CommitItem retrieveCommitItem(BranchDescriptor branch) {
		CommitItem localCommitItem = null;
    	List<CommitItem> items = CollectorApi.getCommitItemContainer().getAll(); // need to find away to use CollectorApi.getCommitItemContainer().get(String key) instead of getting all and loop
        for (CommitItem currItem : items) {
          if(currItem.getBranchDescriptor().getNewBranch().equals(branch.getNewBranch())){
        	  localCommitItem = currItem;
        	  try {
				CommitItemUtils.createJsonFile(localCommitItem,false);
			  } catch (JSONException e) {
				VerigreenLogger.get().error(
						getClass().getName(),
	                    RuntimeUtils.getCurrentMethodName(),
	                    String.format("Failed creating JSON object",
	                    e));
			  } catch (IOException e) {
				VerigreenLogger.get().error(
					    getClass().getName(),
	                    RuntimeUtils.getCurrentMethodName(),
	                    String.format("Failed creating json file: " + System.getenv("VG_HOME") + "\\history.json",
	                    e));
			 }
        	  break;
          }
        }
		return localCommitItem;
	}
	
	private String getHashedPassword(String password) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
			md.update(password.getBytes());
			
			byte byteData[] = md.digest();
			
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < byteData.length; i++)
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			VerigreenLogger.get().error(
					getClass().getName(),
					RuntimeUtils.getCurrentMethodName(), "Problem in password");
			return null;
		}
		
	}
}
