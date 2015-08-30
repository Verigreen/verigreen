#!/usr/local/bin/groovy
/*******************************************************************************
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
#*******************************************************************************/
import com.gitblit.GitBlit
import com.gitblit.Keys
import com.gitblit.models.RepositoryModel
import com.gitblit.utils.JGitUtils
import com.gitblit.utils.StringUtils
import java.lang.ProcessBuilder
import groovy.transform.Field
import java.io.File
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.transport.ReceiveCommand
import org.eclipse.jgit.transport.ReceiveCommand.Result
import org.slf4j.Logger

/********************
* Globals
*********************/
// Change these as needed:
// Windows only: In case JAVA_HOME path contains spaces, escape it with quotes 
@Field def JAVA_HOME = '"c:\\Program Files\\Java\\jre7"'               // leave empty if already configured in environment
// Linux example: @Field def JAVA_HOME = '/usr/lib/jvm/java-7-oracle'

// VG paths must NOT be escaped with quotes!
@Field def VG_HOOK   = 'c:\\apps\\gitblit-1.6.2\\data\\groovy\\vg'  // path to hook.properties file
// Linux example: @Field def VG_HOOK   = '/opt/gitblit-data/groovy/vg-hook-2.0.1'

@Field def VG_PATH   = 'c:\\apps\\gitblit-1.6.2\\data\\groovy\\vg' // path to git-hook.jar file
// Linux example: @Field def VG_PATH   = '/opt/gitblit-data/groovy/vg-hook-2.0.1'


// system globals
@Field def debugLevel = 1 // Verbosity level - 0: No messages, 1: minimal, 4: maximal
@Field def scriptName = 'verigreen.groovy'
@Field def ln         = System.getProperty("line.separator")    // get the system's line separator
@Field def osType     = System.properties['os.name'].toLowerCase()
@Field def folderSep  = (osType.contains('windows')) ? '\\' : '/'
@Field def javaExe    = (JAVA_HOME?.trim()) as boolean ? JAVA_HOME + folderSep + 'bin' + folderSep + 'java -jar ' : 'java -jar '
@Field def HOOK_CALL  = javaExe + '"' + VG_PATH + folderSep + "git-hook.jar"+ '"'


/********************
* Functions
*********************/
def executeOnShell(String command) {
  return executeOnShell(command, new File(System.properties.'user.dir'))
}

private def executeOnShell(String command, File workingDir, StringBuilder output) {
  def exitCode            = 2
  ProcessBuilder pb       = new ProcessBuilder(addShellPrefix(command))
  Map<String, String> env = pb.environment()
  
  if (VG_HOOK.equals("")) {
    output.append("[${scriptName}] Error: VG_HOOK is undefined. Exiting." + ln)
  }
  else
  {
    env.put("VG_HOOK", VG_HOOK)  // required
    
    if (!JAVA_HOME.equals("")) {     // use the JAVA_HOME if it is defined (earlier in this script)
        env.put("JAVA_HOME", JAVA_HOME)
    }
	if (debugLevel >= 1) {
		logger.info("[${scriptName}] JAVA_HOME:" + JAVA_HOME + ln )
		logger.info("[${scriptName}] VG_HOOK:"   + VG_HOOK   + ln )
		logger.info("[${scriptName}] HOOK_CALL:" + HOOK_CALL + ln + ln )
	}
	if (debugLevel >= 3) {
		logger.info("[${scriptName}] --- PB ENV ---" + ln + env.toString())
	}
    pb.directory(workingDir)  // gitblit runs groovy hooks in gitblit-data/groovy context by default
    pb.redirectErrorStream(true)
    Process p = pb.start()
    p.inputStream.eachLine {output.append it + ln }
    logger.info(output.toString())
    p.waitFor();
    exitCode = p.exitValue()
  }
  return exitCode
}
 
private def addShellPrefix(String command) {
  commandArray    = new String[3]
  commandArray[0] = (osType.contains('windows')) ? 'cmd' : 'sh'
  commandArray[1] = (osType.contains('windows')) ? '/c' : '-c'
  commandArray[2] = command
  return commandArray
}


/********************
* MAIN
********************/
// Indicate we have started the script
logger.info("[${scriptName}] Verigreen hook triggered by ${user.username} for ${repository.name}")
def repoName          = repository.name
Repository repo       = gitblit.getRepository(repository.name)
def repoPath          = repo.directory.canonicalPath
def sanitizedRepoName = StringUtils.stripDotGit(repoName)
if (debugLevel >= 1) { logger.info("[${scriptName}] --- Repo info ---" + ln) }
if (debugLevel >= 2) {
	// logger.info('Folder Separator:' + folderSep + ln) }
	logger.info("repo: " + repoName) 
}
if (debugLevel >= 1) { 
	logger.info("repo Path: "       + repoPath ) 
	logger.info("repo short name: " + sanitizedRepoName) 
	logger.info("HOOK_CALL: " + HOOK_CALL) 
}
repo.close()

for (ReceiveCommand command : commands) {
    // get the command's git parameters
	if (debugLevel >= 1) {
    logger.info("newrev: " + command.getNewId().name()) 
    logger.info("oldrev: " + command.getOldId().name()) 
    logger.info("Ref: "    + command.refName) 
	}
    vgCmd = HOOK_CALL                 + " " +
            sanitizedRepoName         + " " +
            command.getOldId().name() + " " +
            command.getNewId().name() + " " +
            command.refName
	logger.info('vgCmd: ' + vgCmd.toString())
    def repoPathHandle  = new File(repoPath)
    StringBuilder vgMsg = new StringBuilder()
    def vgErrCode       = executeOnShell(vgCmd.toString(), repoPathHandle, vgMsg)

    logger.info("[${scriptName}] Preparing to Exit Verigreen.groovy")

    // Reject the push if Verigreen is protecting this branch
    if (vgErrCode != 0) 
    {
        command.setResult(Result.REJECTED_OTHER_REASON, ln + vgMsg.toString() + ln)
    }
}