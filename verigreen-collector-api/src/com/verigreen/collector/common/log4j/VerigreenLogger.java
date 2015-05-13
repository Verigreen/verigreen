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
package com.verigreen.collector.common.log4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.verigreen.common.utils.StringUtils;

public class VerigreenLogger implements com.verigreen.common.logger.Logger {
    
    private static VerigreenLogger _instance = new VerigreenLogger();
    
    private final Logger _logger = LogManager.getLogger();
    
    public static VerigreenLogger get() {
        
        return _instance;
    }
    
    public void log(String className, String methodName, String log) {
        
        add(className, methodName, log);
    }
    
    public void error(String className, String methodName, String log) {
        
        error(className, methodName, -1, log, new Throwable());
    }
    
    public void error(String className, String methodName, int contextId, String log) {
        
        error(className, methodName, contextId, log, new Throwable());
    }
    
    public void error(String className, String methodName, String log, Throwable thrown) {
        
        error(className, methodName, -1, log, thrown);
    }
    
    public void error(
            String className,
            String methodName,
            int contextId,
            String log,
            Throwable thrown) {
        
        _logger.error(format(className, methodName, log), thrown);
    }
    
    @Override
    public void debug(String log) {
        
        _logger.debug(format(log + StringUtils.NEW_LINE));
    }
    
    @Override
    public void info(String log) {
        
        _logger.info(format(log + StringUtils.NEW_LINE));
    }
    
    @Override
    public void warning(String log) {
        
        _logger.warn(format(log + StringUtils.NEW_LINE));
    }
    
    @Override
    public void error(String log) {
        
        _logger.error(format(log + StringUtils.NEW_LINE));
    }
    
    @Override
    public void error(String log, Throwable thrown) {
        
        _logger.error(format(log + StringUtils.NEW_LINE), thrown);
    }
    
    private void add(String className, String methodName, String log) {
        
        _logger.info(format(className, methodName, log + StringUtils.NEW_LINE));
    }
    
    private String format(String className, String methodName, String log) {
        
        return String.format(
                "*** %s::%s (tid: %s) *** %s *** END ***",
                className,
                methodName,
                Thread.currentThread().getId(),
                log);
    }
    
    private String format(String log) {
        
        return String.format(
                "*** (tid: %s) *** %s *** END ***",
                Thread.currentThread().getId(),
                log);
    }
}
