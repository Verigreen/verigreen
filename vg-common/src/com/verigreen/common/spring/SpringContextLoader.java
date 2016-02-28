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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.support.AbstractContextLoader;

public class SpringContextLoader extends AbstractContextLoader {
    
    public static final String CONFIG_FILE_NAME = "*-context.xml";
    public static final String CONFIG_PATH_PREFIX = "classpath*:/Spring/";
    public static final String CONFIG_LOCATION = CONFIG_PATH_PREFIX + CONFIG_FILE_NAME;
    
    protected String[] _configLocations = { CONFIG_LOCATION, };
    
    @Override
    protected String getResourceSuffix() {
        
        return "_context.xml";
    }
    
    @Override
    protected boolean isGenerateDefaultLocations() {
        
        return false;
    }
    
    @Override
    public ApplicationContext loadContext(String... locations) {
        
        String[] allLocations = generateConfigLocations(Arrays.asList(locations));
        ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext(allLocations, false);
        /*
         * Set validating=false to avoid validation of XML configuration files.
         * This decreases startup time significantly.
         */
        context.setValidating(false);
        context.refresh(); // Load the context.
        SpringContextHolder.getInstance().setApplicationContext(context);
        
        return context;
    }
    
    private String[] getLocationsToLoad() {
        
        return _configLocations;
    }
    
    private String[] generateConfigLocations(List<String> additionalLocations) {
        
        String[] ecLocations = getLocationsToLoad();
        
        int size = ecLocations.length + additionalLocations.size();
        ArrayList<String> newLocations = new ArrayList<>(size);
        Collections.addAll(newLocations, ecLocations);
        
        // this line needs to be at the end so that any mock contexts will override normal contexts
        newLocations.addAll(additionalLocations);
        
        return newLocations.toArray(new String[size]);
    }
    
    /**
     * Added in order to support new version of Spring. There is a new
     * {@link org.springframework.test.context.SmartContextLoader} interface implemented in Spring.
     */
    @Override
    public ApplicationContext loadContext(MergedContextConfiguration mergedConfig) throws Exception {
        return mergedConfig.getContextLoader().loadContext(mergedConfig.getLocations());
    }
    
}
