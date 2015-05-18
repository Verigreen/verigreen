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

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;

import javax.mail.MessagingException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.collector.model.CollectorName;
import com.verigreen.common.concurrency.RuntimeUtils;
import com.verigreen.common.utils.EmailUtils;

public class EmailSender {
   
    private final String _mailServer;
    private static String _collectorAddress;

	protected EmailSender(String mailServer) {
        
        _mailServer = mailServer;
    }

	public void setcollectorAddress(String collectorAddress) {
		_collectorAddress = collectorAddress;
	}
	
	public String getCommitMessage(String commitId){
    	String commitMessage = null;
		try {
			FileRepositoryBuilder builder = new FileRepositoryBuilder();
			String repoPath = VerigreenNeededLogic.properties.getProperty("git.repositoryLocation");
			Repository repo = builder.setGitDir(new File(repoPath)).setMustExist(true).build();
			Git git = new Git(repo);
			Iterable<RevCommit> log = git.log().call();
			Iterator<RevCommit> iterator = log.iterator();
			while(commitMessage == null && iterator.hasNext()){
				RevCommit rev = iterator.next();
				String commit = rev.getName().substring(0,7);
				if(commit.equals(commitId)){
		    	  commitMessage = rev.getFullMessage();
				}
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		return commitMessage;
    }
    public void notifyCommiter(    
    		String commitId,
            VerificationStatus result,
            URI jobUrl,
            String subject,
            String signature,
            String committerEmail,
            String protectedBranch,
            String tempBranch)
    {
    	notifyCommiter(
                commitId,
                result,
                jobUrl,
                subject,
                signature,
                committerEmail,
                protectedBranch,
                null,
                tempBranch); 
    }
    public void notifyCommiter(
            String commitId,
            VerificationStatus result,
            URI jobUrl,
            String subject,
            String signature,
            String committerEmail,
            String protectedBranch,
            String parentCommitId,
            String tempBranch) {
        
        StringBuilder status =
                new StringBuilder();
        if(subject.equals("Verigreen Status - Failure"))
        {
        	status.append(String.format("<table><tr><td width=\"160\">Verification status:</td><td><font color=\"red\">%s</font></td></tr>", result));
        		
        } else if(subject.equals("Verigreen Status - Success"))
        		{
        			status.append(String.format("<table><tr><td width=\"160\">Verification status:</td><td><font color=\"green\">%s</font></td></tr>", result));
        		}
        		else 
        		{
        			status.append(String.format("<table><tr><td width=\"160\">Verification status:</td><td><font color=\"FF6600\">%s</font></td></tr>", result));
        		}
        if (jobUrl != null && subject.equals("Verigreen Status - Failure")) {
            status.append(String.format(
                    "<tr><td><b>Job URL</b></td><td><a href=\"%s\">%s</a></td></tr>",
                    jobUrl,
                    jobUrl));
        }
        status.append(String.format("<tr><td>Repository name:</td><td>%s</td></tr>", CollectorName._collector));
        status.append(String.format("<tr><td>Protected branch:</td><td>%s</td></tr>", protectedBranch.substring(11)));
        if(result.equals(VerificationStatus.MERGE_FAILED))
        {
            status.append(String.format("<tr><td>Merge failed between commits:<td> <b>%s</b> and <b>%s</b></td></td></tr>",parentCommitId.substring(0, 7),commitId.substring(0, 7)));
        }
        else
        {
        status.append(String.format("<tr><td>Commit Id:</td><td>%s</td></tr>", commitId.substring(0, 7)));
        }
        if(getCommitMessage(commitId.substring(0,7)) != null)
        {
        	status.append(String.format("<tr><td>Commit message:</td><td>%s</td></tr>", getCommitMessage(commitId.substring(0,7))));
        }
        if (jobUrl != null && subject.equals("Verigreen Status - Failure") == false) {
            status.append(String.format(
                    "<tr><td>Job URL</td><td><a href=\"%s\">%s</a></td></tr>",
                    jobUrl,
                    jobUrl));
        }
        if(subject.equals("Verigreen Status - Failure"))
        {
        	status.append(String.format("<tr><td>Temporary branch:</td><td>%s</td></tr></table>", tempBranch));
        } else {
       	 status.append(String.format(
                 "</table>"));
        }
        
        send(subject, status.toString(), new String[] { committerEmail }, signature);
    }
    
    public static String getFailedSignature() {
        
        return "<br><br>"+String.format("<table><tr><td>Verigreen service</td><td>(Check Verigreen service information <a href=\"%s\">Here</a>)</td></tr></table>", _collectorAddress.replace("/rest",""))+"<br><font color=\"blue\"><b>Regards,<br><br>Verigreen.<br></b></font></br><img src=\""+_collectorAddress.replace("/rest","/Facepalm-100.png")+"\" width=\"100\" height=\"100\">";
    }
    
    public static String getSuccessSignature() {
        
        return "<br><br>"+String.format("<table><tr><td>Verigreen service</td><td>(Check Verigreen service information <a href=\"%s\">Here</a>)</td></tr></table>", _collectorAddress.replace("/rest",""))+"<br><font color=\"blue\"><b>Regards,<br><br>Verigreen.<br></b></font></br><img src=\""+_collectorAddress.replace("/rest","/Good Quality Filled-100.png")+"\" width=\"100\" height=\"100\">";
    }
    
    public static String getFailedPushSignature() {
        
        return "<br><br>"+String.format("<table><tr><td>verigreen service</td><td>(Check Verigreen service information <a href=\"%s\">Here</a>)</td></tr></table>", _collectorAddress.replace("/rest",""))+"<br><font color=\"blue\"><b>Regards,<br><br>Verigreen.<br></b></font></br><img src=\""+_collectorAddress.replace("/rest","/So-So-100.png")+"\" width=\"100\" height=\"100\">";
    }
    
    protected void send(String subject, String messageText, String[] recipients, String signature) {
        
        try {
            EmailUtils.send(
                    subject,
                    messageText,
                    recipients,
                    "verigreen@github.com",
                    _mailServer,
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
}
