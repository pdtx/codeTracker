/**
 * @description: extractor fundamental information of project structure base on a single file
 * @author: fancying
 * @create: 2019-05-25 12:02
 **/
package cn.edu.fudan.codetracker.util;

import cn.edu.fudan.codetracker.domain.projectinfo.*;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class FileInfoExtractor {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    private final String prefix = IS_WINDOWS ? "E:\\Lab\\" : "/Users/tangyuan/Desktop/demo/";

    private String projectName;
    private String moduleName;
    private String packageName;
    private Set<String> importNames;
    private String fileName;
    private String filePath;

    private FileInfo fileInfo;

    private CompilationUnit compilationUnit;
    private BaseInfo baseInfo;

    FileInfoExtractor(BaseInfo baseInfo, String path, String relativePath, String projectName) {
        this.projectName = projectName;
        this.baseInfo = baseInfo;
        importNames = new HashSet<>();
        try {
            // 根据操作系统修改
            compilationUnit = JavaParser.parse(Paths.get(path), Charset.forName("UTF-8"));
            parsePackageName(compilationUnit);
            String[] singleDir = relativePath.replace('\\','/').split("/");
            fileName = singleDir[singleDir.length - 1];
            // module name is null
            moduleName = parseModuleName(singleDir);
            //filePath = deletePrefix(path).replace('\\','/');
            String [] s = relativePath.replace('\\','/').split( moduleName + "/");
            filePath = moduleName + "/" + s[s.length - 1];
            fileInfo = new FileInfo(baseInfo, fileName, filePath, packageName, moduleName);
            // analyze import package
            List<ImportDeclaration> importDeclarations = compilationUnit.findAll(ImportDeclaration.class);
            for (ImportDeclaration importDeclaration :importDeclarations) {
                importNames.add(importDeclaration.getName().asString());
            }
        }catch (Exception e) {
            log.error("FileInfoExtractor :" + path);
            e.printStackTrace();
        }
    }

    private String deletePrefix(String path) {
        return  path.replace(prefix, "");
    }

    /**
     * need to be improved
     */
    private String parseModuleName(String[] singleDir) {
        for (int i = 1; i < singleDir.length; i++) {
            if ("src".equals(singleDir[i]) || "main".equals(singleDir[i]) ||  "java".equals(singleDir[i])) {
                return singleDir[i - 1];
            }
        }
        for (int i = 0; i < singleDir.length - 1; i++) {
            if (projectName.equals(singleDir[i])) {
                return singleDir[i + 1];
            }
        }
        return projectName;
    }

    private void parsePackageName(CompilationUnit cu) {
        if (cu.getPackageDeclaration().isPresent()) {
            packageName = cu.getPackageDeclaration().get().getName().asString();
        }
    }

    void parseClassInterface() {
        List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarationList = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
        List<ClassInfo> classInfos = new ArrayList<>(1);
        for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : classOrInterfaceDeclarationList) {
            List<String> extendNames = new ArrayList<>();
            List<String> implementedNames = new ArrayList<>();
            // 名字
            String classOrInterfaceName = classOrInterfaceDeclaration.getName().asString();
            // 修饰符
            StringBuilder modifiers = new StringBuilder();
            for (Modifier modifier : classOrInterfaceDeclaration.getModifiers()) {
                modifiers.append(modifier.asString());
                modifiers.append(" ");
            }
            // 扩展的类名
            for (ClassOrInterfaceType extendedType : classOrInterfaceDeclaration.getExtendedTypes()) {
                extendNames.add(extendedType.getName().asString());
            }
            // 实现的接口名
            for (ClassOrInterfaceType implementedType : classOrInterfaceDeclaration.getImplementedTypes()) {
                implementedNames.add(implementedType.asString());
            }
            // fullname 重新考虑
            String fullname = modifiers.toString() + classOrInterfaceDeclaration.getNameAsString();

            // BaseInfo baseInfo, FileInfo parent, String fullname, String className, String modifier, int begin, int end
            ClassInfo classInfo = new ClassInfo(baseInfo, fileInfo, fullname, classOrInterfaceName, modifiers.toString(), classOrInterfaceDeclaration.getBegin().get().line, classOrInterfaceDeclaration.getEnd().get().line);
            classInfo.setExtendedList(extendNames);
            classInfo.setImplementedList(implementedNames);

            classInfo.setFieldInfos(parseField(classOrInterfaceDeclaration.findAll(FieldDeclaration.class), classInfo ) );
            // 构造函数也属于函数
            List<MethodInfo> methodInfos = parseConstructors(classOrInterfaceDeclaration.findAll(ConstructorDeclaration.class), classInfo);
            methodInfos.addAll(parseMethod(classOrInterfaceDeclaration.findAll(MethodDeclaration.class), classInfo));
            classInfo.setMethodInfos(methodInfos);
            classInfo.setChildren(methodInfos);
            classInfos.add(classInfo);
        }
        fileInfo.setChildren(classInfos);
    }

    private List<FieldInfo> parseField(List<FieldDeclaration> fieldDeclarations, ClassInfo parent) {
        List<FieldInfo> fieldInfos = new ArrayList<>();

        for (FieldDeclaration fieldDeclaration : fieldDeclarations) {
            //modifier
            StringBuilder modifiers = new StringBuilder();
            for (Modifier modifier : fieldDeclaration.getModifiers()) {
                modifiers.append(modifier.asString());
                modifiers.append(" ");
            }

            StringBuilder simpleName = new StringBuilder();
            StringBuilder initValue = new StringBuilder();
            StringBuilder simpleType = new StringBuilder();
            for (VariableDeclarator variableDeclarator: fieldDeclaration.getVariables()) {
                simpleName.append(variableDeclarator.getName());
                simpleName.append(" ");
                if (variableDeclarator.getInitializer().isPresent()) {
                    initValue.append(variableDeclarator.getInitializer().get());
                    initValue.append(" ");
                }

                simpleType.append(variableDeclarator.getType());
                simpleType.append(" ");
            }
            //BaseInfo baseInfo, ClassInfo parent, String simpleName, String modifier, String simpleType, String initValue
            FieldInfo fieldInfo = new FieldInfo(baseInfo, parent, simpleName.toString(), modifiers.toString(), simpleType.toString(), initValue.toString());
            fieldInfo.setFullName(fieldDeclaration.toString());

            if (fieldDeclaration.getBegin().isPresent() && fieldDeclaration.getEnd().isPresent()) {
                fieldInfo.setBegin(fieldDeclaration.getBegin().get().line);
                fieldInfo.setEnd(fieldDeclaration.getEnd().get().line);
            }
            // field statement
            /*fieldInfo.setChildren(parseLevelOneStmt());*/
            fieldInfos.add(fieldInfo);
        }
        return fieldInfos;
    }

    private List<MethodInfo> parseConstructors(List<ConstructorDeclaration> constructorDeclarations, ClassInfo classInfo) {
        List<MethodInfo> methodInfos = new ArrayList<>(2);
        for (ConstructorDeclaration constructorDeclaration : constructorDeclarations) {
            StringBuilder modifiers  = new StringBuilder();
            //BaseInfo baseInfo, ClassInfo parent, String className, String classUuid
            MethodInfo conMethodInfo = new MethodInfo(baseInfo, classInfo);
            // modifier
            for (Modifier modifier : constructorDeclaration.getModifiers()) {
                modifiers.append(modifier.asString());
                modifiers.append(" ");
            }
            //fullname
            conMethodInfo.setFullname(constructorDeclaration.getDeclarationAsString(true,true,true));

            // signature
            conMethodInfo.setSignature(constructorDeclaration.getSignature().toString());
            if (constructorDeclaration.getRange().isPresent()) {
                conMethodInfo.setBegin(constructorDeclaration.getRange().get().begin.line);
                conMethodInfo.setBegin(constructorDeclaration.getRange().get().begin.line);
                conMethodInfo.setEnd(constructorDeclaration.getRange().get().end.line);
            }
            conMethodInfo.setModifier(modifiers.toString());
            //primitiveType
            conMethodInfo.setPrimitiveType(classInfo.getClassName());
            conMethodInfo.setContent(constructorDeclaration.getBody().toString());
            //statementInfo
            conMethodInfo.setChildren(parseLevelOneStmt(constructorDeclaration.getBody(), conMethodInfo));
            methodInfos.add(conMethodInfo);
        }

        return methodInfos;
    }

    private List<MethodInfo> parseMethod(List<MethodDeclaration> methodDeclarations, ClassInfo classInfo) {
        List<MethodInfo> methodInfos = new ArrayList<>();

        for (MethodDeclaration methodDeclaration : methodDeclarations) {
            // BaseInfo baseInfo, ClassInfo parent, String className, String classUuid
            MethodInfo methodInfo = new MethodInfo(baseInfo, classInfo);
            StringBuilder m = new StringBuilder();
            // modifier
            for (Modifier modifier : methodDeclaration.getModifiers()) {
                m.append(modifier.asString());
                m.append(" ");
            }
            //simpleName
            methodInfo.setSimpleName(methodDeclaration.getNameAsString());

            // signature
            methodInfo.setSignature(methodDeclaration.getSignature().toString());

            methodInfo.setBegin(methodDeclaration.getRange().get().begin.line);
            methodInfo.setEnd(methodDeclaration.getRange().get().end.line);
            methodInfo.setModifier(m.toString());
            //primitiveType
            methodInfo.setPrimitiveType(methodDeclaration.getType().asString());
            methodInfo.setFullname(methodDeclaration.getDeclarationAsString(true,true,true));
            if (methodInfo.getFullname().length() > 4096) {
                log.warn("methodInfo fullname is too long, fullname:{}, length:{}", methodInfo.getFullname(), methodInfo.getFullname().length());
            }

            if (methodDeclaration.getTokenRange().isPresent()) {
                methodInfo.setContent(methodDeclaration.getTokenRange().get().toString());
            }
            //statementInfo
            if (methodDeclaration.getBody().isPresent()) {
                methodInfo.setChildren(parseLevelOneStmt(methodDeclaration.getBody().get(), methodInfo));
            }
            methodInfos.add(methodInfo);
        }
        return methodInfos;
    }

    /**
     *  statement
     * */

    private List<StatementInfo> parseLevelOneStmt(BlockStmt blockStmt, MethodInfo methodInfo) {
        List<StatementInfo> statementInfos = new ArrayList<>();
        // blockStatement expressionStatement
        List<Statement>  statementList = blockStmt.getStatements();
        int sequence = 0;
        for (Statement statement : statementList) {
            // BaseInfo baseInfo, BaseInfo parent, String body, int begin, int end, String methodUuid
            if (statement.getBegin().isPresent() &&  statement.getEnd().isPresent() && statement.getTokenRange().isPresent()) {
                String body = statement.getTokenRange().get().toString();
                StatementInfo statementInfo = new StatementInfo(baseInfo, methodInfo, body, statement.getBegin().get().line, statement.getEnd().get().line, methodInfo.getUuid());
                statementInfo.setSequence(++sequence);
                statementInfo.setChildren(parseLevelTwoStmt(statement, methodInfo, statementInfo));
                statementInfos.add(statementInfo);
            }
        }
        return statementInfos;
    }

    private List<StatementInfo> parseLevelTwoStmt(Statement parentStmt, MethodInfo methodInfo, StatementInfo parent) {
        List<StatementInfo> statementInfos = new ArrayList<>();
        int sequence = 0;
        for (Node node : parentStmt.getChildNodes()) {
            if (node.findFirst(Statement.class).isPresent()) {
                Statement statement = node.findFirst(Statement.class).get();
                if ((statement.getTokenRange().isPresent() && statement.getBegin().isPresent() && statement.getEnd().isPresent()) ){
                    StatementInfo statementInfo = new StatementInfo(baseInfo, parent,  statement.getTokenRange().get().toString(), statement.getBegin().get().line, statement.getEnd().get().line, methodInfo.getUuid());
                    statementInfo.setSequence(++sequence);
                    statementInfo.setChildren(parseLevelTwoStmt(statement, methodInfo, statementInfo));
                    statementInfos.add(statementInfo);
                }
            }
        }
        return statementInfos;
    }

    /**
     * getter and setter
     * */
    public String getPackageName() {
        return packageName;
    }

    public Set<String> getImportNames() {
        return importNames;
    }

    public String getFileName() {
        return fileName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }


}