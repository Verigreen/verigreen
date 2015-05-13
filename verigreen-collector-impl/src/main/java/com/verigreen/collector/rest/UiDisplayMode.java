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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.JsonObject;
import com.verigreen.collector.common.CollectorVersion;
import com.verigreen.collector.common.VerigreenNeededLogic;
import com.verigreen.collector.model.CollectorName;

	@Path("/uidisplaymode")
	public class UiDisplayMode {
		@GET
	    @Produces(MediaType.APPLICATION_JSON)
	    public Response get(){
			CollectorVersion collectorVersion = new CollectorVersion();
			CollectorName collectorName = new CollectorName();
			String mode = VerigreenNeededLogic.VerigreenMap.get("_fullPush");
			JsonObject obj = new JsonObject();
			obj.addProperty("Name", collectorName.get_collector());
			obj.addProperty("Version", collectorVersion.get_collectorVersion());
			obj.addProperty("Mode", mode);
			String objSend = obj.toString();
		return Response.status(Status.OK).entity(objSend).build();
		}
}
