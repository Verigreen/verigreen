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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class RuntimeUtils {

    private RuntimeUtils() {
    }

    public static boolean isDebuggerAttached() {
        
        boolean isDebug =
                java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf(
                        "-agentlib:jdwp") > 0;
        
        return isDebug;
    }
    
    public static String getCurrentMethodName() {
        
        return getMethodName(1);
    }
    
    public static String getMethodName(int positionInStackTrace) {
        
        //positionInStackTrace refers to the previous method - that is why we increment by 2
        return Thread.currentThread().getStackTrace()[positionInStackTrace + 2].getMethodName();
    }
    
    public static boolean isInProductionMode() {
        // note: add the following JVM parameter to run in dev mode: 
        // -Dproduction.mode=false  
        // the default is production mode (not dev mode), therefore:
        // 1. SystemOut will not print anything, and
        // 2. Single LAB project.
        String property = System.getProperty("production.mode");
        if (property != null && property.equals("false")) {
            return false;
        }
        return true;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        
        return (T) obj;
    }
    
    public static List<Field> getAnnotatedFields(
            Field[] fields,
            Class<? extends Annotation> annotation) {
        
        List<Field> ret = new ArrayList<Field>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(annotation))
                ret.add(field);
        }
        
        return ret;
    }
}
