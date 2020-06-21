package cn.edu.fudan.codetracker.util;

import cn.edu.fudan.codetracker.util.comparison.SuffixUtil;

import static cn.edu.fudan.codetracker.util.comparison.CosineUtil.cosineSimilarity;

/**
 * description:
 *
 * @author fancying
 * create: 2020-06-22 00:15
 **/
public class Test {

    public static void main(String[] args) {

        String parent = "";
        String child1 = "";
        String child2 = "" ;

        String code1 = "{\n" +
//                "            FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();\n" +
//                "            Repository repository = repositoryBuilder.setGitDir(new File(repo_path + \"/.git\"))\n" +
//                "                    .readEnvironment() // scan environment GIT_* variables\n" +
//                "                    .findGitDir() // scan up the file system tree\n" +
//                "                    .setMustExist(true)\n" +
//                "                    .build();\n" +
//                "            // find the current commit id\n" +
//                "//            ObjectId curCommitId = repository.resolve(commit_id);\n" +
//                "            Git git = new Git(repository);\n" +
//                "            try {\n" +
//                "                Iterable<RevCommit> log = git.log().call();\n" +
//                "                for(RevCommit revCommit:log){\n" +
//                "                    spi.add(revCommit.getAuthorIdent().getName()) ;\n" +
//                "                }\n" +
//                "            } catch (GitAPIException e) {\n" +
//                "                e.printStackTrace();\n" +
//                "            }\n" +
//                "\n" +
                "        }";
        String code2 = "{\n" +
//                "            FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();\n" +
//                "            Repository repository = repositoryBuilder.setGitDir(new File(repo_path + \"/.git\"))\n" +
//                "                    .readEnvironment() // scan environment GIT_* variables\n" +
//                "                    .findGitDir() // scan up the file system tree\n" +
//                "                    .setMustExist(true)\n" +
//                "                    .build();\n" +
//                "            // find the current commit id\n" +
//                "//            ObjectId curCommitId = repository.resolve(commit_id);\n" +
//                "            Git git = new Git(repository);\n" +
//                "            try {\n" +
//                "                Iterable<RevCommit> log = git.log().call();\n" +
//                "                for(RevCommit revCommit:log){\n" +
//                "                    spi.add(revCommit.getAuthorIdent().getName()) ;\n" +
//                "                }\n" +
//                "            } catch (GitAPIException e) {\n" +
//                "                e.printStackTrace();\n" +
//                "            }\n" +
//                "\n" +
                "        }";

        double t = cosineSimilarity(code1,code2);
        if (((Double)Double.NaN).equals(t)){
            System.out.println("equal");
        }

        if (t> 0.8) {
            System.out.println(true);
        }

        System.out.println(t);

        t = SuffixUtil.suffixSimilarity(code1,code2);

        System.out.println(t);

    }
}