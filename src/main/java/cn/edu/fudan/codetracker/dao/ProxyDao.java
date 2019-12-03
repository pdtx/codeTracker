/**
 * @description: 代理各个dao层的操作；在批量分析场景下代理实现数据库的crud操作
 * @author: fancying
 * @create: 2019-12-01 21:00
 **/
package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import cn.edu.fudan.codetracker.domain.projectinfo.TrackerInfo;
import cn.edu.fudan.codetracker.util.RepoInfoBuilder;
import org.apache.kafka.common.protocol.types.Field;

public class ProxyDao {

    private RepoInfoBuilder repoInfo;

    private PackageDao packageDao;
    private FileDao fileDao;
    private ClassDao classDao;
    private FieldDao fieldDao;
    private MethodDao methodDao;
    private StatementDao statementDao;

    public ProxyDao() {

    }

    public ProxyDao(PackageDao packageDao, FileDao fileDao, ClassDao classDao, FieldDao fieldDao, MethodDao methodDao, StatementDao statementDao) {
        this.packageDao = packageDao;
        this.fileDao = fileDao;
        this.classDao = classDao;
        this.fieldDao = fieldDao;
        this.methodDao = methodDao;
        this.statementDao = statementDao;
    }

    public TrackerInfo getTrackerInfo(ProjectInfoLevel projectInfoLevel, String... args) {
        if (repoInfo != null) {
            return null;
        }

        switch (projectInfoLevel) {
            case PACKAGE:
                return packageDao.getTrackerInfo(args[0], args[1], args[2], args[3]);
            case FILE:
                return fileDao.getTrackerInfo(args[0], args[1], args[2]);
            case CLASS:
                return classDao.getTrackerInfo(args[0], args[1], args[2], args[3]);
            case METHOD:
                return methodDao.getTrackerInfo(args[0], args[1], args[2], args[3], args[4]);
            case FIELD:
                return fieldDao.getTrackerInfo(args[0], args[1], args[2], args[3], args[4]);
            case STATEMENT:
                return statementDao.getTrackerInfo(args[0], args[1], args[2], args[3]);
            default:
                return null;
        }
    }

    /**
     *  getter and setter
     */
    public RepoInfoBuilder getRepoInfo() {
        return repoInfo;
    }

    public void setRepoInfo(RepoInfoBuilder repoInfo) {
        this.repoInfo = repoInfo;
    }

    public PackageDao getPackageDao() {
        return packageDao;
    }

    public void setPackageDao(PackageDao packageDao) {
        this.packageDao = packageDao;
    }

    public FileDao getFileDao() {
        return fileDao;
    }

    public void setFileDao(FileDao fileDao) {
        this.fileDao = fileDao;
    }

    public ClassDao getClassDao() {
        return classDao;
    }

    public void setClassDao(ClassDao classDao) {
        this.classDao = classDao;
    }

    public FieldDao getFieldDao() {
        return fieldDao;
    }

    public void setFieldDao(FieldDao fieldDao) {
        this.fieldDao = fieldDao;
    }

    public MethodDao getMethodDao() {
        return methodDao;
    }

    public void setMethodDao(MethodDao methodDao) {
        this.methodDao = methodDao;
    }

    public StatementDao getStatementDao() {
        return statementDao;
    }

    public void setStatementDao(StatementDao statementDao) {
        this.statementDao = statementDao;
    }
}