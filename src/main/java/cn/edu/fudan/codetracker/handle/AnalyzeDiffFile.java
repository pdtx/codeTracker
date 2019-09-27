/**
 * @description:
 * @author: fancying
 * @create: 2019-06-06 16:41
 **/
package cn.edu.fudan.codetracker.handle;

import cn.edu.fudan.RepoInfoBuilder;
import cn.edu.fudan.codetracker.domain.projectInfo.ClassInfo;
import cn.edu.fudan.codetracker.util.RepoInfoBuilder;
import cn.edu.fudan.projectInfo.*;
import cn.edu.fudan.util.Extractor;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AnalyzeDiffFile {

    private String projectName;
    private String curCommit;
    private String preCommit;
    private String curCommitter;
    private String preCommitter;

    // moduleName , packageName Set
    private Map<String, Set<String>> changedModulePackageMap;
    private RepoInfoBuilder preProjectInfo;
    private RepoInfoBuilder curProjectInfo;

    private Set<String> cqlSet;

    public AnalyzeDiffFile(RepoInfoBuilder preProjectInfo, RepoInfoBuilder curProjectInfo) {
        this.preProjectInfo = preProjectInfo;
        this.curProjectInfo = curProjectInfo;
        projectName = preProjectInfo.getProjectName();

        curCommit = curProjectInfo.getCommit();
        curCommitter = curProjectInfo.getCommitter();

        preCommitter = preProjectInfo.getCommitter();
        preCommit = preProjectInfo.getCommit();
        cqlSet = new HashSet<>();
        changedModulePackageMap = new HashMap<>();
    }

    public void addInfoConstruction(List<String> addFilesList) {
        String cql;
        Set<String> packageSet = new HashSet<>();
        RepoInfoBuilder newProjectInfo = new RepoInfoBuilder(projectName, curCommit, curCommitter, addFilesList);
        for (Map.Entry<String, Map<String, PackageInfo>> newModuleInfoEntry : newProjectInfo.getModuleInfos().entrySet()) {
            // 非新增的module 的情况
            if (preProjectInfo.getModuleInfos().containsKey(newModuleInfoEntry.getKey())) {
                for (String newPackageInfo : newModuleInfoEntry.getValue().keySet()) {
                    // 非新增package的情况 意味着package为change
                    if (preProjectInfo.getModuleInfos().get(newModuleInfoEntry.getKey()).keySet().contains(newPackageInfo)) {
                        cql = "MATCH (p:package:`" + projectName + "`:`" + preCommit + "`:`" + newModuleInfoEntry.getKey() +"`),(c:package:`" +
                                projectName + "`:`" + curCommit + "`:`" + newModuleInfoEntry.getKey() +
                                "`)where p.packageName = \""+ newPackageInfo + "\" AND c.packageName = \"" + newPackageInfo +
                                "\" CREATE (p)-[r:change{}]->(c)" ;
                        cqlSet.add(cql);
                        packageSet.add(newPackageInfo);
                    } else {
                        // 新增package的情况
                        cql = "MATCH (c:package:`" + projectName + "`:`" + curCommit + "`:`" + newModuleInfoEntry.getKey() +
                                "`) where c.packageName = \"" + newPackageInfo +
                                "\" set c:add" ;
                        cqlSet.add(cql);
                        // package 所包含的内容都为新增
                        cql = "MATCH (c:`" + projectName + "`:`" + curCommit + "`:`" + newModuleInfoEntry.getKey() +
                                "`:`" + newPackageInfo+ "`)  set c:add" ;
                        cqlSet.add(cql);
                    }
                }
            } else {
                // 新增module 的情况
                cql = "MATCH (c:`" + projectName + "`:`" + curCommit + "`:`" + newModuleInfoEntry.getKey() +
                        "`) set c:add" ;
                cqlSet.add(cql);
            }
            if (packageSet.size() != 0) {
                changedModulePackageMap.put(newModuleInfoEntry.getKey(), new HashSet<>(packageSet));
                packageSet.clear();
            }
        }

        for (PackageInfo packageInfo : newProjectInfo.getPackageInfos()) {
            for (ClassInfo classInfo : packageInfo.getClassInfos()) {
                // 添加class级别的 add标签
                cql = "MATCH (c:class:`" + projectName + "`:`" + curCommit + "`:`" + packageInfo.getModuleName() +
                        "`:`" + packageInfo.getPackageName() + "`) where c.className = \"" + classInfo.getClassName() +
                        "\" set c:add" ;
                cqlSet.add(cql);
                // 添加class 级别之下 add的标签
                cql = "MATCH (p:`" + projectName + "`:`" + curCommit + "`:`" + packageInfo.getModuleName() + "`:`" +
                        packageInfo.getPackageName() + "`:`" + classInfo.getClassName() +
                        "`) set p:add" ;
                cqlSet.add(cql);
            }
        }
    }

    // delete： 有一个新增的 delete label 表示在此commit的下一个commit上被删除
    // 可考虑增加一个 deleteAt 属性 表示下一个commit
    public void deleteInfoConstruction(List<String> deleteFilesList) {
        String cql;
        Set<String> packageSet = new HashSet<>();
        // 这里应该是preCommitter 上一个版本中delete的信息
        RepoInfoBuilder deleteProjectInfo = new RepoInfoBuilder(projectName, preCommit, preCommitter, deleteFilesList);
        for (Map.Entry<String, Map<String, PackageInfo>> deleteModuleInfoEntry : deleteProjectInfo.getModuleInfos().entrySet()) {
            // 非删除整个的module 的情况
            if (curProjectInfo.getModuleInfos().containsKey(deleteModuleInfoEntry.getKey())) {
                for (String deletePackageName : deleteModuleInfoEntry.getValue().keySet()) {
                    // 非删除package的情况 意味着package为change
                    if (curProjectInfo.getModuleInfos().get(deleteModuleInfoEntry.getKey()).keySet().contains(deletePackageName)) {
                        cql = "MATCH (p:package:`" + projectName + "`:`" + preCommit + "`:`" + deleteModuleInfoEntry.getKey() +"`),(c:package:`" +
                                projectName + "`:`" + curCommit + "`:`" + deleteModuleInfoEntry.getKey() +
                                "`)where p.packageName = \""+ deletePackageName + "\" AND c.packageName = \"" + deletePackageName +
                                "\" CREATE (p)-[r:change{}]->(c)" ;
                        cqlSet.add(cql);
                        packageSet.add(deletePackageName);
                    } else {
                        // 删除package的情况
                        cql = "MATCH (c:package:`" + projectName + "`:`" + preCommit + "`:`" + deleteModuleInfoEntry.getKey() +
                                "`) where c.packageName = \"" + deletePackageName +
                                "\" set c:delete" ;
                        cqlSet.add(cql);
                        // package 所包含的内容都为删除
                        cql = "MATCH (c:`" + projectName + "`:`" + preCommit + "`:`" + deleteModuleInfoEntry.getKey() +
                                "`:`" + deletePackageName+ "`)  set c:delete" ;
                        cqlSet.add(cql);
                    }
                }
            } else {
                // 删除module 的情况
                cql = "MATCH (c:`" + projectName + "`:`" + preCommit + "`:`" + deleteModuleInfoEntry.getKey() +
                        "`) set c:delete" ;
                cqlSet.add(cql);
            }
            if (packageSet.size() != 0) {
                if (changedModulePackageMap.containsKey(deleteModuleInfoEntry.getKey())) {
                    packageSet.addAll(changedModulePackageMap.get(deleteModuleInfoEntry.getKey()));
                }
                changedModulePackageMap.put(deleteModuleInfoEntry.getKey(), new HashSet<>(packageSet));
                packageSet.clear();
            }
        }

        for (PackageInfo packageInfo : deleteProjectInfo.getPackageInfos()) {
            for (ClassInfo classInfo : packageInfo.getClassInfos()) {
                // 添加class级别的 delete标签
                cql = "MATCH (c:class:`" + projectName + "`:`" + preCommit + "`:`" + packageInfo.getModuleName() +
                        "`:`" + packageInfo.getPackageName() + "`) where c.className = \"" + classInfo.getClassName() +
                        "\" set c:delete" ;
                cqlSet.add(cql);
                // 添加class 级别之下 delete的标签
                cql = "MATCH (p:`" + projectName + "`:`" + preCommit + "`:`" + packageInfo.getModuleName() + "`:`" +
                        packageInfo.getPackageName() + "`:`" + classInfo.getClassName() +
                        "`) set p:delete" ;
                cqlSet.add(cql);
            }
        }

/*        Set<PackageInfo> deleteRecord = new HashSet<>();
        // 查找是否整个package 都被 删除
        // delete 部分在上一个节点中添加新的label
        for (PackageInfo deletePackageInfo : deleteProjectInfo.getPackageInfos()) {
            PackageInfo prePackageInfo = findPackageInfoByModuleNameAndPackageName( preProjectInfo.getPackageInfos(),deletePackageInfo);
            if (prePackageInfo.getClassInfos().size() == deletePackageInfo.getClassInfos().size()) {
                cql = "MATCH (p:package:`" + projectName + "`:`" + preCommit + "`:`" + deletePackageInfo.getModuleName() +
                        "`) where p.packageName =\"" + prePackageInfo.getPackageName() +
                        "\" set p:delete;" ;
                cqlSet.add(cql);
                cql = "MATCH (p:`" + projectName + "`:`" + preCommit + "`:`" + deletePackageInfo.getModuleName() + "`:`" +
                        deletePackageInfo.getPackageName() +
                        "`)set p:delete;" ;
                cqlSet.add(cql);
                // 表示package 被删除 记录被删除的package
                deleteRecord.add(deletePackageInfo);
            } else {
                for (ClassInfo classInfo : deletePackageInfo.getClassInfos()) {
                    // 添加class级别的 delete标签
                    cql = "MATCH (c:class:`" + projectName + "`:`" + preCommit + "`:`" + deletePackageInfo.getModuleName() +
                            "`:`" + deletePackageInfo.getPackageName() + "`) where c.className = \"" + classInfo.getClassName() +
                            "\" set c:delete" ;
                    cqlSet.add(cql);
                    // 添加class 级别之下 delete的标签
                    cql = "MATCH (c:`" + projectName + "`:`" + preCommit + "`:`" + deletePackageInfo.getModuleName() + "`:`" +
                            deletePackageInfo.getPackageName() + "`:`" + classInfo.getClassName() +
                            "`) set c:delete;" ;
                    cqlSet.add(cql);
                }
            }
        }
        // 被刪除的package 不放在屬於change 範疇
        deleteProjectInfo.getPackageInfos().removeAll(deleteRecord);
        changedModulePackageMap.addAll(deleteProjectInfo.getPackageInfos());*/
    }

    // 分析单个文件
    public void modifyInfoConstruction(List<String> prePathList, List<String> curPathList, List<String> diffPathList) {
        Set<String> packageSet ;
        String cql;
        for (int i = 0; i < prePathList.size(); i++) {
            Extractor preInfo = new Extractor(prePathList.get(i), projectName);
            Extractor curInfo = new Extractor(curPathList.get(i), projectName);
            if (changedModulePackageMap.containsKey(preInfo.getModuleName())) {
                if (! changedModulePackageMap.get(preInfo.getModuleName()).contains(preInfo.getPackageName())) {
                    cql = "MATCH (p:package:`" + projectName + "`:`" + preCommit + "`:`" + preInfo.getModuleName() +"`),(c:package:`" +
                            projectName + "`:`" + curCommit + "`:`" + preInfo.getModuleName() +
                            "`)where p.packageName = \""+ preInfo.getPackageName() + "\" AND c.packageName = \"" + preInfo.getPackageName() +
                            "\" CREATE (p)-[r:change{}]->(c)" ;
                    cqlSet.add(cql);
                }
            } else {
                packageSet = new HashSet<>();
                packageSet.add(preInfo.getPackageName());
                changedModulePackageMap.put(preInfo.getModuleName(), packageSet);
                cql = "MATCH (p:package:`" + projectName + "`:`" + preCommit + "`:`" + preInfo.getModuleName() +"`),(c:package:`" +
                        projectName + "`:`" + curCommit + "`:`" + preInfo.getModuleName() +
                        "`)where p.packageName = \""+ preInfo.getPackageName() + "\" AND c.packageName = \"" + preInfo.getPackageName() +
                        "\" CREATE (p)-[r:change{}]->(c)" ;
                cqlSet.add(cql);
            }

            String input ;
            try {
                input = FileUtils.readFileToString(new File(diffPathList.get(i)), "UTF-8");
            }catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            JSONArray diffDetail = JSONArray.parseArray(input);
            createModifyDetail(preInfo, curInfo, diffDetail);
        }
    }

    /**
     *
     * type1: 层次级别：statement、member 指method 以及 field ;  这里 type1 重命名为 type
     * type2: 类型 Move  delete insert change  Change.Move等; 这里type2 重命名为 changeType
     * description: 描述 作为 relation 的一个属性
     * range ： 代码片段的 range eg: (30,34)-(28,32)
     * */
    private void createModifyDetail(Extractor preInfo, Extractor curInfo, JSONArray diffDetail) {
        String cql;
        String type;
        String changeType;
        String description;
        String range;
        int begin;
        int end;

        for (ClassInfo preClassInfo : preInfo.getClassInfos()) {
            ClassInfo curClassInfo = findClassInfoByName(curInfo, preClassInfo.getClassName());
            //先分析class 之间的关系  之后分析class 内的method与field的关系

            /////////////// class 之间的关系  不包含新增/////////////////////////
            //找不到说明是 preClassInfo 是被删除的
            if (curClassInfo == null) {
                // 为该 class 添加delete 标签
                cql = "MATCH (p:class`" + projectName + "`:`" + preCommit + "`:`" + preInfo.getPackageName() + "`:`" + preInfo.getModuleName() + "`) set p:delete" ;
                cqlSet.add(cql);
                // 为所有该class下的 method 以及 field添加delete标签
                cql = "MATCH (p:`" + projectName + "`:`" + preCommit + "`:`" + preInfo.getPackageName() + "`:`" + preInfo.getModuleName() +
                        "`:`" + preClassInfo.getClassName() + "`) set p:delete" ;
                cqlSet.add(cql);

                preInfo.getClassInfos().remove(preClassInfo);
                continue;
            }
            // class 之间的change关系
            cql = "MATCH (p:class:`" + projectName + "`:`" + preCommit + "`:`" + preInfo.getPackageName() + "`:`" + preInfo.getModuleName() + "`),(c:class:`" +
                    projectName + "`:`" + curCommit + "`:`" + preInfo.getPackageName() + "`:`" + preInfo.getModuleName() +
                    "`)where p.className = \""+ preClassInfo.getClassName() + "\" AND c.className = \"" + preClassInfo.getClassName() +
                    "\" CREATE (p)-[r:change{}]->(c)" ;
            cqlSet.add(cql);

            /////////////// field 之间的关系/////////////////////////
            List<FieldInfo> removeCurFieldInfo = new ArrayList<>();
            List<FieldInfo> removePreFieldInfo = new ArrayList<>();
            for (FieldInfo curFieldInfo : curClassInfo.getFiledInfos()) {
                for (FieldInfo preFieldInfo : preClassInfo.getFiledInfos()) {
                    // 没有改变的field处理
                    if (preFieldInfo.equals(curFieldInfo)) {
                        cql = "MATCH (p:field:`" + projectName + "`:`" + preCommit + "`:`" + preInfo.getPackageName() + "`:`" + curClassInfo.getClassName() +  "`:`" + preInfo.getModuleName() + "`),(c:field:`" +
                                projectName + "`:`" + curCommit + "`:`" + preInfo.getPackageName() + "`:`" + curClassInfo.getClassName() + "`:`" + preInfo.getModuleName() +
                                "`)where p.simpleName = \""+ preFieldInfo.getSimpleName().replace("\"", "\\\"") + "\" AND c.simpleName = \"" + preFieldInfo.getSimpleName().replace("\"", "\\\"") +
                                "\" CREATE (p)-[r:notChange{}]->(c)" ;
                        cqlSet.add(cql);
                        removeCurFieldInfo.add(curFieldInfo);
                        removePreFieldInfo.add(preFieldInfo);
                    }
                }
            }
            curClassInfo.getFiledInfos().removeAll(removeCurFieldInfo);
            preClassInfo.getFiledInfos().removeAll(removePreFieldInfo);


            //删除的field处理
            for (FieldInfo preFieldInfo : preClassInfo.getFiledInfos()) {
                cql = "MATCH (p:field:`" + projectName + "`:`" + preCommit + "`:`" + preInfo.getPackageName() + "`:`" + preClassInfo.getClassName() + "`:`" + preInfo.getModuleName() +
                        "`)where p.simpleName = \""+ preFieldInfo.getSimpleName().replace("\"", "\\\"")+
                        "\" set p:delete" ;
                cqlSet.add(cql);
            }
            //增加的field处理
            for (FieldInfo curFieldInfo : curClassInfo.getFiledInfos()) {
                cql = "MATCH (p:field:`" + projectName + "`:`" + curCommit + "`:`" + curInfo.getPackageName() + "`:`" + curClassInfo.getClassName() + "`:`" + curInfo.getModuleName() +
                        "`)where p.simpleName = \""+ curFieldInfo.getSimpleName().replace("\"", "\\\"")+
                        "\" set p:add" ;
                cqlSet.add(cql);
            }

            Set<MethodInfo> changedMethodSet = new HashSet<>();
            // key 是cur ，value 是 pre
            Map<String,String> signatureChangedMethodMap = new HashMap<>();
            /////////////// method 之间的关系/////////////////////////
            for (int i = 0; i < diffDetail.size(); i++) {
                JSONObject oneDiff = diffDetail.getJSONObject(i);
                type = oneDiff.getString("type1").toLowerCase();
                // Change.Move 是比statement 更细粒度的语句的change
                changeType = oneDiff.getString("type2").toLowerCase();
                if ("change.type".equals(changeType)) {
                    changeType = "change";
                }
                description = oneDiff.getString("description");
                range = oneDiff.getString("range");

                // 查找所有的method 以及处理其关系
                if ("member".equals(type) && description.toLowerCase().contains("method")) {
                    switch (changeType){
                        case "insert":
                            //根据range当前版本的method
                            begin = rangeAnalyzeBegin(range);
                            end = rangeAnalyzeEnd(range);
                            if (!isCurClass(begin, end, curClassInfo)) {
                                break;
                            }
                            MethodInfo curMethodInfo = findMethodByBeginEnd(curClassInfo, begin, end);
                            cql = "MATCH (c:method:`" + projectName + "`:`" + curCommit + "`:`" + curInfo.getPackageName() +  "`:`" + curClassInfo.getClassName() + "`:`" + curInfo.getModuleName() +
                                    "`)where c.signature = \""+  curMethodInfo.getSignature().replace("\"", "\\\"")  +
                                    "\" set c:add" ;
                            cqlSet.add(cql);
                            //该method 下面的所有statement 都加上add 标签
                            cql = "MATCH (c:`" + projectName + "`:`" + curCommit + "`:`" + preInfo.getPackageName() +  "`:`" + curClassInfo.getClassName() + "`:`" + curInfo.getModuleName() +
                                    "`:`" +  curMethodInfo.getSignature() + "`) set c:add" ;
                            cqlSet.add(cql);
                            //删除映射完的method
                            curClassInfo.getMethodInfos().remove(curMethodInfo);
                            break;
                        case "delete":
                            //根据range之前版本的method
                            begin = rangeAnalyzeBegin(range);
                            end = rangeAnalyzeEnd(range);
                            if (!isCurClass(begin, end, preClassInfo)) {
                                break;
                            }
                            MethodInfo preMethodInfo = findMethodByBeginEnd(preClassInfo, begin, end);
                            cql = "MATCH (p:method:`" + projectName + "`:`" + preCommit + "`:`" + preInfo.getPackageName() +  "`:`" + preClassInfo.getClassName() + "`:`" + preInfo.getModuleName() +
                                    "`)where p.signature ="+  preMethodInfo.getSignature().replace("\"", "\\\"")  +
                                    "\" set p:delete" ;
                            cqlSet.add(cql);
                            //该method 下面的所有statement 都加上add 标签
                            cql = "MATCH (p:`" + projectName + "`:`" + preCommit + "`:`" + preInfo.getPackageName() +  "`:`" + preClassInfo.getClassName() + "`:`" + preInfo.getModuleName() +
                                    "`:`" +  preMethodInfo.getSignature() + "`) set p:delete" ;
                            cqlSet.add(cql);
                            //删除映射完的method
                            preClassInfo.getMethodInfos().remove(preMethodInfo);
                            break;
                        case "change":
                            // method change 可能是change signature or change 声明
                            //根据range找上一个版本的method change signature
                            begin = rangeAnalyzeBegin(range.split("-")[0]);
                            end = rangeAnalyzeEnd(range.split("-")[0]);
                            if (!isCurClass(begin, end, preClassInfo)) {
                                break;
                            }
                            preMethodInfo = findMethodByBeginEnd(preClassInfo, begin, end);
                            //根据range当前版本的method
                            begin = rangeAnalyzeBegin(range.split("-")[1]);
                            end = rangeAnalyzeEnd(range.split("-")[1]);
                            curMethodInfo = findMethodByBeginEnd(curClassInfo, begin, end);
                            if (preMethodInfo==null || curMethodInfo==null)
                                continue;
                            //method 之间的关系为changeType 属性有 description:${description}
                            cql = "MATCH (p:method:`" + projectName + "`:`" + preCommit + "`:`" + preInfo.getPackageName() + "`:`" + preClassInfo.getClassName() + "`:`" + preInfo.getModuleName() +
                                    "`),(c:method:`" + projectName + "`:`" + curCommit + "`:`" + preInfo.getPackageName() +  "`:`" + curClassInfo.getClassName() + "`:`" + preInfo.getModuleName() +
                                    "`)where p.signature = \""+ preMethodInfo.getSignature().replace("\"", "\\\"") + "\" AND c.signature = \"" + curMethodInfo.getSignature().replace("\"", "\\\"") +
                                    "\" CREATE (p)-[r:change{description:\"" + description + "\"}]->(c)" ;
                            cqlSet.add(cql);
/*                            // method 之下的statement 都不变
                            // 基于findALL 函数的所有statement 都不变
                            String statementLabel = projectName + "`:`" + preCommit + "`:`" + preInfo.getPackageName() + "`:`" + preClassInfo.getClassName();
                            createNotChangedStatRelationship(preMethodInfo, curMethodInfo, statementLabel);

                            //删除映射完的method
                            // 方法签名改变 里面的statement 也有可能改变
                            preClassInfo.getMethodInfos().remove(preMethodInfo);
                            curClassInfo.getMethodInfos().remove(curMethodInfo);*/
                            // 该map 对应的是 change signature 前后的名字
                            signatureChangedMethodMap.put(curMethodInfo.getSignature(), preMethodInfo.getSignature());
                    }
                    continue;
                }

                /////////////// statement 之间的关系/////////////////////////
                // 新增的statement 是否属于更大的statement block？ 属于的话更大的statement也有可能是新增 还是增加change relation
                ///////////////////////////// 处理所有statement的信息/////////////////////////
                // 处理 基于statement chang 的method 的statement的关系
                if ("statement".equals(type)) {
                    MethodInfo curMethodInfo, preMethodInfo ;
                    StatementInfo curStatementInfo ,preStatementInfo;

                    String label = projectName + "`:`" + curInfo.getPackageName() +  "`:`" + curClassInfo.getClassName() + "`:`";
                    switch (changeType){
                        case "insert":
                            begin = rangeAnalyzeBegin(range);
                            end = rangeAnalyzeEnd(range);
                            if (!isCurClass(begin, end, curClassInfo)) {
                                break;
                            }
                            // 根据begin 与 end 找到 method
                            curMethodInfo = findMethodByBeginEnd(curClassInfo, begin, end);
                            if (curMethodInfo == null )
                                continue;
                            curStatementInfo = findStatementInfoByBeginEnd(curMethodInfo , begin, end);

                            cql = "MATCH (p:statement:`" + label + curMethodInfo.getSignature() + "`:`" + curCommit + "`:`" + curInfo.getModuleName() +
                                    "`)where p.begin = \""+ begin + "\" AND p.end = \"" + end +
                                    "\" set p:add" ;
                            cqlSet.add(cql);
                            // 寻找  父statement
                            // 有 父 statement 的话 父statement的关系只能是新增或者change
/*                            for (StatementInfo statementInfo : curMethodInfo.getStatementInfo()) {

                            }*/
                            changedMethodSet.add(curMethodInfo);
                            curMethodInfo.getStatementInfo().remove(curStatementInfo);
                            break;
                        case "delete":
                            begin = rangeAnalyzeBegin(range);
                            end = rangeAnalyzeEnd(range);
                            if (!isCurClass(begin, end, preClassInfo)) {
                                break;
                            }
                            // 根据begin 与 end 找到 method
                            preMethodInfo = findMethodByBeginEnd(preClassInfo, begin, end);
                            preStatementInfo = findStatementInfoByBeginEnd(preMethodInfo , begin, end);
                            cql = "MATCH (p:statement:`" + label + preMethodInfo.getSignature() + "`:`" + preCommit + "`:`" + preInfo.getModuleName() +
                                    "`)where p.begin = \""+ begin + "\" AND p.end = \"" + end +
                                    "\" set p:delete" ;
                            cqlSet.add(cql);
                            preMethodInfo.getStatementInfo().remove(preStatementInfo);
                            String curMethodSignature = preMethodInfo.getSignature();
                            for (Map.Entry<String,String> map : signatureChangedMethodMap.entrySet()) {
                                if (map.getValue().equals(preMethodInfo.getSignature())) {
                                    curMethodSignature = map.getKey();
                                    break;
                                }
                            }
                            curMethodInfo = findMethodBySignature(curClassInfo.getMethodInfos(), curMethodSignature);
                            changedMethodSet.add(curMethodInfo);
                            break;
                        case "change":
                            begin = rangeAnalyzeBegin(range.split("-")[0]);
                            end = rangeAnalyzeEnd(range.split("-")[0]);
                            if (!isCurClass(begin, end, preClassInfo)) {
                                break;
                            }
                            preMethodInfo = findMethodByBeginEnd(preClassInfo, begin, end);
                            preStatementInfo = findStatementInfoByBeginEnd(preMethodInfo , begin, end);
                            begin = rangeAnalyzeBegin(range.split("-")[1]);
                            end = rangeAnalyzeEnd(range.split("-")[1]);
                            // 根据begin 与 end 找到 method
                            curMethodInfo = findMethodByBeginEnd(curClassInfo, begin, end);
                            curStatementInfo = findStatementInfoByBeginEnd(curMethodInfo , begin, end);
                            cql = "MATCH (p:statement:`" + label + preMethodInfo.getSignature() + "`:`" + preCommit + "`:`" + preInfo.getModuleName() +
                                    "`),(c:statement:`" + label + curMethodInfo.getSignature() + "`:`" + curCommit + "`:`" + preInfo.getModuleName() +
                                    "`)where p.begin=" + preStatementInfo.getBegin() + " AND c.begin= " + begin +
                                    " AND p.end= " + preStatementInfo.getEnd() + " AND c.end= " + end +
                                    " CREATE (p)-[r:change{description:\"" + description + "\"}]->(c)" ;
                            cqlSet.add(cql);
                            changedMethodSet.add(curMethodInfo);
                            curMethodInfo.getStatementInfo().remove(curStatementInfo);
                            preMethodInfo.getStatementInfo().remove(preStatementInfo);
                    }
                }
            }

            // 处理   chang 的method 关系
            for (MethodInfo curMethodInfo : changedMethodSet) {
                // method 之间的关系为change
                cql = "MATCH (p:method:`" + projectName + "`:`" + preCommit + "`:`" + preInfo.getPackageName() + "`:`" + preClassInfo.getClassName() + "`:`" + preInfo.getModuleName() +
                        "`),(c:method:`" + projectName + "`:`" + curCommit + "`:`" + curInfo.getPackageName() +  "`:`" + curClassInfo.getClassName() + "`:`" + preInfo.getModuleName() +
                        "`)where p.signature = \""+ curMethodInfo.getSignature().replace("\"", "\\\"") + "\" AND c.signature = \"" + curMethodInfo.getSignature().replace("\"", "\\\"") +
                        "\" CREATE (p)-[r:change{}]->(c)" ;
                cqlSet.add(cql);
                curClassInfo.getMethodInfos().remove(curMethodInfo);
                String preMethodSignature = curMethodInfo.getSignature();
                for (Map.Entry<String,String> map : signatureChangedMethodMap.entrySet()) {
                    if (map.getKey().equals(preMethodSignature)) {
                        preMethodSignature = map.getValue();
                        break;
                    }
                }
                // method 为 change  下面： statement 的关系
                // ？？？？ method 下面statement 数量不等的情况怎么处理
                MethodInfo preMethodInfo = findMethodBySignature(preClassInfo.getMethodInfos(), preMethodSignature);
                String statementLabel = projectName + "`:`" + preInfo.getPackageName() + "`:`" + preClassInfo.getClassName() + "`:`" + preInfo.getModuleName() ;
                createNotChangedStatRelationship(preMethodInfo, curMethodInfo, statementLabel);

            }
            // 不变的method 以及
            // 处理不变的method 及其对应的statement
            for (MethodInfo curMethodInfo : curClassInfo.getMethodInfos()) {
                MethodInfo preMethodInfo = findMethodBySignature(preClassInfo.getMethodInfos(), curMethodInfo.getSignature());
                if (preMethodInfo == null){
                    continue;
                }
                cql = "MATCH (p:method:`" + projectName + "`:`" + preCommit + "`:`" + preInfo.getPackageName() + "`:`" + preClassInfo.getClassName() + "`:`" + preInfo.getModuleName() +
                        "`),(c:method:`" + projectName + "`:`" + curCommit + "`:`" + preInfo.getPackageName() +  "`:`" + curClassInfo.getClassName() + "`:`" + preInfo.getModuleName() +
                        "`)where p.signature = \""+ preMethodInfo.getSignature().replace("\"", "\\\"") + "\" AND c.signature = \"" + curMethodInfo.getSignature().replace("\"", "\\\"") +
                        "\" CREATE (p)-[r:notChange]->(c)" ;
                cqlSet.add(cql);
                String statementLabel = projectName + "`:`" + preInfo.getPackageName() + "`:`" + preClassInfo.getClassName() + "`:`" + preInfo.getModuleName() ;
                createNotChangedStatRelationship(preMethodInfo, curMethodInfo, statementLabel);
            }

            curInfo.getClassInfos().remove(curClassInfo);
        }

        /////////////// class 之间的关系  新增class /////////////////////////
        // curClass 还存在说明为新增的class
        for (ClassInfo curClassInfo : curInfo.getClassInfos()) {
            // 为该 class 添加add 标签
            cql = "MATCH (p:class`" + projectName + "`:`" + curCommit + "`:`" + curInfo.getPackageName() + "`:`" + curInfo.getModuleName() + "`) set p:add" ;
            cqlSet.add(cql);
            // 为所有该class下的 method 以及 field添加add标签
            cql = "MATCH (p:`" + projectName + "`:`" + curCommit + "`:`" + curInfo.getPackageName() + "`:`" + curInfo.getModuleName() +
                    "`:`" + curClassInfo.getClassName() + "`) set p:add" ;
            cqlSet.add(cql);
            preInfo.getClassInfos().remove(curClassInfo);
        }

    }

    private boolean isCurClass(int begin, int end, ClassInfo curClassInfo) {
        return ((begin >= curClassInfo.getBegin()) && (end <= curClassInfo.getEnd()));
    }

    // package 层 处理
    // 非变动的文件关系创造
    public void packageRelationAnalyze(List<String> notChangedFileLIst) {
        /*Set<PackageInfo> prePackageSet = preProjectInfo.getPackageInfos();*/
        String cql ;

/*        for (String packageName : changedModulePackageMap) {
            if (prePackageSet.contains(packageName)) {
                // 根据change packageList 构造 package级 的 change relationship
                cql = "MATCH (p:package:`" + projectName + "`:`" + preCommit +"`),(c:package:`" +
                        projectName + "`:`" + curCommit +
                        "`)where p.packageName = \""+ packageName + "\" AND c.packageName = \"" + packageName +
                        "\" CREATE (p)-[r:change{}]->(c)" ;
            } else {
                // 给新增的package添加 add label
                cql = "MATCH (p:package:`" + projectName + "`:`" + curCommit +
                        "`) where p.packageName =\"" + packageName +"\" set p:add" ;
            }
            cqlSet.add(cql);
        }*/

        RepoInfoBuilder notChangedProjectInfo = new RepoInfoBuilder(projectName, curCommit, curCommitter, notChangedFileLIst);
        //需要得到所有不变的文件分析成projectInfo  来做信息匹配
        for (PackageInfo notChangedPackageInfo : notChangedProjectInfo.getPackageInfos()) {
            if ((! changedModulePackageMap.keySet().contains(notChangedPackageInfo.getModuleName())) ||
                    (changedModulePackageMap.keySet().contains(notChangedPackageInfo.getModuleName()) &&
                            !changedModulePackageMap.get(notChangedPackageInfo.getModuleName()).contains(notChangedPackageInfo.getPackageName()))) {

                cql = "MATCH (p:package:`" + projectName + "`:`" + preCommit + "`:`" + notChangedPackageInfo.getModuleName() +
                        "`),(c:package:`" + projectName + "`:`" + curCommit + "`:`" + notChangedPackageInfo.getModuleName() +
                        "`)where p.packageName = \""+ notChangedPackageInfo.getPackageName() + "\" AND c.packageName = \"" + notChangedPackageInfo.getPackageName() +
                        "\" CREATE (p)-[r:notChange{}]->(c)" ;
                cqlSet.add(cql);
            }
            //该package 之下的所有均为notChange ； notChange 也需要一个个匹配
            PackageInfo prePackageInfo = findPackageByPackageNameAndModuleName(preProjectInfo, notChangedPackageInfo.getPackageName(), notChangedPackageInfo.getModuleName());
            // class notChange
            for (ClassInfo curClassInfo : notChangedPackageInfo.getClassInfos()) {
                cql = "MATCH (p:class:`" + projectName + "`:`" + preCommit + "`:`" + notChangedPackageInfo.getPackageName() + "`:`" + notChangedPackageInfo.getModuleName() +
                        "`),(c:class:`" + projectName + "`:`" + curCommit + "`:`" + notChangedPackageInfo.getPackageName() + "`:`" + notChangedPackageInfo.getModuleName() +
                        "`)where p.className = \""+ curClassInfo.getClassName() + "\" AND c.className =\"" + curClassInfo.getClassName() +
                        "\" CREATE (p)-[r:notChange{}]->(c)" ;
                cqlSet.add(cql);
                // method notChange
                ClassInfo preClassInfo = findClassInfoByName(prePackageInfo, curClassInfo.getClassName());
                for (MethodInfo curMethodInfo : curClassInfo.getMethodInfos()) {
                    cql = "MATCH (p:method:`" + projectName + "`:`" + preCommit + "`:`" + notChangedPackageInfo.getPackageName() + "`:`" + curClassInfo.getClassName() + "`:`" + notChangedPackageInfo.getModuleName() +
                            "`),(c:method:`" + projectName + "`:`" + curCommit + "`:`" + notChangedPackageInfo.getPackageName() + "`:`" + curClassInfo.getClassName() + "`:`" + notChangedPackageInfo.getModuleName() +
                            "`)where p.signature = \""+ curMethodInfo.getSignature().replace("\"", "\\\"") + "\" AND c.signature =\"" + curMethodInfo.getSignature().replace("\"", "\\\"") +
                            "\" CREATE (p)-[r:notChange{}]->(c)" ;
                    cqlSet.add(cql);
                    String statementLabel = projectName + "`:`" + notChangedPackageInfo.getPackageName() + "`:`" + curClassInfo.getClassName() + "`:`" + notChangedPackageInfo.getModuleName() ;
                    MethodInfo preMethodInfo = findMethodBySignature(preClassInfo.getMethodInfos() ,curMethodInfo.getSignature()) ;
                    createNotChangedStatRelationship(preMethodInfo, curMethodInfo, statementLabel);
                }

                // field notChange
                for (FieldInfo curFieldInfo : curClassInfo.getFiledInfos()) {
                    cql = "MATCH (p:field:`" + projectName + "`:`" + preCommit + "`:`" + notChangedPackageInfo.getPackageName() + "`:`" + curClassInfo.getClassName() + "`:`" + notChangedPackageInfo.getModuleName() +
                            "`),(c:field:`" + projectName + "`:`" + curCommit + "`:`" + notChangedPackageInfo.getPackageName() + "`:`" + curClassInfo.getClassName() + "`:`" + notChangedPackageInfo.getModuleName() +
                            "`)where p.simpleName = \""+ curFieldInfo.getSimpleName().replace("\"", "\\\"") + "\" AND c.simpleName =\"" + curFieldInfo.getSimpleName().replace("\"", "\\\"") +
                            "\" CREATE (p)-[r:notChange{}]->(c)" ;
                    cqlSet.add(cql);
                }
            }
        }

    }

    private PackageInfo findPackageByPackageNameAndModuleName(RepoInfoBuilder preProjectInfo, String packageName, String moduleName) {
        for (PackageInfo packageInfo : preProjectInfo.getPackageInfos()) {
            if (moduleName.equals(packageInfo.getModuleName()) && packageName.equals(packageInfo.getPackageName())) {
                return packageInfo;
            }
        }
        return null;
    }

    private StatementInfo findStatementInfoByBeginEnd(MethodInfo curMethodInfo, int begin, int end) {
        for (StatementInfo statementInfo : curMethodInfo.getStatementInfo()) {
            if (statementInfo.getBegin() == begin && statementInfo.getEnd() == end) {
                return statementInfo;
            }
        }
        return null;
    }

    private MethodInfo findMethodBySignature(List<MethodInfo> methodInfos, String signature) {
        for (MethodInfo methodInfo : methodInfos) {
            if (methodInfo.getSignature().equals(signature))
                return methodInfo;
        }
        return null;
    }

    private void createNotChangedStatRelationship(MethodInfo preMethodInfo, MethodInfo curMethodInfo, String statementLabel) {
        String cql;
        int minSize = Math.min(preMethodInfo.getStatementInfo().size(), curMethodInfo.getStatementInfo().size());
        try {
            for (int j = 0; j < minSize; j++) {
                cql = "MATCH (p:statement:`" + statementLabel + "`:`" + preCommit +
                        "`),(c:statement:`" + statementLabel + "`:`" + curCommit +
                        "`)where p.begin=" + preMethodInfo.getStatementInfo().get(j).getBegin() + " AND c.begin= " + curMethodInfo.getStatementInfo().get(j).getBegin() +
                        " AND p.end= " + preMethodInfo.getStatementInfo().get(j).getEnd() + " AND c.end= " + curMethodInfo.getStatementInfo().get(j).getEnd() +
                        " CREATE (p)-[r:notChange{}]->(c)" ;
                cqlSet.add(cql);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    private MethodInfo findMethodByBeginEnd(ClassInfo classInfo, int begin, int end) {
        for(MethodInfo methodInfo : classInfo.getMethodInfos()) {
            if (methodInfo.getBegin() <= begin && methodInfo.getEnd() >= end) {
                return methodInfo;
            }
        }
        return null;
    }

    private int rangeAnalyzeBegin(String range) {
        // range ：(87,102)
        return Integer.valueOf(range.substring(1,range.length() - 1).split(",")[0]);
    }

    private int rangeAnalyzeEnd(String range) {
        return Integer.valueOf(range.substring(0,range.length() - 1).split(",")[1]);
    }

    private ClassInfo findClassInfoByName(Extractor curInfo, String className) {
        for (ClassInfo curClassInfo : curInfo.getClassInfos()) {
            if (className.equals(curClassInfo.getClassName()))
                return curClassInfo;
        }
        return null;
    }

    private ClassInfo findClassInfoByName(PackageInfo packageInfo, String className) {
        for (ClassInfo curClassInfo : packageInfo.getClassInfos()) {
            if (className.equals(curClassInfo.getClassName()))
                return curClassInfo;
        }
        return null;
    }

    public Set<String> getCqlSet() {
        return cqlSet;
    }

    private PackageInfo findPackageInfoByModuleNameAndPackageName(Set<PackageInfo> packageInfos, PackageInfo deletePackageInfo) {
        for (PackageInfo packageInfo : packageInfos) {
            if (packageInfo.getModuleName().equals(deletePackageInfo.getModuleName()) &&
                    packageInfo.getPackageName().equals(deletePackageInfo.getPackageName())) {
                return packageInfo;
            }
        }
        return null;
    }
}