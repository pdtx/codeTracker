/**
 * @description:
 * @author: fancying
 * @create: 2019-06-05 17:16
 **/
package cn.edu.fudan.codetracker.jgit;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JGitHelper {

    private Repository repository;
    private RevWalk revWalk;
    private String repoPath;
    private Git git;

    private final int durationLength = 21;

    /**
     *
     * repoPath 加上了 .git 目录
     *
     */
    public JGitHelper(String repoPath) {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try {
            repository = builder.setGitDir(new File(repoPath + "\\.git"))
                    .readEnvironment() // scan environment GIT_* variables
                    .findGitDir() // scan up the file system tree
                    .build();
            git = new Git(repository);
            revWalk = new RevWalk(repository);
            this.repoPath = repoPath;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkout(String version) {
        try {
            CheckoutCommand checkoutCommand = git.checkout();
            checkoutCommand.setName(version).call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getAuthorName(String version) {
        String authorName = null;
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(version));
            authorName = revCommit.getAuthorIdent().getName();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return authorName;
    }

    public String getCommitTime(String version) {
        String time = null;
        final String format = "yyyy-MM-dd HH:mm:ss";
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(version));
            int t = revCommit.getCommitTime() ;
            Long timestamp = Long.parseLong(String.valueOf(t)) * 1000;
            time = new java.text.SimpleDateFormat(format).format(new java.util.Date(timestamp));
        }catch (Exception e) {
            e.printStackTrace();
        }
        return time;
    }

    public String getMess(String version) {
        String message = null;
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(version));
            message = revCommit.getFullMessage();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }


    public void close() {
        if (repository != null) {
            repository.close();
        }
    }


    public static Repository openJGitRepository(String repoPath) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        return builder.setGitDir(new File(repoPath))
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build();
    }

    public static Repository createNewRepository() throws IOException {
        // prepare a new folder
        File localPath = File.createTempFile("TestGitRepository", "");
        if(!localPath.delete()) {
            throw new IOException("Could not delete temporary file " + localPath);
        }

        // create the directory
        Repository repository = FileRepositoryBuilder.create(new File(localPath, ".git"));
        repository.create();

        return repository;
    }

    public static void gitCheckout(String repoDir, String version) {

        File RepoGitDir = new File(repoDir + "\\.git");
        if (!RepoGitDir.exists()) {
            System.out.println("Error! Not Exists : " + RepoGitDir.getAbsolutePath());
            //logger.info("Error! Not Exists : " + RepoGitDir.getAbsolutePath());
        } else {
            Repository repo = null;
            try {
                repo = new FileRepository(RepoGitDir.getAbsolutePath());
                Git git = new Git(repo);
                CheckoutCommand checkout = git.checkout();
                checkout.setName(version);
                checkout.call();
                System.out.println("Checkout to " + version);
                //logger.info("Checkout to " + version);

                PullCommand pullCmd = git.pull();
                pullCmd.call();
                System.out.println("Pulled from remote repository to local repository at " + repo.getDirectory());
                //logger.info();
            } catch (Exception e) {
                System.out.println(e.getMessage() + " : " + RepoGitDir.getAbsolutePath());
                //logger.info(e.getMessage() + " : " + RepoGitDir.getAbsolutePath());
            } finally {
                if (repo != null) {
                    repo.close();
                }
            }
        }
    }


    public static void main(String[] args) {
        //gitCheckout("E:\\Lab\\project\\IssueTracker-Master", "f8263335ef380d93d6bb93b2876484e325116ac2");
        String repoPath = "E:\\Lab\\project\\train-ticket";
        String commitId = "56ecb887353075ff557638843e234a8411b5fb8c";
        JGitHelper jGitHelper = new JGitHelper(repoPath);
        jGitHelper.checkout(commitId);
        System.out.println(jGitHelper.getAuthorName(commitId));
        System.out.println(jGitHelper.getCommitTime(commitId));
        System.out.println(jGitHelper.getMess(commitId));
    }


    public List<String> getCommitListByBranchAndDuration(String branchName, String duration) {
        List<String> commitList = new ArrayList<>(32);
        if (duration.length() < durationLength) {
            throw new RuntimeException("duration error!");
        }
        String start = duration.substring(0,10);
        String end = duration.substring(11,21);

        RevWalk walk = new RevWalk(repository);

        try {
            List<Ref> branches = git.branchList().call();
        } catch (GitAPIException g) {

        }


        if (! gitCheckout(branchName)) {
            return null;
        }

/*        String branchName = branchName.getName();

        try {
            Iterable<RevCommit> commits = git.log().all().call();
            for (RevCommit commit : commits) {
                boolean foundInThisBranch = false;

                RevCommit targetCommit = walk.parseCommit(repo.resolve(
                        commit.getName()));
                for (Map.Entry<String, Ref> e : repo.getAllRefs().entrySet()) {
                    if (e.getKey().startsWith(Constants.R_HEADS)) {
                        if (walk.isMergedInto(targetCommit, walk.parseCommit(
                                e.getValue().getObjectId()))) {
                            String foundInBranch = e.getValue().getName();
                            if (branchName.equals(foundInBranch)) {
                                foundInThisBranch = true;
                                break;
                            }
                        }
                    }
                }

                if (foundInThisBranch) {
                    System.out.println(commit.getName());
                    System.out.println(commit.getAuthorIdent().getName());
                    System.out.println(new Date(commit.getCommitTime() * 1000L));
                    System.out.println(commit.getFullMessage());
                }
            }
        } catch (Exception e) {
            e.getMessage();
        }*/


        

        return commitList;

    }

    private boolean gitCheckout(String version) {
        try {
            CheckoutCommand checkout = git.checkout();
            checkout.setName(version);
            checkout.call();
            System.out.println("Checkout to " + version);
            //logger.info("Checkout to " + version);
            PullCommand pullCmd = git.pull();
            pullCmd.call();
            System.out.println("Pulled from remote repository to local repository at " );
            //logger.info();
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage() + " : " + git.getRepository().getRemoteNames());
            return false;
            //logger.info(e.getMessage() + " : " + RepoGitDir.getAbsolutePath());
        }

    }


}