/**
 * @description: 工具调用
 * @author: fancying
 * @create: 2019-09-25 09:51
 **/
package cn.edu.fudan.codetracker.util.cldiff;

import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import edu.fdu.se.API.CLDiffLocal;

import java.util.logging.Level;

public class ClDiffHelper {


    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    public static void executeDiff(String repoPath, String commitId, String outputDir) {
        repoPath = IS_WINDOWS ? repoPath + "\\.git" : repoPath + "/.git";
        Global.runningMode = Constants.COMMAND_LINE;
        Global.granularity = Constants.GRANULARITY.STATEMENT;
        Global.initLangAndLogger(Constants.RUNNING_LANG.JAVA, Level.WARNING);
        Global.isMethodRangeContainsJavaDoc = false;
        Global.isLink = true;
        CLDiffLocal clDiffLocal = new CLDiffLocal(repoPath);
        clDiffLocal.run(commitId, repoPath, outputDir);
    }

    public static void main(String[] args) {

        String repo = "E:\\Lab\\project\\IssueTracker-Master";
        String commitId = "d81d2b4c232392228a9375bc12e3c3ebe349407c";
        String outputDir = "E:\\Lab\\project\\new";

        ClDiffHelper.executeDiff(repo, commitId, outputDir);
    }


}