package com.verigreen.collector.jobs;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.buildverification.CommitItemVerifier;
import com.verigreen.collector.buildverification.JenkinsUpdater;
import com.verigreen.collector.buildverification.JenkinsVerifier;
import com.verigreen.collector.common.VerigreenNeededLogic;
import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.model.MinJenkinsJob;
import com.verigreen.collector.observer.Observer;
import com.verigreen.common.concurrency.RuntimeUtils;
import com.verigreen.restclient.RestClientImpl;
import com.verigreen.restclient.RestClientResponse;
import com.verigreen.spring.common.CollectorApi;

@DisallowConcurrentExecution
public class CallJenkinsJob implements Job {

	JenkinsUpdater jenkinsUpdater = JenkinsUpdater.getInstance();
	JenkinsVerifier jenkinsVerifier = new JenkinsVerifier();
	private int _maximumRetries = getNumberOfRetriesCounter();
	private int _maximumTimeout = getTriggerTimeoutCounter();
	private long _timeOutInMillies = getTimeoutInMillies();
	
	
	public CallJenkinsJob(){}
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {

		calllingJenkinsForUpdate();
		calllingJenkinsForCreate();
		calllingJenkinsForCancel();
	}

	private void calllingJenkinsForCancel() {
		// TODO Auto-generated method stub
		
	}

	private int getTriggerTimeoutCounter()
	{
		int counterTimeout = Integer.parseInt(VerigreenNeededLogic.properties.getProperty("timeout.counter"));
		return counterTimeout;

	}
	
	private int getNumberOfRetriesCounter()
	{
		int counterRetries = Integer.parseInt(VerigreenNeededLogic.properties.getProperty("default_count"));
		return counterRetries;
	}
	private long getTimeoutInMillies()
	{
		int timeoutInMillies = Integer.parseInt(VerigreenNeededLogic.properties.getProperty("jenkins.timeoutInSeconds"));
		timeoutInMillies = timeoutInMillies * 1000;
		return timeoutInMillies;
	}
	
	private void calllingJenkinsForCreate() {
		VerigreenLogger.get().log(
	             getClass().getName(),
	             RuntimeUtils.getCurrentMethodName(),
	             String.format(
	                     "There are [%s] not triggered items...",
	                     CommitItemVerifier.getInstance().getCommitItems().size() ));


		// triggers a job for each item in the commit item verifier
		for (Iterator<CommitItem> iterator = CommitItemVerifier.getInstance().getCommitItems().iterator(); iterator.hasNext();) {
			CommitItem ci = iterator.next();
			iterator.remove();
			JenkinsVerifier.triggerJob(com.verigreen.collector.spring.CollectorApi.getCommitItemContainer().get(ci.getKey()));
			
			// Remove the current element from the iterator and the list.
	        
		}
	}

	private RestClientResponse createRestCall(String param) {
		
		VerigreenLogger.get().log(
				getClass().getName(),
				RuntimeUtils.getCurrentMethodName(),
				String.format(
						"Sending REST call to Jenkins server..."));
		
		String jenkinsUrl = VerigreenNeededLogic.properties.getProperty("jenkins.url");
		String jobName = VerigreenNeededLogic.properties.getProperty("jenkins.jobName");
		
		RestClientResponse result = new RestClientImpl().get(CollectorApi.getJenkinsCallRequest(jenkinsUrl, jobName, param));
		return result;
	}

