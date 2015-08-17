#! /bin/bash 
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

# Functions
# Read the hook's return code and exit if it's not a success (0)
# This function MUST be run after each hook call, it verified the script exits correctly
function ExitOnNonZero() {
    result=$?

    if [ $result -ne 0 ]; then
        echo "$1 returned: $result";
        exit $result
    fi
}

#*******************************************************************************
# Required Exports and internal parameters - Modify as needed!
export JAVA_HOME="<JAVA_HOME_PATH>"
export VG_HOOK="<Path_to_Verigreen_hook.properties_File>"
VG_PATH="<Path_to_Verigreen_git-hook.jar_File"

#*******************************************************************************
# Get git parameters off STDIN, as passed by the git provider
# NOTE: the order of these parameters may change depending git provider, i.e. 
# Stash / GitLab / GitBlit / Others
read oldrev newrev ref;

#*******************************************************************************
# Verify the script's received it's required parameter(s)
if [ "$#" -ne 1 ]; then
    echo "[$0] Illegal number of parameters provided."
    echo "   Please specify the repository name when executing this script."
fi

for parameter in "RepositoryName $1" "Oldrev $oldrev" "Newrev $newrev" "Ref $ref" "VG_HOOK $VG_HOOK" "VG_PATH $VG_PATH"
do
    set -- $parameter

    if [ -z "$2" ]; then
        echo "[$0] Bad or missing parameter: $1"
    fi
done

#*******************************************************************************
# Debugging:
DEBUG="1"                       # "1" enables parameters printouts.
DEBUG_FILE=/tmp/hook_caller.log # modify as needed

if [ $DEBUG -eq 1 ]; then
    echo "Script name: $0"                            >  $DEBUG_FILE
    echo "JAVA_HOME: $JAVA_HOME"                      >> $DEBUG_FILE
    echo "VG_HOOK: $VG_HOOK"                          >> $DEBUG_FILE
    echo "Oldrev: $oldrev Newrev: $newrev Ref: $ref"  >> $DEBUG_FILE
fi

#*******************************************************************************
# run the Verigreen hook
# Note: Verigreen is typically the last hook that need be run;
# Any other git hooks need be run BEFORE the verigreen hook.
# Make sure and call the ExitOnNonZero function after each executed hook.
java -jar $VG_PATH/git-hook.jar $1 $oldrev $newrev $ref
ExitOnNonZero "Verigreen"

exit 0 # Exit with Success if all checks completed OK