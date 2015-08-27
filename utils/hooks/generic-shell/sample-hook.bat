@ECHO OFF
REM #*******************************************************************************
REM # Copyright 2015 Hewlett-Packard Development Company, L.P.
REM #
REM # Licensed under the Apache License, Version 2.0 (the "License");
REM # you may not use this file except in compliance with the License.
REM # You may obtain a copy of the License at
REM #
REM #        http://www.apache.org/licenses/LICENSE-2.0
REM #
REM # Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM # See the License for the specific language governing permissions and limitations under the License.
REM #*******************************************************************************
REM # Sample git pre-receive shell script to execute the Verigreen hook.
REM # Please use this carefully to avoid any issues with git hook triggering
REM # can and should be adapted for use with/for other hooks.
REM #*******************************************************************************
REM # Parameters: Accepts one required parameter - the repository name which is protected
REM #*******************************************************************************
SETLOCAL
SET "DEBUG_FILE=C:\Temp\vg.txt"

REM Check number of arguments specified on run
IF "%4" == "" (
	ECHO Error: Script must be run with 4 arguments!
    EXIT /B 2
)

REM Set the path to the hook.properties file if needed.
REM modify as needed.
IF  "X~%VG_HOOK%" EQU "X~%VG_HOOK%" SET "VG_HOOK=C:\verigreen\hook\2.0.1"
ECHO VG_HOOK: %VG_HOOK%
rem "<Path to hook.properties file>"

REM This sets the JAVA_HOME for use.
REM modify as needed.
IF "X~%JAVA_HOME%" EQU "X~%JAVA_HOME%" SET "JAVA_HOME=C:\Program Files\Java\jdk1.7.0_25"
ECHO JAVA_HOME: %JAVA_HOME%
rem "<Path to JAVA_HOME if not defined in environment>"

REM debug
ECHO VG HOOK: %VG_HOOK%      > %DEBUG_FILE%
ECHO Vector: %1 %2 %3 %4    >> %DEBUG_FILE%
ECHO JAVA_HOME: %JAVA_HOME% >> %DEBUG_FILE%

REM Note: Each hook call must exit if its result is not 0
echo CALL: "%JAVA_HOME%\bin\java.exe" -jar "%VG_HOOK%\git-hook.jar" %1 %2 %3 %4 >> %DEBUG_FILE%
"%JAVA_HOME%\bin\java.exe" -jar "%VG_HOOK%\git-hook.jar" %1 %2 %3 %4

ECHO ERRORLEVEL: %ERRORLEVEL% >> %DEBUG_FILE%
EXIT /B %ERRORLEVEL%