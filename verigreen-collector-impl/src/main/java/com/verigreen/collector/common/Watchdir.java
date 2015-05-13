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

import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Properties;

import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.common.concurrency.RuntimeUtils;

public class Watchdir implements Runnable{

	String path = System.getenv("VG_HOME");
	public Path myDir = Paths.get(path);
	
	@Override
	public void run() {
		FileReader reader = null;
		Properties properties = new Properties();
		
		for(;;){
			 try
	           {
				    WatchService watcher = myDir.getFileSystem().newWatchService();
					myDir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
					WatchKey watckKey = watcher.take();
					List<WatchEvent<?>> events = watckKey.pollEvents();
					for (WatchEvent<?> event : events) {
						if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
							VerigreenLogger.get().log(
				                       getClass().getName(),
				                       RuntimeUtils.getCurrentMethodName(),
				                       "Modify: " + event.context().toString());
							reader = new FileReader(path + "/config.properties");
							properties.load(reader);
						
							String protectedBranches=properties.getProperty("git.protectedBranches");
							String permittedUsers=properties.getProperty("git.permittedUsers");
							String jobName=properties.getProperty("jenkins.jobName");
							String fullPush=properties.getProperty("full.push");
							
							//If the value is null then add it from the prop file to the hash map. This is important in case reading the value when a first push happened after changing the job name 
							if((VerigreenNeededLogic.VerigreenMap.get("_jobName") == null) || (!VerigreenNeededLogic.VerigreenMap.get("_jobName").equals(jobName))){
								VerigreenNeededLogic.VerigreenMap.put("_jobName", jobName);
							}
							
							if(!VerigreenNeededLogic.VerigreenMap.get("_protectedBranches").equals(protectedBranches)){
								VerigreenNeededLogic.VerigreenMap.put("_protectedBranches", protectedBranches);
							}
							
							if(!VerigreenNeededLogic.VerigreenMap.get("_permittedUsers").equals(permittedUsers)){
								VerigreenNeededLogic.VerigreenMap.put("_permittedUsers", permittedUsers);
							}
							
							if(!VerigreenNeededLogic.VerigreenMap.get("_fullPush").equals(fullPush)){
								VerigreenNeededLogic.VerigreenMap.put("_fullPush", fullPush);
							}
						}
					}					
	           }
	           catch( Exception e)
	           {
	               e.printStackTrace ( );
	           } 
		}
		
	}
}