	private void calllingJenkinsForUpdate() {

		String result;
		int sizeObservers = jenkinsUpdater.getObservers().size();
		VerigreenLogger.get().log(
	             getClass().getName(),
	             RuntimeUtils.getCurrentMethodName(),
	             String.format(
	                     "Jenkins called for update on [%s] not updated items...",
	                     sizeObservers ));
		
		if (sizeObservers > 0){
			RestClientResponse response = createRestCall("api/json?depth=1&pretty=true&tree=builds[number,result,building,timestamp,actions[parameters[value]]]");
			result = response.getEntity(String.class);
			try {
				Map<String, MinJenkinsJob> parsedResults = parsingJSON(result);
				
				List<Observer> analyzedResults = analyzeResults(parsedResults);
				
				
				if(!analyzedResults.isEmpty())
				{
					jenkinsUpdater.notifyObserver(jenkinsUpdater.setObserversStatus(analyzedResults, parsedResults));
				}
			} catch (JSONException e) {
				VerigreenLogger.get().error(
			             getClass().getName(),
			             RuntimeUtils.getCurrentMethodName(),
			             "Bad JSON response: " + result);//for security reasons - remove the result from the exception.
			}
		}		
	}
	private Map<String, MinJenkinsJob> parsingJSON(String json) throws JSONException {
		
		VerigreenLogger.get().log(
	             getClass().getName(),
	             RuntimeUtils.getCurrentMethodName(),
	             String.format(
	                     "Parsing JSON results fron Jenkins..."));
		Map<String, MinJenkinsJob> buildsAndStatusesMap = new HashMap<String, MinJenkinsJob>();
		JsonParser parser = new JsonParser();
		JsonObject mainJson = (JsonObject) parser.parse(json);
		
		JsonObject parameterJsonObjectArray = null;

		JsonArray jsonBuildsArray = mainJson.getAsJsonArray("builds");
		for (int i = 0; i < jsonBuildsArray.size(); i++) 
		{
			 JsonObject childJsonObject = (JsonObject) jsonBuildsArray.get(i);
			 
			 String buildNumber = childJsonObject.get("number").getAsString();
			 
			 MinJenkinsJob values = new MinJenkinsJob();
			 
			 values.setBuildNumber(buildNumber);
			 
			 if (childJsonObject.get("result") instanceof JsonNull)
			 {
				 values.setJenkinsResult("null");
			 }
			 else
			 {
				 values.setJenkinsResult(childJsonObject.get("result").getAsString());
			 }
			 
			 JsonArray actionsJsonArray = childJsonObject.get("actions").getAsJsonArray();
			 
			 parameterJsonObjectArray = checkForParameters(actionsJsonArray);
			
			 JsonArray jsonParametersArray =  parameterJsonObjectArray.getAsJsonArray("parameters");
			 
			 JsonObject parameterJsonObject = (JsonObject) jsonParametersArray.get(0);
			 
			 values.setBranchName(parameterJsonObject.get("value").getAsString());
			 
			 buildsAndStatusesMap.put(values.getBranchName(), values);				 
		}
		return buildsAndStatusesMap;
	}
	
	private JsonObject checkForParameters(JsonArray array)
	{
		JsonObject result = null;
		for(int j = 0 ; j < array.size() ; j ++)
		 {
			 if(((JsonObject)array.get(j)).getAsJsonArray("parameters")!= null) 
			 {
				 result =  (JsonObject) array.get(j);
				 break;
			 }
		 }
		return result;
	}
	
	private void checkTriggerAndRetryMechanism(Observer observer)
	{
		/*TODO check the observer, if the observer (CommitItem) doesn't have _buildnumber 
		 * then check the parsedResults, and if there is no value calculate timeout.
		 * this method will do:
		 * 1) if both counters reaches their limits if so = trigger failed
		 * 	  if timeoutConter didn't reach the limit then:
		 *	 		++timeoutcounter
	     * 		else
		 * 			++triggercounter;
		 * 			timeoutCounter = 0;
		 * 			change the triggerAttempt to false
		 * 			save the commit item
		 * 			unregister the observer from subject (update)
		 * 			adding it (commitItem) to the commitItemVerifier list.
		*/
		
		CommitItem itemToBeChecked = com.verigreen.collector.spring.CollectorApi.getCommitItemContainer().get(((CommitItem)observer).getKey());
		int retriableCounter = itemToBeChecked.getRetriableCounter();
		int timeoutCounter = itemToBeChecked.getTimeoutCounter();

		
		if(timeoutCounter >= _maximumTimeout && retriableCounter >= _maximumRetries)
		{
			com.verigreen.collector.spring.CollectorApi.getCommitItemContainer().save(itemToBeChecked);
			itemToBeChecked.setStatus(VerificationStatus.TRIGGER_FAILED);
			jenkinsUpdater.unregister(observer);
			com.verigreen.collector.spring.CollectorApi.getCommitItemContainer().save(com.verigreen.collector.spring.CollectorApi.getCommitItemContainer().get(((CommitItem)itemToBeChecked).getKey()));

		}
		else if(timeoutCounter < _maximumTimeout)
		{

			timeoutCounter++;
			itemToBeChecked.setTimeoutCounter(timeoutCounter);
			//we update the observer too
			((CommitItem)observer).setTimeoutCounter(timeoutCounter);			
			com.verigreen.collector.spring.CollectorApi.getCommitItemContainer().save(itemToBeChecked);
		}
		else{
			//increase the retriable counter for the observer, unregister it and retrigger the job
			retriableCounter++;
			timeoutCounter = 0;
			itemToBeChecked.setTimeoutCounter(timeoutCounter);
			itemToBeChecked.setRetriableCounter(retriableCounter);
			itemToBeChecked.setTriggeredAttempt(false);
			com.verigreen.collector.spring.CollectorApi.getCommitItemContainer().save(itemToBeChecked);
			jenkinsUpdater.unregister(observer);
			CommitItemVerifier.getInstance().getCommitItems().add(com.verigreen.collector.spring.CollectorApi.getCommitItemContainer().get(((CommitItem)itemToBeChecked).getKey()));

		}
	}
	
