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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class CollectionUtils {

    private CollectionUtils() {
    }

    public static boolean isEquals(List<String> left, List<String> right) {
        
        boolean ret = false;
        if (left != null && right != null) {
            int rightSize = right.size();
            if (left.size() == rightSize) {
                if (left.isEmpty() && right.isEmpty()) {
                    ret = true;
                } else {
                    ret =
                            org.apache.commons.collections.CollectionUtils.intersection(left, right).size() == rightSize;
                }
            }
        } else if (left == null && right == null) {
            ret = true;
        }
        
        return ret;
    }
    
    public static boolean isNullOrEmpty(Collection<?> collection) {
        
        return collection == null || collection.size() == 0;
    }
    
    public static String toString(Collection<?> data, String collectonName) {
        StringBuilder builder = new StringBuilder();
        builder.append(collectonName).append(" = ").append("{");
        if (data != null) {
            Iterator<?> iter = data.iterator();
            while (iter.hasNext()) {
                builder.append(iter.next());
                if (iter.hasNext()) {
                    builder.append(", ");
                }
            }
        }
        builder.append("}");
        return builder.toString();
    }
    
    public static String toString(Collection<?> data) {
        
        return toString(data, StringUtils.EMPTY_STRING);
    }
}
