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

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

import com.verigreen.common.exception.LabException;

/**
 * Dynamically changing the classpath by adding classes during runtime
 */
public class ClasspathHacker {
    
    /**
     * Recursively searches the given directory and add to the classpath locations that ends with
     * the given suffix
     */
    public void add(Path directory, final String suffix) {
        
        try {
            final ClasspathContainer container = new ClasspathContainer();
            container.initialize(0);
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                
                @Override
                public FileVisitResult visitFile(Path p, BasicFileAttributes att)
                        throws IOException {
                    
                    if (p.toFile().getName().endsWith(suffix)) {
                        addURL(container, p.toUri().toURL());
                    }
                    
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new LabException(
                    String.format(
                            "Failed adding directory (%s) files with the following suffix: %s to the classpath",
                            directory.toString(),
                            suffix));
        }
    }
    
    /**
     * Search the first level of the given directory and add its sub folders to the classpath
     * according to filter & suffix
     * 
     * @param directory
     *            - the root directory
     * @param filter
     *            - filtering for sub directories
     * @param suffix
     *            - suffix to add to the filtered directories
     */
    public void add(Path directory, String filter, Path suffix) {
        
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(directory, filter)) {
            ClasspathContainer container = new ClasspathContainer();
            container.initialize(suffix.getNameCount());
            for (Path dir : ds) {
                Path path = dir.resolve(suffix).normalize();
                addURL(container, path.toUri().toURL());
            }
        } catch (IOException e) {
            throw new LabException(String.format(
                    "Failed adding directory (%s) sub-folders to the classpath",
                    directory.toString()));
        }
    }
    
    private void addURL(ClasspathContainer container, URL url) {
        
        if (!container.isExists(url)) {
            Class<?> sysclass = URLClassLoader.class;
            try {
                Method method = sysclass.getDeclaredMethod("addURL", new Class[] { URL.class });
                method.setAccessible(true);
                method.invoke(container.getLoader(), new Object[] { url });
            } catch (Throwable thrown) {
                throw new LabException(
                        String.format("Could not add URL (%s) to the classpath", url));
            }
        }
    }
    
    private static class ClasspathContainer {
        
        private Set<String> _classpath;
        private int _urlIndexDelta;
        
        public void initialize(int urlIndexDelta) {
            
            Set<String> classpath = new HashSet<>();
            URL[] urls = getLoader().getURLs();
            for (int i = 0; i < urls.length; i++) {
                String[] currClasspathUrl = urls[i].getFile().split("/");
                classpath.add(currClasspathUrl[currClasspathUrl.length - 1 - urlIndexDelta]);
            }
            _classpath = classpath;
            _urlIndexDelta = urlIndexDelta;
        }
        
        public URLClassLoader getLoader() {
            
            return (URLClassLoader) Thread.currentThread().getContextClassLoader();
        }
        
        public boolean isExists(URL url) {
            
            String[] splitUrl = url.getFile().split("/");
            
            return !_classpath.add(splitUrl[splitUrl.length - 1 - _urlIndexDelta]);
        }
    }
}
