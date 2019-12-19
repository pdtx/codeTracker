package cn.edu.fudan.codetracker.exception;

import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;

/**
 * description: constants of exception message
 * @author fancying
 * create: 2019-12-14 22:59
 **/
public final class ExceptionMessage {
    public static final String TRACKER_INFO_NULL = " TRACKER_INFO_NULL! ";
    public static final String PACKAGE_TRACKER_INFO_NULL = ProjectInfoLevel.PACKAGE.getName() + TRACKER_INFO_NULL;
    public static final String FILE_TRACKER_INFO_NULL = ProjectInfoLevel.FILE.getName() + TRACKER_INFO_NULL;
    public static final String CLASS_TRACKER_INFO_NULL = ProjectInfoLevel.CLASS.getName() + TRACKER_INFO_NULL;
    public static final String FIELD_TRACKER_INFO_NULL = ProjectInfoLevel.FIELD.getName() + TRACKER_INFO_NULL;
    public static final String METHOD_TRACKER_INFO_NULL = ProjectInfoLevel.METHOD.getName() + TRACKER_INFO_NULL;
    public static final String STATEMENT_TRACKER_INFO_NULL = ProjectInfoLevel.STATEMENT.getName() + TRACKER_INFO_NULL;


}