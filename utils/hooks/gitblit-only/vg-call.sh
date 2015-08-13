#!/bin/bash 
#*******************************************************************************
# Copyright 2015 Hewlett-Packard Development Company, L.P.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and limitations under the License.
#*******************************************************************************
# Sample git pre-receive shell script to execute the Verigreen hook.
# Please use this carefully to avoid any issues with git hook triggering
# can and should be adapted for use with/for other hooks.
#*******************************************************************************
# Parameters: Accepts one required parameter - the repository name which is protected
#*******************************************************************************
debugFile=/tmp/vg.txt

if [ "$#" -ne 4 ]; then
    echo Error: This script must be run with 4 agruments!
    exit 2
fi

# Set the path to the hook.properties file if needed.
# modify as needed.
if [ -z $(env|grep "VG_HOOK") ]; then
    export VG_HOOK="<Path to hook.properties file>"
fi

# This sets the JAVA_HOME for use.
# modify as needed.
if [ -z $(env|grep "JAVA_HOME") ]; then
    export JAVA_HOME="<Path to JAVA_HOME if not defined in environment>"
fi

# debug
echo "VG HOOK: $VG_HOOK"      > $debugFile
echo "vector: $1 $2 $3 $4"   >> $debugFile
echo "JAVA_HOME: $JAVA_HOME" >> $debugFile

# Note: Each hook call must exit if its result is not 0
echo Call: java -jar "$VG_HOOK/git-hook.jar" $1 $2 $3 $4 >> $debugFile
java -jar "$VG_HOOK/git-hook.jar" $1 $2 $3 $4
result=$?
exit $result