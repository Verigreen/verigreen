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

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.verigreen.collector.common.VerigreenNeededLogic;

@Path("/commit-message")
public class CommitMessage {

	@GET
    @Produces(MediaType.APPLICATION_JSON)
	public static Response get() {
		Map<String,String> logMessages = new HashMap<>();
		try {
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
	     Repository repo = builder.setGitDir(new File(VerigreenNeededLogic.properties.getProperty("git.repositoryLocation"))).setMustExist(true).build();
	    Git git = new Git(repo);
	    Iterable<RevCommit> log = git.log().call();
	    for (Iterator<RevCommit> iterator = log.iterator(); iterator.hasNext();) {
	      RevCommit rev = iterator.next();
	      logMessages.put(rev.getName(),rev.getFullMessage());
	    }
		} catch(Exception e){
			e.printStackTrace();
		}
		return Response.status(Status.OK).entity(logMessages).build();		
	}
	
}
