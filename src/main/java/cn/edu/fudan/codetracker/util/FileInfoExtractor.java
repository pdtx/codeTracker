/**
 * @description: extractor fundamental information of project structure base on a single file
 * @author: fancying
 * @create: 2019-05-25 12:02
 **/
package cn.edu.fudan.codetracker.util;

import cn.edu.fudan.codetracker.domain.projectInfo.*;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;


public class FileInfoExtractor {

    private final String PREFIX="E:\\Lab\\project\\";

    private String projectName;
    private String moduleName;
    private String packageName;
    private Set<String> importNames;
    private String fileName;

    private FileInfo fileInfo;
    private List<ClassInfo> classInfos;
    private List<FieldInfo> fieldInfos;
    private List<MethodInfo> methodInfos;

    private CompilationUnit compilationUnit;

    public FileInfoExtractor(String path, String projectName) {
        this.projectName = projectName;
        importNames = new HashSet<>();
        classInfos = new ArrayList<>();
        fieldInfos = new ArrayList<>();
        methodInfos = new ArrayList<>();
        try {
            compilationUnit = JavaParser.parse(new File(path));
            parsePackageName(compilationUnit);
            String[] singleDir = path.replace('\\','/').split("/");
            fileName = singleDir[singleDir.length - 1];
            // module Name 为 null
            moduleName = parseModuleName(singleDir);

            // 后面需要根据packageName 与 moduleName 找到packageUUID
            fileInfo = new FileInfo(fileName, packageName, moduleName, deletePrefix(path));

            // import 包解析
            List<ImportDeclaration> importDeclarations = compilationUnit.findAll(ImportDeclaration.class);
            for (ImportDeclaration importDeclaration :importDeclarations) {
                importNames.add(importDeclaration.getName().asString());
            }

        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String deletePrefix(String path) {
        return  path.substring(path.indexOf(PREFIX) + 1);
    }

    // 有待改进
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

    public void parseClassInterface() {

        List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarationList = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
        for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : classOrInterfaceDeclarationList) {
            //ResolvedReferenceTypeDeclaration resolvedReferenceTypeDeclaration = classOrInterfaceDeclaration.resolve();

            List<String> extendNames = new ArrayList<>();
            List<String> implementedNames = new ArrayList<>();

            // 名字
            String classOrInterfaceName = classOrInterfaceDeclaration.getName().asString();
            // 修饰符
            StringBuilder sb = new StringBuilder();
            for (Modifier modifier : classOrInterfaceDeclaration.getModifiers()) {
                sb.append(modifier.asString());
                sb.append(" ");
            }
            // 扩展的类名
            for (ClassOrInterfaceType extendedType : classOrInterfaceDeclaration.getExtendedTypes()) {
                extendNames.add(extendedType.getName().asString());
            }
            // 实现的接口名
            for (ClassOrInterfaceType implementedType : classOrInterfaceDeclaration.getImplementedTypes()) {
                implementedNames.add(implementedType.asString());
            }
            // ？？？fullname 重新考虑
            String fullname = classOrInterfaceDeclaration.getNameAsString();
            ClassInfo classInfo = new ClassInfo(fullname, classOrInterfaceName, packageName, moduleName, fileInfo.getUuid(),
                    sb.toString(), classOrInterfaceDeclaration.getBegin().get().line, classOrInterfaceDeclaration.getEnd().get().line);

            classInfo.setExtendedList(extendNames);
            classInfo.setImplementedList(implementedNames);
            classInfo.setFiledInfos(parseField(classOrInterfaceDeclaration.findAll(FieldDeclaration.class), classInfo.getUuid()));
            classInfo.setMethodInfos(parseMethod(classOrInterfaceDeclaration.findAll(MethodDeclaration.class), classInfo));

            // 解析构造函数
            //classInfo.getMethodInfos().addAll(parseConstructor(classOrInterfaceDeclaration.findAll(ConstructorDeclaration.class)));


            classInfos.add(classInfo);
            fieldInfos.addAll(classInfo.getFiledInfos());
            methodInfos.addAll(classInfo.getMethodInfos());
        }
    }

    private Collection<? extends MethodInfo> parseConstructor(List<ConstructorDeclaration> all) {
        return null;
    }

    private List<FieldInfo> parseField(List<FieldDeclaration> fieldDeclarations, String classUuid) {
        List<FieldInfo> fieldInfos = new ArrayList<>();

        for (FieldDeclaration fieldDeclaration : fieldDeclarations) {
            //modifier
            StringBuilder sb = new StringBuilder();
            for (Modifier modifier : fieldDeclaration.getModifiers()) {
                sb.append(modifier.asString());
                sb.append(" ");
            }
            FieldInfo fieldInfo = new FieldInfo(fieldDeclaration.toString(), sb.toString(), fieldDeclaration.getElementType().asString(), classUuid);
            fieldInfos.add(fieldInfo);
        }
        return fieldInfos;
    }

    private List<MethodInfo> parseMethod(List<MethodDeclaration> methodDeclarations, ClassInfo classInfo) {
        List<MethodInfo> methodInfos = new ArrayList<>();

        for (MethodDeclaration methodDeclaration : methodDeclarations) {
            StringBuilder sb  = new StringBuilder();
            MethodInfo methodInfo = new MethodInfo(classInfo.getClassName(), classInfo.getUuid(), fileName, packageName, fileInfo.getpackageUuid(), moduleName);

            //fullname
            methodInfo.setFullname(methodDeclaration.getNameAsString());


            sb.append(methodDeclaration.getNameAsString());
            // parameters
            for (Parameter parameter : methodDeclaration.getParameters()) {
                sb.append(" ");
                sb.append(parameter.toString());
            }

            // signature
            methodInfo.setSignature(sb.toString());

            // modifier
            sb.setLength(0);
            for (Modifier modifier : methodDeclaration.getModifiers()) {
                sb.append(modifier.asString());
                sb.append(" ");
            }

            methodInfo.setBegin(methodDeclaration.getRange().get().begin.line);
            methodInfo.setEnd(methodDeclaration.getRange().get().end.line);
            methodInfo.setModifier(sb.toString());
            //primitiveType
            methodInfo.setPrimitiveType(methodDeclaration.getType().asString());

            //statementInfo
            //methodInfo.setStatementInfo(parseStmt(methodDeclaration.getBody()));

            methodInfos.add(methodInfo);
        }
        return methodInfos;
    }

    /**
     *  statement 有待商议
     * */
    private List<StatementInfo> parseStmt(Optional<BlockStmt> body) {
        List<StatementInfo> statements = new ArrayList<>();
        if (body.isPresent()) {
            BlockStmt blockStmt = body.get();
            List<Statement>  statementList = blockStmt.findAll(Statement.class);
            for (Statement statement : statementList) {
                statements.add(new StatementInfo(UUID.randomUUID().toString()  ,statement.toString(),
                        statement.getRange().get().begin.line, statement.getRange().get().end.line));
            }
        }
        return statements;
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

    public List<ClassInfo> getClassInfos() {
        return classInfos;
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

    public List<FieldInfo> getFieldInfos() {
        return fieldInfos;
    }

    public List<MethodInfo> getMethodInfos() {
        return methodInfos;
    }
}