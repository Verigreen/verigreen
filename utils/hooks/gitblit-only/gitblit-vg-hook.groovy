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
@Field def JAVA_HOME = '/usr/lib/jvm/java-7-oracle'               // leave empty if already configured in environment
@Field def VG_HOOK   = '/opt/gitblit-data/groovy/vg-hook-2.0.1'  // path to hook.properties file
@Field def VG_PATH   = '/opt/gitblit-data/groovy/vg-hook-2.0.1' // path to git-hook.jar file
@Field def ln        = System.getProperty("line.separator")    // get the system's line separator
def HOOK_CALL = "java -jar " + VG_PATH + '/git-hook.jar'


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
    output.append("[Verigreen] VG_HOOK is undefined. Exiting." + ln)
  }
  else
  {
    env.put("VG_HOOK", VG_HOOK)  // required
    
    if (!JAVA_HOME.equals("")) {     // use the JAVA_HOME if it is defined (earlier in this script)
        env.put("JAVA_HOME", JAVA_HOME)
    }
    logger.info("--- PB ENV ---" + ln + env.toString()) // for debugging only
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
  commandArray[0] = "sh"
  commandArray[1] = "-c"
  commandArray[2] = command
  return commandArray
}


/********************
* MAIN
********************/
// Indicate we have started the script
logger.info("Verigreen hook triggered by ${user.username} for ${repository.name}")
def repoName          = repository.name
Repository repo       = gitblit.getRepository(repository.name)
def repoPath          = repo.directory.canonicalPath
def sanitizedRepoName = StringUtils.stripDotGit(repoName)
logger.info(ln + "--- Repo info ---" + ln)
logger.info("repo: "            + repoName)
logger.info("repo Path: "       + repoPath )
logger.info("repo short name: " + sanitizedRepoName)
repo.close()
logger.info("VG_CALL: " + HOOK_CALL)

for (ReceiveCommand command : commands) {
    // get the command's git parameters
    logger.info("newrev: " + command.getNewId().name())
    logger.info("oldrev: " + command.getOldId().name())
    logger.info("Ref: "    + command.refName)
    vgCmd = HOOK_CALL                 + " " +
            sanitizedRepoName         + " " +
            command.getOldId().name() + " " +
            command.getNewId().name() + " " +
            command.refName
    def repoPathHandle  = new File(repoPath)
    StringBuilder vgMsg = new StringBuilder()
    def vgErrCode       = executeOnShell(vgCmd.toString(), repoPathHandle, vgMsg)

    logger.info("Preparing to Exit Verigreen.groovy")

    // Reject the push if Verigreen is protecting this branch
    if (vgErrCode != 0) 
    {
        command.setResult(Result.REJECTED_OTHER_REASON, ln + vgMsg.toString() + ln)
    }
}