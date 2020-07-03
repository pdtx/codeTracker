/**
 * @description: 工具调用
 * @author: fancying
 * @create: 2019-09-25 09:51
 **/
package cn.edu.fudan.codetracker.util.cldiff;

import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import edu.fdu.se.API.CLDiffLocal;
import lombok.extern.slf4j.Slf4j;

import java.util.logging.Level;

@Slf4j
public class ClDiffHelper {


    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    public synchronized static void executeDiff(String repoPath, String commitId, String outputDir) {
        try {
            repoPath = IS_WINDOWS ? repoPath + "\\.git" : repoPath + "/.git";
            Global.runningMode = Constants.COMMAND_LINE;
            Global.granularity = Constants.GRANULARITY.STATEMENT;
            Global.initLangAndLogger(Constants.RUNNING_LANG.JAVA, Level.WARNING);
            Global.isMethodRangeContainsJavaDoc = false;
            Global.isLink = false;
            CLDiffLocal clDiffLocal = new CLDiffLocal(repoPath);
            clDiffLocal.run(commitId, repoPath, outputDir);
        } catch (Exception e) {
            log.error("CLDiff error: {}", e.getMessage());
        }
    }

    public static void main(String[] args) {

        String repo = "/Users/tangyuan/Documents/Git/iec-wepm-develop";
        String commitId = "6b2cafe873e520843a637733188fa3495929d02a";
        String outputDir = "/Users/tangyuan/Desktop/CLDdemo";

        ClDiffHelper.executeDiff(repo, commitId, outputDir);
    }


}