	private boolean checkForTimeout(CommitItem ci)
	{
		return System.currentTimeMillis() - ci.getRunTime().getTime() > _timeOutInMillies;
	}
	private List<Observer> analyzeResults(Map<String, MinJenkinsJob> parsedResults){
		VerigreenLogger.get().log(
	             getClass().getName(),
	             RuntimeUtils.getCurrentMethodName(),
	             String.format(
	                     "Analyzing JSON results and returning the relevant observers..."));

		List<Observer> observers =  jenkinsUpdater.getObservers();
		List<Observer> relevantObservers = new ArrayList<Observer>();
		for(Observer observer : observers)
		{	
			observer = com.verigreen.collector.spring.CollectorApi.getCommitItemContainer().get(((CommitItem)observer).getKey());
			try {
				//the default build url for an untriggered item is 0, also check for null value in the parsed results, that means that 
				//the MinJenkinsJob didn't get any response for that particular commit item 
			
				boolean hasTimedOut = checkForTimeout((CommitItem)observer);
				if(hasTimedOut)
				{
					observer.update(VerificationStatus.TIMEOUT);
					jenkinsUpdater.unregister(observer);
					com.verigreen.collector.spring.CollectorApi.getCommitItemContainer().save((CommitItem)observer);
				}
				if(((CommitItem)observer).getBuildNumber() == 0)
				{
					if(parsedResults.get(((CommitItem)observer).getMergedBranchName()) == null)
					{//we don't have a build number and no result from Jenkins -- we need to check the retry and trigger mechanism
			
						checkTriggerAndRetryMechanism(observer);				
					}
					else
					{//we don't have a build number but the observer is running
						((CommitItem)observer).setBuildNumber(Integer.parseInt(parsedResults.get(((CommitItem)observer).getMergedBranchName()).getBuildNumber()));
						((CommitItem)observer).setBuildUrl(new URI (JenkinsVerifier.getBuildUrl(Integer.parseInt(parsedResults.get(((CommitItem)observer).getMergedBranchName()).getBuildNumber()))));
						
						com.verigreen.collector.spring.CollectorApi.getCommitItemContainer().save(((CommitItem)observer));
					}
					
				}
				
				else if(!((MinJenkinsJob)parsedResults.get(((CommitItem)observer).getMergedBranchName())).getJenkinsResult().equals("null"))
				{// if we are here we already have a build number
					relevantObservers.add(observer);
				}
			
			}
			catch (NullPointerException e){ //means that the update didn't get details of the new create.
				continue;
			} catch (NumberFormatException e) {
				VerigreenLogger.get().error(
	                    getClass().getName(),
	                    RuntimeUtils.getCurrentMethodName(),
	                    String.format(
	                            "Illegal character in build number: [%s]",
	                            Integer.parseInt(parsedResults.get(((CommitItem)observer).getMergedBranchName()).getBuildNumber())),
	                    e);
			} catch (URISyntaxException e) {
				VerigreenLogger.get().error(
	                    getClass().getName(),
	                    RuntimeUtils.getCurrentMethodName(),
	                    String.format(
	                            "Illegal character in build URL: [%s]",
	                            JenkinsVerifier.getBuildUrl(Integer.parseInt(parsedResults.get(((CommitItem)observer).getMergedBranchName()).getBuildNumber()))),
	                    e);
			}
		}
		return relevantObservers;
	}
}
