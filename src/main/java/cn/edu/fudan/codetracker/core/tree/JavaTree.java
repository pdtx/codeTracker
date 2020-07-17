package cn.edu.fudan.codetracker.core.tree;

import cn.edu.fudan.codetracker.core.tree.parser.JavaFileParser;
import cn.edu.fudan.codetracker.domain.projectinfo.*;
import cn.edu.fudan.codetracker.util.FileFilter;
import cn.edu.fudan.codetracker.util.JavancssScaner;
import cn.edu.fudan.codetracker.util.comparison.CosineUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * description: java语言树
 *
 * @author fancying
 * create: 2020-05-18 00:22
 **/
@Slf4j
@Data
@Component("java")
@Scope("prototype")
@NoArgsConstructor
public class JavaTree extends BaseLanguageTree {

    private Map<String, List<PackageNode>> moduleInfos;
    private List<PackageNode> packageInfos;
    private List<FileNode> fileInfos;
    private List<ClassNode> classInfos;
    private List<FieldNode> fieldInfos;
    private List<MethodNode> methodInfos;
    private List<StatementNode> statementInfos;
    private Map<String, List<MethodCall>> methodCallMap;

    public JavaTree(List<String> fileList, String repoUuid, String repoPath) {
        super(fileList, repoUuid, repoPath);
        parseTree();
    }


    @Override
    public void parseTree() {
        moduleInfos = new HashMap<>(4);
        packageInfos = new ArrayList<>();
        fileInfos = new ArrayList<>();
        classInfos = new ArrayList<>();
        fieldInfos = new ArrayList<>();
        methodInfos = new ArrayList<>();
        statementInfos = new ArrayList<>();
        methodCallMap = new HashMap<>(4);
        analyze(super.getFileList(), super.getRepoUuid());
    }

    private void analyze(List<String> fileList, String repoUuid) {
        // 一个module内包含哪些package
        for (int i = 0; i < fileList.size() ;i++) {
            String path = fileList.get(i);
            // 特定文件过滤
            if (FileFilter.javaFilenameFilter(path)) {
                continue;
            }
            JavaFileParser javaFileParser = new JavaFileParser();
            javaFileParser.parse(path, repoUuid, super.getRepoPath());
            String packageName = javaFileParser.getPackageName();
            // special situation ： end with .java but empty
            if (packageName == null) {
                log.error("packageName is null");
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

            //添加调用关系
            methodCallMap.putAll(javaFileParser.getMethodCallMap());

            extractFileCCNs(path,javaFileParser.getFileNode());
        }
        travelRepoInfo();
    }

    /**
     * 计算出文件节点的圈复杂度，以及属于该文件的方法的圈复杂度
     * @param fileNode,path
     */
    private void extractFileCCNs(String path,FileNode fileNode) {
        Map<String,Integer> methodCCNs = JavancssScaner.getOneFileCCNs(path);
        List<MethodNode> methodNodes = getMethodNodesFromFileNode(fileNode);
        int fileCCN = 0;
        Set<MethodNode> unMatchNodes = new HashSet<>();
        for (MethodNode methodNode : methodNodes) {
            String signature = methodNode.getSignature().replace(" ","");
            if (methodCCNs.keySet().contains(signature)) {
                int methodCCN = methodCCNs.get(signature);
                methodNode.setCcn(methodCCN);
                fileCCN += methodCCN;
                methodCCNs.remove(signature);
            } else {
                unMatchNodes.add(methodNode);
            }
        }
        //未能精准匹配的，按照一定相似度阈值进行匹配，以处理签名解析有误情况
        for (String key : methodCCNs.keySet()) {
            boolean find = false;
            for (MethodNode methodNode : unMatchNodes) {
                if (isSameMethod(key,methodNode.getSignature())) {
                    int methodCCN = methodCCNs.get(key);
                    methodNode.setCcn(methodCCN);
                    fileCCN += methodCCN;
                    find = true;
                    break;
                }
            }
            if (!find) {
                log.warn("{} : can not find this method",key);
            }
        }
        fileNode.setCcn(fileCCN);
    }

    private boolean isSameMethod(String key, String signature) {
        String[] keyWords = key.replace(")","").split("\\(");
        String[] signatureWords = signature.replace(")","").split("\\(");
        if (keyWords.length == 0 || signatureWords.length == 0 || keyWords.length != signatureWords.length) {
            return false;
        }
        if (keyWords[0].equals(signatureWords[0])){
            if(keyWords.length == 1 && signatureWords.length == 1) {
                return true;
            }
            String[] args1 = keyWords[1].split(",");
            String[] args2 = signatureWords[1].split(",");
            if (args1.length == args2.length) {
                return true;
            }
        }
        return false;
    }

    private List<MethodNode> getMethodNodesFromFileNode(FileNode fileNode) {
        List<MethodNode> methodNodes = new ArrayList<>();
        for (BaseNode baseNode : fileNode.getChildren()) {
            for (BaseNode methodNode : baseNode.getChildren()) {
                methodNodes.add((MethodNode)methodNode);
            }
        }
        return methodNodes;
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