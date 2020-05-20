package cn.edu.fudan.codetracker.core.tree.parser;

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
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * description: java 语言解析
 *
 * @author fancying
 * create: 2020-05-17 15:15
 **/
@Slf4j
@Getter
@Setter
public class JavaFileParser implements FileParser {

    private String projectName;
    private String moduleName;
    private String packageName;
    private String fileName;
    private String filePath;
    private FileNode fileNode;
    private Set<String> importNames;
    private CompilationUnit compilationUnit;
    private ClassOrInterfaceDeclaration classOrInterfaceDeclaration;
    private BlockStmt blockStmt;
    private Statement parentStmt;

    public JavaFileParser() {
        importNames = new HashSet<>();
    }

    @Override
    public void parse(String path, String relativePath, String projectName) {
        this.projectName = projectName;
        try {
            // 根据操作系统修改
            compilationUnit = JavaParser.parse(Paths.get(path), Charset.forName("UTF-8"));
            packageName = parsePackageName();
            String[] singleDir = relativePath.replace('\\','/').split("/");
            fileName = singleDir[singleDir.length - 1];
            // module name is null
            moduleName = parseModuleName(singleDir);
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

    public String parseModuleName(String[] singleDir) {
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

    public String parsePackageName() {
        if (compilationUnit.getPackageDeclaration().isPresent()) {
            packageName = compilationUnit.getPackageDeclaration().get().getName().asString();
        }
        return packageName;
    }

    public void parseClassOrInterface(){
        List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarationList = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
        List<ClassNode> classInfos = new ArrayList<>(1);
        for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : classOrInterfaceDeclarationList) {
            this.classOrInterfaceDeclaration = classOrInterfaceDeclaration;
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

            classNode.setFieldNodes(parseField(classNode));
            // 构造函数也属于函数
            List<MethodNode> methodInfos = parseConstructors(classNode);
            methodInfos.addAll(parseMethod(classNode));
            classNode.setChildren(methodInfos);
            classInfos.add(classNode);
        }
        fileNode.setChildren(classInfos);
    }

    public List<FieldNode> parseField(ClassNode classNode){
        List<FieldNode> fieldInfos = new ArrayList<>();
        List<FieldDeclaration> fieldDeclarations = classOrInterfaceDeclaration.findAll(FieldDeclaration.class);
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

    public List<MethodNode> parseConstructors(ClassNode classNode){
        List<MethodNode> methodInfos = new ArrayList<>(2);
        List<ConstructorDeclaration> constructorDeclarations = classOrInterfaceDeclaration.findAll(ConstructorDeclaration.class);
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
            blockStmt = constructorDeclaration.getBody();
            conMethodNode.setChildren(parseLevelOneStmt(conMethodNode));
            methodInfos.add(conMethodNode);
        }

        return methodInfos;
    }

    public List<MethodNode> parseMethod(ClassNode classNode){
        List<MethodNode> methodInfos = new ArrayList<>();
        List<MethodDeclaration> methodDeclarations = classOrInterfaceDeclaration.findAll(MethodDeclaration.class);
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
                blockStmt = methodDeclaration.getBody().get();
                methodNode.setChildren(parseLevelOneStmt(methodNode));
            }
            methodInfos.add(methodNode);
        }
        return methodInfos;
    }

    public List<StatementNode> parseLevelOneStmt(MethodNode methodNode){
        List<StatementNode> statementInfos = new ArrayList<>();
        // blockStatement expressionStatement
        List<Statement>  statementList = blockStmt.getStatements();
        int sequence = 0;
        for (Statement statement : statementList) {
            //String body, int begin, int end
            if (statement.getBegin().isPresent() &&  statement.getEnd().isPresent() && statement.getTokenRange().isPresent()) {
                String body = statement.getTokenRange().get().toString();
                StatementNode statementNode = new StatementNode(body, statement.getBegin().get().line, statement.getEnd().get().line);
                statementNode.setLevel(methodNode.getProjectInfoLevel().getLevel()+1);
                statementNode.setSequence(++sequence);
                statementNode.setParent(methodNode);
                //先设methodUuid为raw_method_uuid，在mapping时改为meta_method_uuid
                statementNode.setMethodUuid(methodNode.getUuid());
                parentStmt = statement;
                statementNode.setChildren(parseLevelTwoStmt(methodNode, statementNode));
                statementInfos.add(statementNode);
            }
        }
        return statementInfos;
    }

    public List<StatementNode> parseLevelTwoStmt(MethodNode methodNode, StatementNode parent){
        List<StatementNode> statementInfos = new ArrayList<>();
        Statement parentStmt = this.parentStmt;
        int sequence = 0;
        for (Node node : parentStmt.getChildNodes()) {
            if (node.findFirst(Statement.class).isPresent()) {
                Statement statement = node.findFirst(Statement.class).get();
                if ((statement.getTokenRange().isPresent() && statement.getBegin().isPresent() && statement.getEnd().isPresent()) ){
                    StatementNode statementNode = new StatementNode(statement.getTokenRange().get().toString(), statement.getBegin().get().line, statement.getEnd().get().line);
                    statementNode.setLevel(parent.getLevel()+1);
                    statementNode.setSequence(++sequence);
                    statementNode.setParent(parent);
                    //先设methodUuid为raw_method_uuid，在mapping时改为meta_method_uuid
                    statementNode.setMethodUuid(methodNode.getUuid());
                    this.parentStmt = statement;
                    statementNode.setChildren(parseLevelTwoStmt(methodNode, statementNode));
                    statementInfos.add(statementNode);
                }
            }
        }
        return statementInfos;
    }

}