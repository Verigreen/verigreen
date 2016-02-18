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
package com.verigreen.hook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.verigreen.collector.common.log4j.VerigreenLogger;
import com.verigreen.common.concurrency.RuntimeUtils;
import com.verigreen.common.spring.SpringContextLoader;

public class EntryPoint {
    
    public static void main(String[] args) throws IOException, InterruptedException {
        
        initialize();
        String[] params = getParams(args);
        int ret = 0;
        if (params != null) {
            String repository = params[0];
            String oldrev = params[1];
            String newrev = params[2];
            String ref = params[3];
            // read xml
            // verify params
            //branch logic
            ret = new BranchOperator().processBranch(repository, oldrev, newrev, ref);
        }
        // else params not legal - exit
        System.exit(ret);
    }
    
    private static void initialize() {
        
        new SpringContextLoader().loadContext();
    }
    
    private static String[] getParams(String[] args) throws IOException {
        
        String[] ret = null;
        // read cmd line param (repo name...)
        // the following 3 params are provided by git using STDIN - oldrev, newrev, ref
        if (args == null || args.length == 0) {
            VerigreenLogger.get().error(
                    EntryPoint.class.getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    "Error: No parameters specified");
        } else if (args.length == 1) {
            // read stdin params
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            String s = in.readLine();
            VerigreenLogger.get().log(
                    EntryPoint.class.getName(),
                    RuntimeUtils.getCurrentMethodName(),
                    s);
            String[] split = s.split(" ");
            if (split.length != 3) {
                VerigreenLogger.get().error(
                        EntryPoint.class.getName(),
                        RuntimeUtils.getCurrentMethodName(),
                        "Error: Bad or missing STDIN params! " + s);
            } else {
                ret = new String[4];
                ret[0] = args[0];
                ret[1] = split[0];
                ret[2] = split[1];
                ret[3] = split[2];
            }
        } else if (args.length == 4) { // all params provided at the command line
            ret = new String[4];
            ret[0] = args[0];
            ret[1] = args[1];
            ret[2] = args[2];
            ret[3] = args[3];
        }
        
        return ret;
    }
}
