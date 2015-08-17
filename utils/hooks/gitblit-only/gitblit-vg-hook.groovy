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
import com.gitblit.models.TeamModel
import com.gitblit.models.UserModel
import com.gitblit.utils.JGitUtils
import com.gitblit.utils.StringUtils
import java.io.File
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.transport.ReceiveCommand
import org.eclipse.jgit.transport.ReceiveCommand.Result
import org.slf4j.Logger

/********************
* Globals
*********************/
// Change this as needed:
def VG_PATH   = '<Path to the folder containing the vg-call.sh file>'
def HOOK_CALL = VG_PATH + '/vg-call.sh'


/********************
* Functions
*********************/
def executeOnShell(String command) {
  return executeOnShell(command, new File(System.properties.'user.dir'))
}

private def executeOnShell(String command, File workingDir, StringBuilder output) {
  logger.info(command)
  def process = new ProcessBuilder(addShellPrefix(command))
                                    .directory(workingDir)
                                    .redirectErrorStream(true)
                                    .start()
  def lineSeperator = System.getProperty("line.separator")
  process.inputStream.eachLine {output.append it + lineSeperator }
  logger.info(output.toString())
  process.waitFor();
  return process.exitValue()
}

// change below to "cmd" and "/c" for Windows
private def addShellPrefix(String command) {
  commandArray = new String[3]
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
// get the system's line separator
def ln        = System.getProperty("line.separator")
logger.info(ln + "--- Repo info ---" + ln)
def repoName          = repository.name
Repository repo       = gitblit.getRepository(repository.name)
def repoPath          = repo.directory.canonicalPath
def sanitizedRepoName = StringUtils.stripDotGit(repoName)
logger.info("repo: "            + repoName)
logger.info("repo Path: "       + repoPath )
logger.info("repo short name: " + sanitizedRepoName)
repo.close()
logger.info("VG_CALL: " + HOOK_CALL)

for (ReceiveCommand command : commands)
{
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