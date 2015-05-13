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
package com.verigreen.common.spring;

import org.springframework.context.ApplicationContext;

public class SpringContextHolder {
    
    private static final SpringContextHolder _instance = new SpringContextHolder();
    private ApplicationContext _appContext = null;
    
    protected SpringContextHolder() {}
    
    public static SpringContextHolder getInstance() {
        
        return _instance;
    }
    
    // used by the loader to set the singleton value
    public void setApplicationContext(ApplicationContext context) {
        
        _appContext = context;
    }
    
    public Object getBean(String name) {
        
        return _appContext.getBean(name);
    }
    
    public <T> T getBean(String name, Class<T> tClass) {
        
        return _appContext.getBean(name, tClass);
    }
    
    /**
     * Retrieves bean by specified class If there are more that one of no bean implementing this
     * interface registered in context org.springframework.beans.BeansException will be raised.
     * 
     * @param tClass
     *            - interface class
     * @param <T>
     *            - class type that will be returned
     * @return found bean instance
     */
    public <T> T getBean(Class<T> tClass) {
        
        return _appContext.getBean(tClass);
    }
    
    public Object getBean(String name, Object... constructorArgs) {
        
        return _appContext.getBean(name, constructorArgs);
    }
    
    public String[] getBeanNamesForType(Class<?> beanType) {
        
        return _appContext.getBeanNamesForType(beanType);
    }
    
    public ApplicationContext getApplicationContext() {
        
        return _appContext;
    }
    
    public boolean containsBean(String name) {
        
        return _appContext.containsBean(name);
    }
}
