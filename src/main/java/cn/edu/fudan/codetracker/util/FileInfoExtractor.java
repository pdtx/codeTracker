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

    private FileNode fileNode;

    private CompilationUnit compilationUnit;

    FileInfoExtractor(String path, String relativePath, String projectName) {
        this.projectName = projectName;
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
            fileNode = new FileNode(fileName, filePath);
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
        List<ClassNode> classInfos = new ArrayList<>(1);
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

            //String fullName, String className, String modifier, int begin, int end
            ClassNode classNode = new ClassNode(fullname, classOrInterfaceName, modifiers.toString(), classOrInterfaceDeclaration.getBegin().get().line, classOrInterfaceDeclaration.getEnd().get().line);
            classNode.setParent(fileNode);
            classNode.setExtendedList(extendNames);
            classNode.setImplementedList(implementedNames);

            classNode.setFieldNodes(parseField(classOrInterfaceDeclaration.findAll(FieldDeclaration.class), classNode));
            // 构造函数也属于函数
            List<MethodNode> methodInfos = parseConstructors(classOrInterfaceDeclaration.findAll(ConstructorDeclaration.class), classNode);
            methodInfos.addAll(parseMethod(classOrInterfaceDeclaration.findAll(MethodDeclaration.class), classNode));
            classNode.setChildren(methodInfos);
            classInfos.add(classNode);
        }
        fileNode.setChildren(classInfos);
    }

    private List<FieldNode> parseField(List<FieldDeclaration> fieldDeclarations, ClassNode classNode) {
        List<FieldNode> fieldInfos = new ArrayList<>();

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

            //String simpleName, String modifier, String simpleType, String initValue
            FieldNode fieldNode = new FieldNode(simpleName.toString(), modifiers.toString(), simpleType.toString(), initValue.toString());
            fieldNode.setParent(classNode);
            fieldNode.setFilePath(classNode.getFilePath());
            fieldNode.setFullName(fieldDeclaration.toString());

            if (fieldDeclaration.getBegin().isPresent() && fieldDeclaration.getEnd().isPresent()) {
                fieldNode.setBegin(fieldDeclaration.getBegin().get().line);
                fieldNode.setEnd(fieldDeclaration.getEnd().get().line);
            }
            // field statement
            /*fieldInfo.setChildren(parseLevelOneStmt());*/
            fieldInfos.add(fieldNode);
        }
        return fieldInfos;
    }

    private List<MethodNode> parseConstructors(List<ConstructorDeclaration> constructorDeclarations, ClassNode classNode) {
        List<MethodNode> methodInfos = new ArrayList<>(2);
        for (ConstructorDeclaration constructorDeclaration : constructorDeclarations) {
            StringBuilder modifiers  = new StringBuilder();
            MethodNode conMethodNode = new MethodNode();
            // modifier
            for (Modifier modifier : constructorDeclaration.getModifiers()) {
                modifiers.append(modifier.asString());
                modifiers.append(" ");
            }
            //fullname
            conMethodNode.setFullName(constructorDeclaration.getDeclarationAsString(true,true,true));

            // signature
            conMethodNode.setSignature(constructorDeclaration.getSignature().toString());
            if (constructorDeclaration.getRange().isPresent()) {
                conMethodNode.setBegin(constructorDeclaration.getRange().get().begin.line);
                conMethodNode.setEnd(constructorDeclaration.getRange().get().end.line);
            }
            conMethodNode.setModifier(modifiers.toString());
            //primitiveType
            conMethodNode.setPrimitiveType(classNode.getClassName());
            conMethodNode.setContent(constructorDeclaration.getBody().toString());
            //statementInfo
            conMethodNode.setParent(classNode);
            conMethodNode.setFilePath(classNode.getFilePath());
            conMethodNode.setPackageName(classNode.getPackageName());
            conMethodNode.setChildren(parseLevelOneStmt(constructorDeclaration.getBody(), conMethodNode));
            methodInfos.add(conMethodNode);
        }

        return methodInfos;
    }

    private List<MethodNode> parseMethod(List<MethodDeclaration> methodDeclarations, ClassNode classNode) {
        List<MethodNode> methodInfos = new ArrayList<>();

        for (MethodDeclaration methodDeclaration : methodDeclarations) {
            // BaseInfo baseInfo, ClassInfo parent, String className, String classUuid
            MethodNode methodNode = new MethodNode();
            StringBuilder m = new StringBuilder();
            // modifier
            for (Modifier modifier : methodDeclaration.getModifiers()) {
                m.append(modifier.asString());
                m.append(" ");
            }

            methodNode.setParent(classNode);
            methodNode.setFilePath(classNode.getFilePath());
            methodNode.setPackageName(classNode.getPackageName());

            // signature
            methodNode.setSignature(methodDeclaration.getSignature().toString());

            methodNode.setBegin(methodDeclaration.getRange().get().begin.line);
            methodNode.setEnd(methodDeclaration.getRange().get().end.line);
            methodNode.setModifier(m.toString());
            //primitiveType
            methodNode.setPrimitiveType(methodDeclaration.getType().asString());
            methodNode.setFullName(methodDeclaration.getDeclarationAsString(true,true,true));

            if (methodDeclaration.getTokenRange().isPresent()) {
                methodNode.setContent(methodDeclaration.getTokenRange().get().toString());
            }
            //statementInfo
            if (methodDeclaration.getBody().isPresent()) {
                methodNode.setChildren(parseLevelOneStmt(methodDeclaration.getBody().get(), methodNode));
            }
            methodInfos.add(methodNode);
        }
        return methodInfos;
    }

    /**
     *  statement
     * */

    private List<StatementNode> parseLevelOneStmt(BlockStmt blockStmt, MethodNode methodNode) {
        List<StatementNode> statementInfos = new ArrayList<>();
        // blockStatement expressionStatement
        List<Statement>  statementList = blockStmt.getStatements();
        int sequence = 0;
        for (Statement statement : statementList) {
            //String body, int begin, int end
            if (statement.getBegin().isPresent() &&  statement.getEnd().isPresent() && statement.getTokenRange().isPresent()) {
                String body = statement.getTokenRange().get().toString();
                StatementNode statementNode = new StatementNode(body, statement.getBegin().get().line, statement.getEnd().get().line);
                statementNode.setSequence(++sequence);
                statementNode.setParent(methodNode);
                statementNode.setChildren(parseLevelTwoStmt(statement, methodNode, statementNode));
                statementInfos.add(statementNode);
            }
        }
        return statementInfos;
    }

    private List<StatementNode> parseLevelTwoStmt(Statement parentStmt, MethodNode methodNode, StatementNode parent) {
        List<StatementNode> statementInfos = new ArrayList<>();
        int sequence = 0;
        for (Node node : parentStmt.getChildNodes()) {
            if (node.findFirst(Statement.class).isPresent()) {
                Statement statement = node.findFirst(Statement.class).get();
                if ((statement.getTokenRange().isPresent() && statement.getBegin().isPresent() && statement.getEnd().isPresent()) ){
                    StatementNode statementNode = new StatementNode(statement.getTokenRange().get().toString(), statement.getBegin().get().line, statement.getEnd().get().line);
                    statementNode.setSequence(++sequence);
                    statementNode.setParent(parent);
                    statementNode.setChildren(parseLevelTwoStmt(statement, methodNode, statementNode));
                    statementInfos.add(statementNode);
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

    public FileNode getFileNode() {
        return fileNode;
    }

}