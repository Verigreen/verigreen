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

import com.verigreen.common.concurrency.RuntimeUtils;

// this class is used so that messages printed to the system console will not
// appear when running in production.
public class SystemOut {
    
    private static boolean isInProductionMode = RuntimeUtils.isInProductionMode();

    private SystemOut() {
    }

    public static void println(String x) {
        
        if (!isInProductionMode) {
            System.out.println(x);
        }
    }
    
    public static void println(int x) {
        
        if (!isInProductionMode) {
            System.out.println(x);
        }
    }
    
    public static void println(Object x) {
        
        if (!isInProductionMode) {
            System.out.println(x);
        }
    }
    
    public static void error(String x) {
        
        if (!isInProductionMode) {
            System.err.println(x);
        }
    }
    
    public static void println(Throwable ex) {
        
        if (!isInProductionMode) {
            ex.printStackTrace();
        }
    }
}
