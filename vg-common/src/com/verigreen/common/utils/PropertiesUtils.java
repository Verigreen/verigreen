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
/**
 * 
 */
package com.verigreen.common.utils;

import java.util.Properties;

/**
 * @author manashir
 * 
 */
public class PropertiesUtils {

    private PropertiesUtils() {
    }

    /**
     * First tries to get the propertyName from the environment variables. <br>
     * If does not exist - gets from props. <br>
     * If does not exist - puts the default value
     * 
     * @param props
     *            The {@link Properties} object
     * @param propertyName
     *            The property name to get
     * @param defaultValue
     *            The default value in case not the environment variables and not the properties
     *            object contains the property
     * @return
     */
    public static String getPropertyOverridenByEnvVar(
            Properties props,
            String propertyName,
            String defaultValue) {
        String value = System.getProperty(propertyName);
        if (value == null) {
            value = props.getProperty(propertyName, defaultValue);
        }
        
        return value;
    }
}
