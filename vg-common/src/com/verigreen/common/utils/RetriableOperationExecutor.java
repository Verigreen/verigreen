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
package com.verigreen.common.utils;

import java.beans.ConstructorProperties;

public class RetriableOperationExecutor {
	
    
	public static int DEFAULT_COUNT;
	private static int INITIAL_SLEEP_MILLIS;
    private static  int MAX_SLEEP_TIME;

   @ConstructorProperties({"Default_Count","Initial_Sleep_Millis","Max_Sleep_Time"})
	private RetriableOperationExecutor(int Default_Count,int Initial_Sleep_Millis,int Max_Sleep_Time) {
		DEFAULT_COUNT=Default_Count;
		INITIAL_SLEEP_MILLIS=Initial_Sleep_Millis;
		MAX_SLEEP_TIME=Max_Sleep_Time;
	}

	private RetriableOperationExecutor() {}
    
    public static <TResult> TResult execute(RetriableOperation<TResult> retriableOperation) {
        
        return execute(retriableOperation, INITIAL_SLEEP_MILLIS, MAX_SLEEP_TIME, DEFAULT_COUNT);
    }
    
    public static <TResult> TResult execute(
            RetriableOperation<TResult> retriableOperation,
            Class<? extends Throwable> tRetriableException) {
        
        return execute(
                retriableOperation,
                INITIAL_SLEEP_MILLIS,
                MAX_SLEEP_TIME,
                DEFAULT_COUNT,
                tRetriableException);
    }
    
    public static <T> T execute(
            RetriableOperation<T> retriableOperation,
            int initialSleepMillis,
            int maxSleepTime,
            int count) {
        
        return execute(
                retriableOperation,
                initialSleepMillis,
                maxSleepTime,
                count,
                Throwable.class);
    }
    
    
    public static <T> T execute(
            RetriableOperation<T> retriableOperation,
            int initialSleepMillis,
            int maxSleepTime,
            int count,
            Class<? extends Throwable> tRetriableException) {
        
        int retries = 0;
        while (true) {
            try {
                return retriableOperation.execute();
            } catch (Throwable thrown) {
                if (thrown.getClass().equals(tRetriableException)) {
                    isInterrupted();
                    retries = shouldRetry(count, retries, thrown);
                    backoff(initialSleepMillis, retries);
                } else {
                    throw new RuntimeException(thrown);
                }
            }
        }
    }
    
    private static void isInterrupted() {
        
        if (Thread.currentThread().isInterrupted()) {
            throw new RuntimeException(String.format(
                    "Thread id: %d interrupted",
                    Thread.currentThread().getId()));
        }
    }
    
    private static int shouldRetry(int count, int retries, Throwable thrown) {
        
        if (++retries >= count) {
            throw new RuntimeException(String.format("Operation failed after %d retries", count), thrown);
        }
        
        return retries;
    }
    
    private static void backoff(int sleepMillis, int retries) {
        
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public interface RetriableOperation<T> {
        
        T execute();
    }
}
