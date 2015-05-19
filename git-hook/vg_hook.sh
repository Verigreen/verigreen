#!/bin/bash

[[ ! -e "$VG_HOOK/hook.properties" ]] && echo "ERROR: please verify that you VG_HOOK points to a directory with a hook.properties file." && exit -1
[[ ! -e "$VG_HOOK_HOME/git-hook.jar" ]] && echo "ERROR: please verify that you VG_HOOK_HOME points to a directory with a git-hook.jar file." && exit -1

REPO="Dummy" #dummy value for now
read LINE;
echo $LINE > "$VG_HOOK_HOME/vg-git-hook.log
java -jar "$VG_HOOK_HOME/git-hook.jar" $REPO $LINE
