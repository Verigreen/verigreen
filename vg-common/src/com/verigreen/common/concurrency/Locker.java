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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.verigreen.common.exception.LabException;

public class Locker {
    
    private static Locker _instance = new Locker();
    private final ConcurrentMap<Integer, LockerData> _data =
            new ConcurrentHashMap<Integer, LockerData>();
    
    private Locker() {}
    
    public static Locker getInstance() {
        
        return _instance;
    }
    
    public void reset() {
        
        _data.clear();
    }
    
    public void tryLock(int key, long timeout) {
        
        tryLock(key, timeout, 1);
    }
    
    /**
     * Locks the calling thread until someone will release it, or timeout will occur. On release -
     * the given recipient will be notified.
     */
    public void tryLock(int key, long timeout, int lockCount) {
        
        try {
            LockerData data = getData(key, lockCount);
            doLock(data.getLock(), timeout);
            checkException(data);
        } finally {
            _data.remove(key);
        }
    }
    
    public void release(int key) {
        
        release(key, null);
    }
    
    /**
     * Releases the lock on the waiting thread(s) for the given reservation id, notifies them about
     * the given exception.
     */
    public synchronized void release(int key, Throwable ex) {
        
        LockerData data = getData(key, 1);
        //set released only if it is the last thread that needs to release
        if (data.getLock().getCount() == 1) {
            data.setAlreadyReleased(true);
        }
        if (ex != null) {
            data.setException(ex);
        }
        notify(data.getLock());
    }
    
    private synchronized LockerData getData(int key, int lockCount) {
        
        LockerData data = _data.get(key);
        if (data == null) {
            data = new LockerData(lockCount);
            _data.put(key, data);
        }
        
        return data;
    }
    
    private void checkException(LockerData data) {
        
        if (!isAlreadyReleased(data)) {
            throw new LabException(new TimeoutException());
        }
        Throwable thrown = data.getException();
        if (thrown != null) {
            throw new LabException(thrown);
        }
    }
    
    private void doLock(CountDownLatch lock, long timeout) {
        
        try {
            lock.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            throw new LabException(ex);
        }
    }
    
    private synchronized boolean isAlreadyReleased(LockerData lockerData) {
        
        return lockerData.isAlreadyReleased();
    }
    
    private void notify(CountDownLatch lock) {
        
        lock.countDown();
    }
    
    private static class LockerData {
        
        private final CountDownLatch _latch;
        private boolean _isAlreadyReleased = false;
        private Throwable _thrown;
        
        public LockerData(int lockCount) {
            
            _latch = new CountDownLatch(lockCount);
        }
        
        public CountDownLatch getLock() {
            
            return _latch;
        }
        
        public boolean isAlreadyReleased() {
            
            return _isAlreadyReleased;
        }
        
        public void setAlreadyReleased(boolean isAlreadyReleased) {
            
            _isAlreadyReleased = isAlreadyReleased;
        }
        
        public Throwable getException() {
            
            return _thrown;
        }
        
        public void setException(Throwable thrown) {
            
            _thrown = thrown;
        }
    }
}
