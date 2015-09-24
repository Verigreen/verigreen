package com.verigreen.collector.buildverification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.offbytwo.jenkins.model.BuildResult;
import com.verigreen.collector.api.VerificationStatus;
import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.collector.model.CommitItem;
import com.verigreen.collector.model.MinJenkinsJob;
import com.verigreen.collector.observer.Observer;
import com.verigreen.collector.observer.Subject;
import com.verigreen.collector.spring.CollectorApi;
import com.verigreen.common.concurrency.RuntimeUtils;

public class JenkinsUpdater implements Subject {

	private ArrayList<Observer> observers = new ArrayList<>();
	private static final Map<String,VerificationStatus> _verificationStatusesMap;
	private static volatile JenkinsUpdater _instance; 
    // Map to make the correlation between the build results from Jenkins and the VerificationStatus class
	static
    {
    	_verificationStatusesMap = new HashMap<String, VerificationStatus>();
    	_verificationStatusesMap.put(BuildResult.SUCCESS.toString(), VerificationStatus.PASSED);
    	_verificationStatusesMap.put(BuildResult.ABORTED.toString(), VerificationStatus.FAILED);
    	_verificationStatusesMap.put(BuildResult.FAILURE.toString(), VerificationStatus.FAILED);
    	_verificationStatusesMap.put("null", VerificationStatus.RUNNING);
    }
    
    
    
    public static JenkinsUpdater getInstance()
    { 
    	if(_instance == null)
    	{
    		synchronized(JenkinsUpdater.class)
    		{ 
    			if(_instance == null)
    			{ 
    				_instance = new JenkinsUpdater();  
    			}
    		}
    	}
    return _instance; 
    }

	public List<Observer> getObservers(){
		return this.observers;
	}

	@Override
	public void register(Observer o) 
	{
		observers.add((CommitItem) o);
		VerigreenLogger.get().log(
             getClass().getName(),
             RuntimeUtils.getCurrentMethodName(),
             String.format(
                     "Observer registered: %s",
                     o.toString()));
	}
	
	@Override
	public void unregister(Observer o) 
	{
		observers.remove(o);
		VerigreenLogger.get().log(
	             getClass().getName(),
	             RuntimeUtils.getCurrentMethodName(),
	             String.format(
	                     "Observer removed: %s",
	                     o.toString()));
	}

	public List<Observer> setObserversStatus(List<Observer> relevantObservers, Map <String, MinJenkinsJob> results)
	{
		//sets the observer status based on the result recieved from Jenkins
		List<Observer> notifiedObservers = new ArrayList<Observer>();
		MinJenkinsJob result;
		for(Observer observer : relevantObservers)
		{	
			result = results.get(((CommitItem)observer).getMergedBranchName());
			observer.update(_verificationStatusesMap.get(result.getJenkinsResult()));
			notifiedObservers.add(observer);			
		}
		return notifiedObservers;
	}
	
	@Override
	public void notifyObserver(List<Observer> relevantObservers) 
	{
	//the relevant observers are notified, unregistered and saved to the commit item container	
		List<CommitItem> notifiedObservers = new ArrayList<CommitItem>();
		for(Observer observer : relevantObservers){
			if(!com.verigreen.collector.spring.CollectorApi.getCommitItemContainer().get(((CommitItem)observer).getKey()).getStatus().isFinalState()) {
				notifiedObservers.add((CommitItem)observer);
				unregister(observer);
				VerigreenLogger.get().log(
			             getClass().getName(),
			             RuntimeUtils.getCurrentMethodName(),
			             String.format(
			                     "Successfully updated and saved observer: %s",
			                     observer.toString()));
			}
			else 
			{
				unregister(observer);
			}
		}	
		CollectorApi.getCommitItemContainer().save(notifiedObservers);
	}
	
}
	
