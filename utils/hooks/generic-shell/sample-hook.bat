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
SETLOCAL enabledelayedexpansion
SET "DEBUG_FILE=C:\Temp\vg.txt"

REM #*******************************************************************************
REM # Required parameters - Set these as needed
REM #*******************************************************************************
REM The IF conditions are used to set the value only if the variable in undefined
REM Remove the IF part and use only the SET part in case variable need be overwritten

REM Set the path to the hook.properties file
REM modify as needed.

IF  "%VG_HOOK%" EQU "%^VG_HOOK%" SET "VG_HOOK=C:\verigreen"
REM ECHO VG_HOOK: %VG_HOOK%


REM Set the path to the git-hook.jar file
REM modify as needed.
IF  "%VG_PATH%" EQU "%^VG_PATH%" SET "VG_PATH=C:\verigreen\hook"
REM ECHO VG_PATH: %VG_PATH%


REM This sets the JAVA_HOME for use.
REM modify as needed.
IF "%JAVA_HOME%" EQU "%^JAVA_HOME%" SET "JAVA_HOME=C:\Program Files\Java\jdk1.7.0_25"
REM ECHO JAVA_HOME: %JAVA_HOME%


REM #*******************************************************************************
REM # Main
REM #*******************************************************************************

REM Check number of arguments specified on run
REM First parameter passed to this script is the repository name
IF "%1" == "" (
	ECHO "Error: Script must be run with one (1) argument!"
    EXIT /B 2
)

REM Read STDIN (oldrev newrev ref)
for /F "tokens=*" %%a in ('more') do (
  SET LINE=%%a 
  REM echo #LINE: %LINE% 
)

REM debug
ECHO VG HOOK:   %VG_HOOK%       >  %DEBUG_FILE%
ECHO Vector:    %LINE%          >> %DEBUG_FILE%
ECHO JAVA_HOME: %JAVA_HOME%     >> %DEBUG_FILE%

REM Note: !!!   Each hook call must exit if its result is not 0   !!!
echo CALL: "%JAVA_HOME%\bin\java.exe" -jar "%VG_PATH%\git-hook.jar" %1 %LINE%
REM >> %DEBUG_FILE%
call "%JAVA_HOME%\bin\java.exe" -jar "%VG_PATH%\git-hook.jar" %1 %LINE%
ECHO ERRORLEVEL: %ERRORLEVEL% >> %DEBUG_FILE%
EXIT /B %ERRORLEVEL%
