package cn.edu.fudan.codetracker.jgit;

import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

/**
 * description: 基于jgit 处理代码仓
 * @author fancying
 * create: 2019-06-05 17:16
 **/
@Slf4j
public class JGitHelper implements Closeable {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    private static final int MERGE_WITH_CONFLICT = -1;
    private static final int MERGE_WITHOUT_CONFLICT = 2;
    private static final int NOT_MERGE = 1;
    private Repository repository;
    private RevWalk revWalk;
    private Git git;

    /**
     *
     * repoPath 加上了 .git 目录
     *
     */
    public JGitHelper(String repoPath) {
        String gitDir =  IS_WINDOWS ? repoPath + "\\.git" : repoPath + "/.git";
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try {
            repository = builder.setGitDir(new File(gitDir))
                    .readEnvironment() // scan environment GIT_* variables
                    .findGitDir() // scan up the file system tree
                    .build();
            git = new Git(repository);
            revWalk = new RevWalk(repository);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void checkout(String commit) {
        try {
            git.reset().setMode(ResetCommand.ResetType.HARD).call();
            CheckoutCommand checkoutCommand = git.checkout();
            checkoutCommand.setName(commit).call();
        } catch (Exception e) {
            log.error("JGitHelper checkout error:{} ", e.getMessage());
        }
    }

    public String getAuthorName(String commit) {
        String authorName = null;
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(commit));
            authorName = revCommit.getAuthorIdent().getName();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return authorName;
    }

    public String getCommitTime(String commit) {
        String time = null;
        final String format = "yyyy-MM-dd HH:mm:ss";
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(commit));
            int t = revCommit.getCommitTime() ;
            long timestamp = Long.parseLong(String.valueOf(t)) * 1000;
            time = new java.text.SimpleDateFormat(format).format(new java.util.Date(timestamp));
        }catch (Exception e) {
            e.printStackTrace();
        }
        return time;
    }

    public Long getLongCommitTime(String version) {
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(version));
            return revCommit.getCommitTime() * 1000L;
        }catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }


    public String getMess(String commit) {
        String message = null;
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(commit));
            message = revCommit.getFullMessage();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }


    @Override
    public void close() {
        if (repository != null) {
            repository.close();
        }
    }


    public List<String> getCommitListByBranchAndBeginCommit(String branchName, String beginCommit, Boolean isUpdate) {
        checkout(branchName);
        Map<String, Long> commitMap = new HashMap<>(512);
        Long start = getLongCommitTime(beginCommit);
        if (start == 0) {
            throw new RuntimeException("beginCommit Error!");
        }
        try {
            Iterable<RevCommit> commits = git.log().call();
            for (RevCommit commit : commits) {
                Long commitTime = commit.getCommitTime() * 1000L;
                if (isUpdate) {
                    if (commitTime > start) {
                        commitMap.put(commit.getName(), commitTime);
                    }
                } else {
                    if (commitTime >= start) {
                        commitMap.put(commit.getName(), commitTime);
                    }
                }

            }
        } catch (GitAPIException e) {
            e.getMessage();
        }
        return new ArrayList<>(sortByValue(commitMap).keySet());
    }


    /**
     *
     *  getCommitTime return second not millisecond
     */
    public List<String> getCommitListByBranchAndDuration(String branchName, String duration) {
        checkout(branchName);
        final int durationLength = 21;
        Map<String, Long> commitMap = new HashMap<>(512);
        if (duration.length() < durationLength) {
            throw new RuntimeException("duration error!");
        }
        long start =  getTime(duration.substring(0,10));
        long end = getTime(duration.substring(11,21));
        try {
            Iterable<RevCommit> commits = git.log().call();
            for (RevCommit commit : commits) {
                long commitTime = commit.getCommitTime() * 1000L;
                if (commitTime <= end && commitTime >= start) {
                    commitMap.put(commit.getName(), commitTime);
                }
            }
        } catch (GitAPIException e) {
            e.getMessage();
        }
        return new ArrayList<>(sortByValue(commitMap).keySet());
    }

    /**
     * 由小到大排序
     * st.sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())).forEach(e -> result.put(e.getKey(), e.getValue()));
     * 默认由大到小排序
     * 类型 V 必须实现 Comparable 接口，并且这个接口的类型是 V 或 V 的任一父类。这样声明后，V 的实例之间，V 的实例和它的父类的实例之间，可以相互比较大小。
     */
    private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        Map<K, V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K, V>> st = map.entrySet().stream();
        st.sorted(Comparator.comparing(Map.Entry::getValue)).forEach(e -> result.put(e.getKey(), e.getValue()));
        return result;
    }

    /**
     * s : 2018.01.01
     */
    private long getTime(String s) {
        s = s.replace(".","-");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = dateFormat.parse(s);
            return  date.getTime();
        }catch (ParseException e) {
            e.getMessage();
        }
        return 0;
    }

    public String[] getCommitParents(String commit) {
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(commit));
            RevCommit[] parentCommits = revCommit.getParents();
            String[] result = new String[parentCommits.length];
            for (int i = 0; i < parentCommits.length; i++) {
                result[i] = parentCommits[i].getName();
            }
            return result;
        }catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public Map<String, List<DiffEntry>> getMappedFileList(String commit) {
        Map<String, List<DiffEntry>> result = new HashMap<>(8);
        try {
            RevCommit currCommit = revWalk.parseCommit(ObjectId.fromString(commit));
            RevCommit[] parentCommits = currCommit.getParents();
            for (RevCommit p : parentCommits) {
                List<DiffEntry> entries = getDiffEntry(p,currCommit);
                result.put(p.getName(), entries);
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, Map<String,List<String>>> getFileList(String commit) {
        Map<String, Map<String,List<String>>> map = new HashMap<>(4);
        Map<String, List<DiffEntry>> res = getMappedFileList(commit);
        for (String preCommit: res.keySet()) {
            Map<String,List<String>> tmp = new HashMap<>(8);
            tmp.put("ADD",new ArrayList<>());
            tmp.put("DELETE",new ArrayList<>());
            tmp.put("CHANGE", new ArrayList<>());
            tmp.put("RENAME", new ArrayList<>());
            List<DiffEntry> entryList = res.get(preCommit);
            for (DiffEntry diff: entryList) {
                switch (diff.getChangeType()){
                    case MODIFY:
                        tmp.get("CHANGE").add(diff.getNewPath());
                        break;
                    case ADD:
                        tmp.get("ADD").add(diff.getNewPath());
                        break;
                    case DELETE:
                        tmp.get("DELETE").add(diff.getOldPath());
                        break;
                    case RENAME:
                        String path = diff.getOldPath() + ":" + diff.getNewPath();
                        tmp.get("RENAME").add(path);
                        break;
                    default:
                        break;
                }
            }
            map.put(preCommit,tmp);
        }

        return map;
    }

    public int mergeJudgment(String commit) {
        Map<String, List<DiffEntry>> diffList = getMappedFileList(commit);
        if (diffList.keySet().size() == NOT_MERGE) {
            return NOT_MERGE;
        }
        Set<String> stringSet = new HashSet<>();
        boolean isFirst = true;
        for (List<DiffEntry> diffEntryList : diffList.values()) {
            for (DiffEntry diffEntry : diffEntryList) {
                if (isFirst) {
                    stringSet.add(diffEntry.getOldPath());
                } else if (stringSet.contains(diffEntry.getOldPath())){
                    return MERGE_WITH_CONFLICT;
                }
            }
            isFirst = false;
        }
        return MERGE_WITHOUT_CONFLICT;
    }

    @SneakyThrows
    public Map<String, List<DiffEntry>> getConflictDiffEntryList (String commit) {
        RevCommit currCommit = revWalk.parseCommit(ObjectId.fromString(commit));
        RevCommit[] parentCommits = currCommit.getParents();
        if (parentCommits.length != 2) {
            return null;
        }

        List<DiffEntry> parent1 = getDiffEntry(parentCommits[0], currCommit);
        List<DiffEntry> parent2 = getDiffEntry(parentCommits[1], currCommit);
        Map<String, List<DiffEntry>> result = new HashMap<>();
        String parent = parentCommits[0].getName() + "|" + parentCommits[1].getName();
        List<DiffEntry> entryList = new ArrayList<>();
        if (isParent2(parentCommits[0], parentCommits[1], currCommit)) {
            List<DiffEntry> tmp = parent1;
            parent1 = parent2;
            parent2 = tmp;
            parent = parentCommits[1].getName() + "|" + parentCommits[0].getName();
        }

        // oldPath 相同
        for (DiffEntry diffEntry1 : parent1) {
            for (DiffEntry diffEntry2 :parent2) {
                // todo 暂未考虑重命名的情况 或者无需考虑重命名的情况
                //  如 p1 a=a1  p2 a=>a2 是否冲突待验证
                boolean isSame = diffEntry1.getOldPath().equals(diffEntry2.getOldPath()) &&
                        diffEntry1.getNewPath().equals(diffEntry2.getNewPath());

                if (isSame) {
                    entryList.add(diffEntry1);
                }
            }
        }

        result.put(parent,entryList);
        return result;
    }

    private boolean isParent2(RevCommit parent1, RevCommit parent2, RevCommit currCommit) {
        String author1 = parent1.getAuthorIdent().getName();
        String author2 = parent2.getAuthorIdent().getName();
        String author = currCommit.getAuthorIdent().getName();
        if (author.equals(author2) && !author.equals(author1)) {
            return true;
        }

        if (!author.equals(author2) && author.equals(author1)) {
            return false;
        }

        return parent2.getCommitTime() > parent1.getCommitTime();
    }

    @SneakyThrows
    private List<DiffEntry> getDiffEntry(RevCommit parentCommit, RevCommit currCommit) {
        // 不可少 否则parentCommit的 tree为null
        parentCommit = revWalk.parseCommit(ObjectId.fromString(parentCommit.getName()));
        TreeWalk tw = new TreeWalk(repository);
        tw.addTree(parentCommit.getTree());
        tw.addTree(currCommit.getTree());
        tw.setRecursive(true);
        RenameDetector rd = new RenameDetector(repository);
        rd.addAll(DiffEntry.scan(tw));
        rd.setRenameScore(50);
        return rd.compute();
    }


    public static void main(String[] args) {
        //gitCheckout("E:\\Lab\\project\\IssueTracker-Master", "f8263335ef380d93d6bb93b2876484e325116ac2");
        //String repoPath = "E:\\Lab\\iec-wepm-develop";
//        String repoPath = "E:\\Lab\\project\\IssueTracker-Master-pre";
        String repoPath = "/Users/tangyuan/Documents/Git/IssueTracker-Master";
//        String commitId = "6d51c089986c9c7f8766d31a95a20254ecbdbc46";
        JGitHelper jGitHelper = new JGitHelper(repoPath);
//        Map<String, List<DiffEntry>> map = jGitHelper.getMappedFileList(commitId);
//        List<DiffEntry> map = null;
//        try {
//            RevCommit currCommit = jGitHelper.revWalk.parseCommit(ObjectId.fromString("6d51c089986c9c7f8766d31a95a20254ecbdbc46"));
//            RevCommit preCommit = jGitHelper.revWalk.parseCommit(ObjectId.fromString("e43a8c19634265f5ff057183c54e8f13e132fd05"));
//            map = jGitHelper.getDiffEntry(preCommit,currCommit);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        jGitHelper.getCommitListByBranchAndDuration("zhonghui20191012", "2019.10.12-2019.12.30");
//        String s[] = jGitHelper.getCommitParents(commitId);
//        int m = jGitHelper.mergeJudgment(commitId);
        Map<String, List<DiffEntry>> map = jGitHelper.getConflictDiffEntryList("f0f090c7541ca09dbf0d132a511591fdb57ecb23");
        System.out.println(map);
//        String t = jGitHelper.getCommitTime("f61e34233aa536cf5e698b502099e12d1caf77e4");
//        for (String s : jGitHelper.getCommitListByBranchAndDuration("zhonghui20191012", "2019.10.12-2019.12.16")) {
//            System.out.println(s);
//            jGitHelper.checkout(s);
//        }
    }

}