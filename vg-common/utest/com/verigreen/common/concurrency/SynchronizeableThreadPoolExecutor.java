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

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import com.verigreen.common.concurrency.GracefulThreadPoolExecutor;
import com.verigreen.common.exception.LabException;

public class SynchronizeableThreadPoolExecutor extends GracefulThreadPoolExecutor {
    
    Collection<Future<?>> _tasks = new LinkedList<>();
    
    public SynchronizeableThreadPoolExecutor() {
        
        super(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    }
    
    public void join() {
        
        for (Future<?> currTask : _tasks) {
            try {
                currTask.get();
            } catch (Throwable thrown) {
                throw new LabException(thrown);
            }
        }
    }
    
    @Override
    public Future<?> submit(Runnable task) {
        Future<?> ret = super.submit(task);
        _tasks.add(ret);
        
        return ret;
    }
    
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        
        Future<T> ret = super.submit(task);
        _tasks.add(ret);
        
        return ret;
    }
}
