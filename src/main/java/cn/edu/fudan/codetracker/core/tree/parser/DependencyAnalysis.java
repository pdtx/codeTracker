package cn.edu.fudan.codetracker.core.tree.parser;

import cn.edu.fudan.codetracker.domain.projectinfo.MethodCallRelationship;
import cn.edu.fudan.codetracker.util.DirExplorer;
import cn.edu.fudan.codetracker.util.PomAnalysisUtil;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import lombok.SneakyThrows;
import org.apache.maven.model.Dependency;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
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

    private static String DEPENDENCY_PATH ;

    @Value("${mavenRepoDir}")
    public void setDependencyPath(String dependencyPath) {
        DEPENDENCY_PATH = dependencyPath;
    }

    /**
     * 每个线程对应的 依赖项目地址路径是固定的
     */
    private static ThreadLocal<String> repoPathT = new ThreadLocal<>();
    private static ThreadLocal<Set<String>> allGroupIdT = new ThreadLocal<>();
    private static ThreadLocal<CombinedTypeSolver> combinedTypeSolverT = new ThreadLocal<>();
    private static ThreadLocal<List<TypeSolver>>  typeSolverT = new ThreadLocal<>();

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
        Set<String> allGroupId = allGroupIdT.get();
        CombinedTypeSolver combinedTypeSolver = combinedTypeSolverT.get();

        List<MethodCallRelationship> result = new ArrayList<>(4);
        for (MethodCallExpr methodCallExpr : methodCallExprs) {
            try {
                // java.lang.OutOfMemoryError: Java heap space
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
        removeAll();

        List<TypeSolver> typeSolvers = new ArrayList<>(256);
        typeSolvers.add(new ReflectionTypeSolver());

        // JavaParserTypeSolver 必须要指定到项目的java目录下 如 E:\Lab\gitlab\codeTracker\src\main\java
        new DirExplorer((level, path, file) -> file.getAbsolutePath().endsWith("java"),
                (level, path, file) -> typeSolvers.add(new JavaParserTypeSolver(file))).exploreDir(new File(repoPath));

        // todo 可以根据pom中的变化来确定这次的依赖是否需要更新
        Set<Dependency> dependencies = PomAnalysisUtil.getAllDependencies(repoPath);
        new DirExplorer((level, path, file) -> path.endsWith(".jar") && containsJar(dependencies, path),
                (level, path, file) -> {
                    try {
                        typeSolvers.add(new JarTypeSolver(file));
                    } catch (Exception e){
                        // nothing to do
                    }
                }).explore(new File(DEPENDENCY_PATH));

        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        typeSolvers.forEach(combinedTypeSolver::add);

        repoPathT.set(repoPath);
        allGroupIdT.set(PomAnalysisUtil.getAllGroupId(repoPath));
        combinedTypeSolverT.set(combinedTypeSolver);
        typeSolverT.set(typeSolvers);
    }

    private static boolean containsJar(Set<Dependency> dependencies, String path) {
        path = path.replaceAll("/|\\\\",".");
        for (Dependency d: dependencies) {
            if (path.contains(d.getGroupId()) && path.contains(d.getArtifactId())) {
                return true;
            }
        }

        return false;
    }

    private static void removeAll() {
        repoPathT.remove();
        allGroupIdT.remove();

        List<TypeSolver> typeSolvers = typeSolverT.get();
        if(typeSolvers != null) {
            typeSolvers.forEach(t -> t = null);
        }
        typeSolverT.remove();

        CombinedTypeSolver combinedTypeSolver = combinedTypeSolverT.get();
        combinedTypeSolverT.remove();

        // GC
        typeSolvers = null;
        combinedTypeSolver = null;
    }

}