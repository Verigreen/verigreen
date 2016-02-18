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
package com.verigreen.collector.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.api.VerigreenNeeded;
import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.concurrency.RuntimeUtils;
import com.verigreen.common.jbosscache.Criteria;
import com.verigreen.common.utils.CollectionUtils;
import com.verigreen.common.utils.EmailUtils;
import com.verigreen.jgit.JGitOperator;
import com.verigreen.jgit.SourceControlOperator;

public class VerigreenNeededLogic {
	public static Properties properties = new Properties();
	public static Map<String,String> VerigreenMap = new HashMap<String,String>();
	public static Map<String,String> jenkinsParams = new HashMap<String,String>();
	public static Map<String,List<JSONObject>> history = new HashMap<String,List<JSONObject>>();
	private String vgHomePath = System.getenv("VG_HOME");
	private String historyJsonPath = vgHomePath + "//history.json";

	
	private VerigreenNeededLogic(String protectedBranches, String permittedUsers, String hashedPassword, String fullPush
			) throws IOException{
		
			VerigreenMap.put("_protectedBranches", protectedBranches);
			VerigreenMap.put("_permittedUsers", permittedUsers);
			VerigreenMap.put("_hashedPassword", hashedPassword);
			VerigreenMap.put("_fullPush", fullPush);
			getHistory();
			setJenkinsProperties();
//			setGitRepositoryPath();
			
		try{
	    	  checkGitRepo();
			}catch (Throwable thrown){
	    	  VerigreenLogger.get().error(
	                    getClass().getName(),
	                    RuntimeUtils.getCurrentMethodName(),
	                    String.format("Repository not found: %s", properties.getProperty("git.repositoryLocation")),
	                    thrown);
	    	  String lineSeparator = System.lineSeparator();
	    	  sendEmailNotification("Attention: Repository not found", "Repository not found: <b>"+properties.getProperty("git.repositoryLocation")+"</b>. "+thrown.getMessage()+"."+ lineSeparator +
	    			    "Collector name: <b>" + properties.getProperty("collectors") + "</b>." + lineSeparator + 
	                    "Collector location: " + properties.getProperty("collector.address") + "." + lineSeparator +
	                    "We suggest you clone the repository from your Git provider indicated by your <b>" + vgHomePath + "</b> file in the git.repositoryLocation property." + lineSeparator
	                    , new String[] { properties.getProperty("email.address") }, getSignature());
			}

			Thread t = new Thread(new Watchdir());
			t.start();
	}
	
