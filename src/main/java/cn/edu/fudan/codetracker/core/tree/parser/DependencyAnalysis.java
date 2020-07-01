package cn.edu.fudan.codetracker.core.tree.parser;

import cn.edu.fudan.codetracker.domain.projectinfo.MethodCallRelationship;
import cn.edu.fudan.codetracker.util.DirExplorer;
import cn.edu.fudan.codetracker.util.PomAnalysisUtil;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * description: 分析文件之间的调用关系
 *
 * @author fancying
 * create: 2020-06-18 15:30
 **/
@Component
public class DependencyAnalysis {


    private static String DEPENDENCY_PATH;
//    private static String DEPENDENCY_PATH = "/Users/tangyuan/Documents/codeTrackerRepo";

    @Value("${mavenRepoDir}")
    public void setDependencyPath(String dependencyPath) {
        DEPENDENCY_PATH = dependencyPath;
    }

    /**
     *每个线程对应的 依赖项目地址路径是固定的
     */
    private static ThreadLocal<String> repoPathT = new ThreadLocal<>();
    private static ThreadLocal<CombinedTypeSolver> combinedTypeSolverT = new ThreadLocal<>();
    private static ThreadLocal<Set<String>> allGroupIdT = new ThreadLocal<>();

    public static void main(String[] args) {
        test();
    }

    @SneakyThrows
    private static void test() {
//        String path = "E:\\Lab\\gitlab\\codeTracker\\src\\main\\java\\cn\\edu\\fudan\\codetracker\\core\\tree\\parser\\DependencyAnalysis.java";
        String path = "/Users/tangyuan/Documents/Git/IssueTracker-Master/account-service/src/main/java/cn/edu/fudan/accountservice/controller/AccountController.java";
//        String repoPath = "E:\\Lab\\gitlab\\codeTracker";
        String repoPath = "/Users/tangyuan/Documents/Git/IssueTracker-Master";
        setRepoPathT(repoPath);
        CompilationUnit cu = new JavaParser().parse(Paths.get(path)).getResult().get();
        List<MethodCallExpr> methodCallExprs = cu.findAll(MethodCallExpr.class);
        getMethodCallRelationship(methodCallExprs).forEach(System.out::println);
        System.out.println(getMethodCallRelationship(methodCallExprs).size());
    }

    /**
     * 判断是否属于本项目的依赖
     */
    private static boolean isOtherDependency(String qualifiedSignature, Set<String> allGroupId) {
        for (String s1 : allGroupId) {
            if (qualifiedSignature.contains(s1)) {
                return false;
            }
        }
        return true;
    }


    /**
     * fixme 当方法存在 非本项目的依赖时 会出现异常 无法解析
     * 得到当前线程或项目的 所有内部方法调用
     * @param methodCallExprs 通过 {@link com.github.javaparser.ast.Node#findAll} 找到的 {@link MethodCallExpr}
     * @return List<MethodCallRelationship>
     */
    @SneakyThrows
    public static List<MethodCallRelationship> getMethodCallRelationship(List<MethodCallExpr> methodCallExprs) {
        CombinedTypeSolver combinedTypeSolver = combinedTypeSolverT.get();
        Set<String> allGroupId = allGroupIdT.get();

        List<MethodCallRelationship> result = new ArrayList<>(4);
        for (MethodCallExpr methodCallExpr : methodCallExprs) {
            try {
                MethodUsage methodUsage = JavaParserFacade.get(combinedTypeSolver).solveMethodAsUsage(methodCallExpr);
                String methodName = methodUsage.getName();
                String qualifiedSignature = methodUsage.getQualifiedSignature();
                if (isOtherDependency(qualifiedSignature, allGroupId)) {
                    continue;
                }
                MethodDeclaration md = ((JavaParserMethodDeclaration)methodUsage.getDeclaration()).getWrappedNode();
                String signature = md.getSignature().toString();
                String qualifiedClassName = qualifiedSignature.split("." + methodName)[0];
                String className =  qualifiedClassName.substring(qualifiedClassName.lastIndexOf(".") + 1);
                String packageName = qualifiedClassName.replace("." + className, "");
                result.add(new MethodCallRelationship(packageName, className, signature));
            }catch (Exception e) {
                // todo 后续优化 目前无需做处理
                //e.printStackTrace();
            }
        }

        return result;
    }

    @SneakyThrows
    public static void setRepoPathT(String repoPath) {
        repoPathT.remove();
        combinedTypeSolverT.remove();
        allGroupIdT.remove();
        repoPathT.set(repoPath);
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());

        // JavaParserTypeSolver 必须要指定到项目的java目录下 如 E:\Lab\gitlab\codeTracker\src\main\java
        new DirExplorer((level, path, file) -> file.getAbsolutePath().endsWith("java"),
                (level, path, file) -> combinedTypeSolver.add(new JavaParserTypeSolver(file))).exploreDir(new File(repoPath));

        // 指定该项目所依赖的每一个jar   (level, path, file) -> combinedTypeSolver.add(new JarTypeSolver(file))
        // todo 后续可以根据 pom 文件缩小范围 或者全仓公用一个CombinedTypeSolver
        new DirExplorer((level, path, file) -> path.endsWith(".jar"),
                (level, path, file) -> {
                    try {
                        combinedTypeSolver.add(new JarTypeSolver(file));
                    } catch (Exception e){
                      // nothing to do
                    }
                }).explore(new File(DEPENDENCY_PATH));

        combinedTypeSolverT.set(combinedTypeSolver);
        allGroupIdT.set(PomAnalysisUtil.getAllGroupId(repoPath));
    }



}