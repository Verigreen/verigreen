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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputValidation;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjectCopier<T extends Serializable> {
    
    private byte[] objectHolder;
    
    public void copyObject(T resourceList) {
        
        try {
            ByteArrayOutputStream bao = new ByteArrayOutputStream(1024);
            ObjectOutputStream oo = new ObjectOutputStream(bao);
            oo.writeObject(resourceList);
            oo.close();
            objectHolder = bao.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to copy object %s", //$NON-NLS-1$
                    resourceList == null ? "(null)" : resourceList.toString()), e); //$NON-NLS-1$
        }
    }
    
    @SuppressWarnings("unchecked")
    public T pasteObject() {
        
        try {
            ByteArrayInputStream bai = new ByteArrayInputStream(objectHolder);
            ObjectInputStream oi = new ObjectInputStream(bai);
            T resourceList = (T) oi.readObject();
            if (resourceList instanceof ObjectInputValidation) {
                ((ObjectInputValidation) resourceList).validateObject();
            }
            
            return resourceList;
        } catch (Exception e) {
            throw new RuntimeException("Failed To Paste Object", e); //$NON-NLS-1$
        }
    }
}
