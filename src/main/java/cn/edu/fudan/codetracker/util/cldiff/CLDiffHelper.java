/**
 * @description: 工具调用
 * @author: fancying
 * @create: 2019-09-25 09:51
 **/
package cn.edu.fudan.codetracker.util.cldiff;

import edu.fdu.se.cldiff.CLDiffLocal;
import edu.fdu.se.global.CommitDesc;
import edu.fdu.se.global.Global;


public class CLDiffHelper {


    public static void executeCLDiff(String repoPath, String commitId, String outputDir) {
        Global.runningMode = 0;
        Global.granularity = Global.GRANULARITY.STATEMENT;
        Global.RUNNING_LANG = CommitDesc.RUNNING_LANG.JAVA;
        Global.isMethodRangeContainsJavaDoc = false;
        CLDiffLocal CLDiffLocal = new CLDiffLocal();
        //System.out.println(repoPath + "\\.git");
        if (repoPath.contains("\\")) {
            // windows
            CLDiffLocal.run(commitId, repoPath + "\\.git", outputDir);
        }else {
            // Linux
            CLDiffLocal.run(commitId, repoPath + "/.git", outputDir);
        }

    }



}