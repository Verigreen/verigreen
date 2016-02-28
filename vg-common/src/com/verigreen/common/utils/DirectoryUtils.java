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

import java.nio.file.Path;
import java.nio.file.Paths;

public class DirectoryUtils {

    private DirectoryUtils() {
    }

    /**
     * Returns a path to the given directory, if such can be resolved from your current path.
     * 
     * @param folder
     *            - the desired folder name
     */
    public static Path getDirectory(String folder) {
        
        Path basePath = Paths.get(".").toAbsolutePath();
        Path currentPath = basePath;
        while (currentPath != null) {
            Path cloudServicesPath = currentPath.resolve(folder);
            if (cloudServicesPath.toFile().exists()) {
                currentPath = cloudServicesPath;
                break;
            }
            currentPath = currentPath.getParent();
        }
        
        return currentPath;
    }
}
