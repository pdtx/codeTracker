package cn.edu.fudan.codetracker.core.tree;

import cn.edu.fudan.codetracker.core.tree.parser.FileParser;
import cn.edu.fudan.codetracker.core.tree.parser.JavaFileParser;
import cn.edu.fudan.codetracker.domain.projectinfo.*;
import cn.edu.fudan.codetracker.util.FileFilter;
import cn.edu.fudan.codetracker.util.JavaBaseRepoInfoParser;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description: java语言树
 *
 * @author fancying
 * create: 2020-05-18 00:22
 **/
@Slf4j
@Getter
@Setter
public class JavaTree extends BaseLanguageTree {

    public static final Language LANGUAGE = Language.JAVA;

    private Map<String, List<PackageNode>> moduleInfos;
    private List<PackageNode> packageInfos;
    private List<FileNode> fileInfos;
    private List<ClassNode> classInfos;
    private List<FieldNode> fieldInfos;
    private List<MethodNode> methodInfos;
    private List<StatementNode> statementInfos;

    public JavaTree(List<String> fileList, List<String> relativePath, String repoUuid) {
        super(fileList, repoUuid);
        moduleInfos = new HashMap<>(4);
        packageInfos = new ArrayList<>();
        fileInfos = new ArrayList<>();
        classInfos = new ArrayList<>();
        fieldInfos = new ArrayList<>();
        methodInfos = new ArrayList<>();
        statementInfos = new ArrayList<>();
        parseTree();
    }


    @Override
    public void parseTree() {
        analyze(this.getFileList(), this.getRelativePath(), this.getRepoUuid());
    }

    private void analyze(List<String> fileList, List<String> relativePath, String repoUuid) {
        if (fileList.size() != relativePath.size()) {
            log.error("fileList：{}，relativePath：{} ", fileList.size(), relativePath.size());
        }
        int minSize = Math.min(fileList.size(), relativePath.size());
        // 一个module内包含哪些package
        for (int i = 0; i < minSize ;i++) {
            String path = fileList.get(i);
            // 特定文件过滤
            if (FileFilter.javaFilenameFilter(path)) {
                continue;
            }
            JavaFileParser javaFileParser = new JavaFileParser();
            javaFileParser.parse(path, relativePath.get(i), repoUuid);
            String packageName = javaFileParser.getPackageName();
            // special situation ： end with .java but empty
            if (packageName == null) {
                continue;
            }
            String moduleName = javaFileParser.getModuleName();
            //String moduleName, String packageName
            PackageNode packageNode = new PackageNode(moduleName, packageName);
            if (moduleInfos.containsKey(moduleName)) {
                if (moduleInfos.get(moduleName).contains(packageNode)) {
                    packageNode = findPackageInfoByPackageName(packageNode, moduleInfos.get(moduleName));
                } else {
                    moduleInfos.get(moduleName).add(packageNode);
                }
            } else {
                List<PackageNode> packageInfos = new ArrayList<>();
                packageInfos.add(packageNode);
                moduleInfos.put(moduleName, packageInfos);
            }

            // 设置父节点
            javaFileParser.getFileNode().setParent(packageNode);
            javaFileParser.parseClassOrInterface();
            packageNode.getFileNodes().add(javaFileParser.getFileNode());
            // 设置子节点
            packageNode.setChildren(packageNode.getFileNodes());
        }
        travelRepoInfo();
    }

    private void travelRepoInfo() {
        for (List<PackageNode> packageInfos : moduleInfos.values()) {
            this.packageInfos.addAll(packageInfos);
            travel(packageInfos);
        }
    }

    @SuppressWarnings("unchecked")
    private void travel(List<? extends  BaseNode> baseNodes) {
        if (baseNodes == null || baseNodes.size() == 0) {
            return;
        }
        switch (baseNodes.get(0).getProjectInfoLevel()) {
            case FILE:
                fileInfos.addAll((List<FileNode>)baseNodes);
                break;
            case CLASS:
                classInfos.addAll((List<ClassNode>)baseNodes);
                for (BaseNode baseNode : baseNodes) {
                    ClassNode c = (ClassNode) baseNode;
                    travel(c.getFieldNodes());
                }
                break;
            case METHOD:
                methodInfos.addAll((List<MethodNode>)baseNodes);
                break;
            case FIELD:
                fieldInfos.addAll((List<FieldNode>)baseNodes);
                break;
            case STATEMENT:
                statementInfos.addAll((List<StatementNode>)baseNodes);
                break;
            default:
                break;
        }
        for (BaseNode baseNode : baseNodes) {
            baseNode.setRootUuid(baseNode.getUuid());
            travel(baseNode.getChildren());
        }
    }

    private PackageNode findPackageInfoByPackageName(PackageNode p1, List<PackageNode> packageInfos) {
        for (PackageNode packageNode : packageInfos) {
            if (p1.getPackageName().equals(packageNode.getPackageName())) {
                return packageNode;
            }
        }
        log.error("could not find package! package name:{},module name:{}", p1.getPackageName(), p1.getModuleName());
        return null;
    }
}