	@SuppressWarnings("unchecked")
	private void getHistory() throws IOException {
		StringBuilder  stringBuilder = null;
		JSONArray keyValues;
		List<JSONObject> values;
		if (!new File(historyJsonPath).exists()){
			new File(historyJsonPath);
		}
		else {
			stringBuilder = new StringBuilder();
	  		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(historyJsonPath), StandardCharsets.UTF_8));
	  		String line;
	  		while ((line = br.readLine()) != null) 
	  		{
	  		   stringBuilder.append(line);
	  		}
	  		br.close();
	  		if(stringBuilder.length() != 0){
	  			line = stringBuilder.toString();
		  		try {
		            JSONObject jObject = new JSONObject(line); //Parse the JSON to a JSONObject
		            Iterator<String> keys = jObject.keys();
		            String key;
		            while( keys.hasNext() ){
		                key = (String)keys.next();
		                keyValues = jObject.getJSONArray(key);
		                values = new ArrayList<JSONObject>();
		                for(int i = 0; i < keyValues.length(); i++)
		                {
			                values.add(keyValues.getJSONObject(i));
		                }
		                history.put(key, values);
		            }			

		        } catch (JSONException e) {
		        	VerigreenLogger.get().error(
		                    getClass().getName(),
		                    RuntimeUtils.getCurrentMethodName(),
		                    String.format("Failed to parse into json object",
		                    e));
		        }
	  		}
		}
	}
    public void setGitRepositoryPath()
    {
		String repoPath = properties.getProperty("git.repositoryLocation").concat("\\.git");
		properties.setProperty("git.repositoryLocation", repoPath);
    }
	private void checkGitRepo() {
		String repositoryPath = properties.getProperty("git.repositoryLocation");
		SourceControlOperator srcControl = new JGitOperator(repositoryPath);
		srcControl.fetch();
	}
	
	public void sendEmailNotification(String subject, String messageText, String[] recipients, String signature) {
		
		try {
            EmailUtils.send(
                    subject,
                    messageText,
                    recipients,
                    "verigreen@github.com",
                    properties.getProperty("mail.server"),
                    signature);
        } catch (MessagingException ex) {
            VerigreenLogger.get().error(
                    getClass().getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    String.format(
                            "Failed sending email message [%s] to the following recipients: %s",
                            messageText,
                            Arrays.toString(recipients)),
                    ex);
        }
	}
	
	public static String getSignature() 
	{    
		return "<br><br><b>Have a <font color=\"green\">Verigreen</font> Day!</b></br><img src=\""+properties.getProperty("collector.address").replace("/rest","/VeriGreenLogo_Wide.png")+"\" width=\"200\" height=\"75\"></br></br>";
    }		     

	public void setJenkinsProperties() throws IOException
	{
		properties.load(new FileInputStream(vgHomePath + "//config.properties"));
		
		for(String key : properties.stringPropertyNames()) 
		{
			  String value = properties.getProperty(key);
			  if(key.startsWith("jenkinsparam"))
			  {	
				  if(!value.equals("false"))
				  {
					  jenkinsParams.put(key, value);
				  }
			  }
		}
	 }
	
	/**
	 * This method is for system tests needs only Note: this is not made for concurrent operations
	 * on GIT. Meaning, setting branches
	 * 
	 * @param protectedBranches
	 */
	public void setProtectedBranches(List<String> protectedBranches) {
		String protectedBranch =  VerigreenMap.get("_protectedBranches");
		String localBranchesRoot = CollectorApi.getSourceControlOperator().getLocalBranchesRoot();

		for(String branch : protectedBranches)
		{
			protectedBranch+=","+localBranchesRoot+branch;
		}
		VerigreenMap.put("_protectedBranches", protectedBranch);
	}
	public void setPermittedUsers(List<String> permittedUsers) {
		String permittedUsersMap =  VerigreenMap.get("_permittedUsers");
		for(String user : permittedUsers)
		{
			permittedUsersMap+=","+user;
		}
		VerigreenMap.put("_permittedUsers", permittedUsersMap);
	}

	public VerigreenNeeded isVerigreenNeeded(
			final String parentBranchName,
			final String branchNameToVerify,
			final String commitId,
			final String committer) {
		String reason = "";
		boolean ans;
		boolean shouldRejectCommit = true;
		ans= (VerigreenMap.get("_protectedBranches")).contains(parentBranchName) && !(VerigreenMap.get("_permittedUsers")).contains(committer);
		if (ans) {
			CommitItem item =
					checkIfCommitItemExists(parentBranchName, branchNameToVerify, commitId);
			List<String> passCommit = VerigreenMap.get("_passCommit") != null? new LinkedList<String>(Arrays.asList(VerigreenMap.get("_passCommit").split("\\s*,\\s*"))):null;
			
			if (item != null){
				// commit item already exists -> don't do verigreen
				ans = false;
				VerificationStatus vs = item.getStatus();
            	if (vs.equals(VerificationStatus.PASSED) || vs.equals(VerificationStatus.FORCING_PUSH) || vs.equals(VerificationStatus.PASSED_BY_CHILD)){
            		shouldRejectCommit = false;
            	}
            	else if (vs.equals(VerificationStatus.GIT_FAILURE) || 
            		vs.equals(VerificationStatus.TRIGGER_FAILED) || 
            		vs.equals(VerificationStatus.TIMEOUT)){
            		ans = true;
            		shouldRejectCommit = false;
            	}
            	else if(VerigreenMap.get("_fullPush").equals("enabled") && vs.equals(VerificationStatus.FAILED))
            	{
            		ans = true;
            		shouldRejectCommit = false;
            	}
				// if the commit item isn't PASSED yet than it should be rejected otherwise it should be pushed
				reason =
						String.format(
								"branch [%s] already exists and its status is [%s]",
								branchNameToVerify,
								vs.toString());
			}
			else if (passCommit != null){
				String commitToBeDeleted = "";
				for (String commit : passCommit) {
					if (!commit.equals("") && branchNameToVerify.contains(commit)){
						commitToBeDeleted = commit;
						ans = false;
						shouldRejectCommit = false;
						reason =
								String.format(
										"Verigreen is not needed, Commit [%s] was created due to push by permitted user",
										commit);
						break;
					}
				}
				if (passCommit.contains(commitToBeDeleted)){
					passCommit.remove(commitToBeDeleted);
					String passCommitString = VerigreenNeededLogic.VerigreenMap.get("_passCommit");
					passCommitString = passCommitString.replace(commitToBeDeleted, "");
					passCommitString = passCommitString.replace(",,", ",");
					if (passCommitString.startsWith(",")){
						passCommitString = passCommitString.replace(",", "");
					}
					VerigreenMap.put("_passCommit", passCommitString);
				}
			}
		} else {
			// verigreen is not needed. Either branch is not protected or it is a permitted user
			shouldRejectCommit = false;
			reason =
					(VerigreenMap.get("_permittedUsers")).contains(committer)
					? String.format("[%s] is a permitted user", committer)
							: String.format(
									"[%s] isn't protected branch and therefore verigreen won't be executed",
									parentBranchName);
		}

		return new VerigreenNeeded(ans, shouldRejectCommit, reason);
	}

	private static CommitItem checkIfCommitItemExists(
			final String parentBranchName,
			final String branchNameToVerify,
			final String commitId) {

		List<CommitItem> commitItems =
				CollectorApi.getCommitItemContainer().findByCriteria(new Criteria<CommitItem>() {
					
					@Override
					public boolean match(CommitItem entity) {

						return ((entity.getBranchDescriptor().getNewBranch().equals(
								branchNameToVerify) || (entity.getChildCommit().equals(commitId))) && entity.getBranchDescriptor().getProtectedBranch().equals(
										parentBranchName));
					}
				});

		CommitItem item = null;
		if (!CollectionUtils.isNullOrEmpty(commitItems)) {
			item = commitItems.get(0);
		}

		return item;
	}

	/**
	 * This method returns Json Object or fields params in hash map based on Jenkins Mode
	 * 
	 * @param commitItem
	 * @return
	 * @throws JSONException
	 */
	public static Map<String, String> checkJenkinsMode(CommitItem commitItem) {	
		
		Map<String, String> resultMap =  new HashMap<String,String>();
		Map<String, String> paramMap = commitItemAsParameterMap(commitItem);

		if(jenkinsParams.get("jenkinsparam.mode") != null && jenkinsParams.get("jenkinsparam.mode").equals("json")){
			JsonObject json = new JsonObject();
			for(String key : paramMap.keySet()){
				json.addProperty(key, paramMap.get(key));
			}
			resultMap.put("json",json.toString());
		}
		else {
			return paramMap;
		}
		return resultMap;
	}
	
	/**
	 * This method return all relevant fields from CommitItem to an HashMap
	 * @param commitItem
	 * @return
	 * @throws JSONException
	 */
	public static Map<String, String> commitItemAsParameterMap(CommitItem commitItem) {	
		
		Map<String, String> returnedForJenkins = new HashMap<String,String>();
		for(String key : jenkinsParams.keySet())
		{
			if(key.contains("commitid")){
				if (jenkinsParams.get(key).equals("true")){
					returnedForJenkins.put("commitid",commitItem.getBranchDescriptor().getNewBranch());
				}
				else if (!jenkinsParams.get(key).equals("false")){
					returnedForJenkins.put(jenkinsParams.get(key), commitItem.getBranchDescriptor().getNewBranch());
				}
			}
			else if(key.contains("committer")){
				if (jenkinsParams.get(key).equals("true")){
					returnedForJenkins.put("committer",commitItem.getBranchDescriptor().getCommitter());
				}
				else if (!jenkinsParams.get(key).equals("false")){
					returnedForJenkins.put(jenkinsParams.get(key), commitItem.getBranchDescriptor().getCommitter());
				}
			}
			else if(key.contains("longid")){
				if (jenkinsParams.get(key).equals("true")){
					returnedForJenkins.put("longid",commitItem.getBranchDescriptor().getCommitId());
				}
				else if (!jenkinsParams.get(key).equals("false")){
					returnedForJenkins.put(jenkinsParams.get(key),commitItem.getBranchDescriptor().getCommitId());
				}
			}
			else if(key.contains("jenkinsparam.protected")){
				if(jenkinsParams.get(key).equals("true")){
					returnedForJenkins.put("protected",commitItem.getBranchDescriptor().getProtectedBranch());
				}
				else if (!jenkinsParams.get(key).equals("false")){
					returnedForJenkins.put(jenkinsParams.get(key), commitItem.getBranchDescriptor().getProtectedBranch());
				}
			}
			else if(key.contains("parent")){
				String paraent = commitItem.getParent() == null?commitItem.getBranchDescriptor().getProtectedBranch():commitItem.getParent().getBranchDescriptor().getNewBranch();
				if(jenkinsParams.get(key).equals("true")){
					returnedForJenkins.put("parent",paraent);
				}
				else if (!jenkinsParams.get(key).equals("false")){
					returnedForJenkins.put(jenkinsParams.get(key), paraent);
				}
			}
		}
		return returnedForJenkins;
	}

}