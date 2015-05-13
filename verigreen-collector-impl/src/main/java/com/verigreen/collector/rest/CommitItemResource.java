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
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.verigreen.collector.api.CommitItemPresentation;
import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.concurrency.RuntimeUtils;
import com.verigreen.common.jbosscache.Criteria;
import com.verigreen.common.utils.CollectionUtils;

@Path("/commit-items")
public class CommitItemResource {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @QueryParam("protectedBranch") final String protectedBranch,
            @QueryParam("branchId") final String branchId,
            @QueryParam("commitId") final String commitId) {
        
        Response ret = null;
        if ((protectedBranch == null) && (branchId == null)) {
            ret = getAllCommits();
        } else {
            ret = getCommit(protectedBranch, branchId, commitId);
        }
        
        return ret;
    }
    
    private Response getCommit(
            final String protectedBranch,
            final String branchId,
            final String commitId) {
        
        Status status = Status.NOT_FOUND;
        List<CommitItem> commitItems =
                CollectorApi.getCommitItemContainer().findByCriteria(new Criteria<CommitItem>() {
                    
                    @Override
                    public boolean match(CommitItem entity) {
                        
                        return (entity.getBranchDescriptor().getNewBranch().equals(branchId) && entity.getBranchDescriptor().getProtectedBranch().equals(
                                protectedBranch))
                               || entity.getChildCommit().equals(commitId);
                    }
                });
        CommitItem item = null;
        if (!CollectionUtils.isNullOrEmpty(commitItems)) {
            status = Status.OK;
            item = commitItems.get(0);
        }
        VerigreenLogger.get().log(
                getClass().getName(),
                RuntimeUtils.getCurrentMethodName(),
                String.format(
                        "CommitItem requested.\n\tProtected branch: %s.\n\tBranch ID: %s\n\tcommit ID: %s\n\tStatus: %s",
                        protectedBranch,
                        branchId,
                        commitId,
                        status.name().toString()));
        
        return Response.status(status).entity(toCommitItemPresentation(item)).build();
    }
    
    private Response getAllCommits() {
        
        Response ret;
        List<CommitItem> items = CollectorApi.getCommitItemContainer().getAll();
        List<CommitItemPresentation> itemsPresentation = new ArrayList<>();
        for (CommitItem currItem : items) {
            itemsPresentation.add(toCommitItemPresentation(currItem));
        }
        ret = Response.status(Status.OK).entity(itemsPresentation).build();
        
        return ret;
    }
    
    private CommitItemPresentation toCommitItemPresentation(CommitItem item) {
        
        CommitItemPresentation ret = null;
        if (item != null) {
            ret = new CommitItemPresentation(item.getBranchDescriptor());
            ret.setBuildUrl(item.getBuildUrl());
            ret.setCreationTime(item.getCreationTime());
            ret.setRunTime(item.getRunTime());
            ret.setEndTime(item.getEndTime());
            ret.setStatus(item.getStatus());
            ret.setParentBranch(getParent(item));
        }
        
        return ret;
    }
    
    private String getParent(CommitItem item) {
        
        CommitItem parent = item.getParent();
        
        return parent != null
                ? parent.getBranchDescriptor().getNewBranch()
                : item.getBranchDescriptor().getProtectedBranch();
    }
}
