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
###############################################################################
# Copies the hook jar files to provided path and sets run permissions
###############################################################################
NAME="copyHook"

if [ -z $1 ]; then
    echo "[$NAME] FATAL: Please specify existing folder to copy hook files to."
    exit 7
elif [ ! -d $1 ]; then
    echo "[$NAME] Folder does not exist. Attempting to create..."
    mkdir $1
    
   if [ ! -d $1 ]; then
       echo "[$NAME] FATAL: failed to create folder. Exiting!"
       exit 1
   fi

fi

cp ./target/git-hook.jar $1
cp ./target/dependency/*.jar $1
chmod 775 $1/*.jar
