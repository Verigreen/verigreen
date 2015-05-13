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
package com.verigreen.common.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceFactory {
    
    private static ExecutorService _executor = new GracefulThreadPoolExecutor(
            0,
            Integer.MAX_VALUE,
            60L,
            TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());
    
    private ExecutorServiceFactory() {}
    
    public static void setCachedThreadPoolExecutor(ExecutorService executor) {
        
        _executor = executor;
    }

    public static ExecutorService getCachedThreadPoolExecutor() {

        return _executor;
    }
    
    public static void fireAndForget(Runnable runnable) {
        
        getCachedThreadPoolExecutor().submit(runnable);
    }
}
