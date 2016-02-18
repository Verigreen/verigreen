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

import java.lang.reflect.Method;

import com.verigreen.common.exception.LabException;

public class ReflectionComparer {

    private ReflectionComparer() {
    }

    public static <T> boolean isEquals(T x, T y) {
        
        boolean ret = true;
        Method[] xMethods = x.getClass().getMethods();
        for (Method currXMethod : xMethods) {
            String currXMethodName = currXMethod.getName();
            if (currXMethodName.startsWith("get")) {
                try {
                    Object currXVal = currXMethod.invoke(x);
                    Method currYMethod = y.getClass().getMethod(currXMethodName);
                    Object currYMethodVal = currYMethod.invoke(y);
                    if (!isObjEquals(currXVal, currYMethodVal)) {
                        ret = false;
                        break;
                    }
                } catch (Throwable thrown) {
                    throw new LabException(thrown);
                }
            }
        }
        
        return ret;
    }
    
    private static boolean isObjEquals(Object x, Object y) {
        
        boolean ret = false;
        if (x == null) {
            if (y == null) {
                ret = true;
            } else {
                ret = false;
            }
        } else if (y == null) {
            ret = false;
        } else {
            ret = x.equals(y);
        }
        
        return ret;
    }
}
