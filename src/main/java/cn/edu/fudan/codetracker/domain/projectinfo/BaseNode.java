package cn.edu.fudan.codetracker.domain.projectinfo;

import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * description: 所有节点的父节点
 *  每个节点都有的属性 因节点不同 属性的值不同
 *
 * @author fancying
 * create: 2020-03-20 00:23
 **/
@Data
@NoArgsConstructor
public  class BaseNode {

    private String uuid = UUID.randomUUID().toString();
    /**
     * isMapping:描述该节点是否已经mapping,新增的节点以及删除的节点也算mapping过
     */
    private boolean isMapping = false;
    private ChangeStatus changeStatus = ChangeStatus.UNCHANGED;

    /**
     * FIXME preMappingBaseNode 和 nextMappingBaseNode 有多个的话说明是merge点
     *  在基于内存优化查询的情况下 需要考虑node只有一个的合理性
     */
    private BaseNode preMappingBaseNode = null;
    private BaseNode nextMappingBaseNode = null;
    private BaseNode parent;

    private List<? extends BaseNode> children;
    /**
     * 描述追溯的信息
     *  version 追溯中第几个版本
     *  rootUuid
     */
    private int version = 1;
    private String rootUuid;
    private ProjectInfoLevel projectInfoLevel;


    /**
     * 描述节点的状态
     */
    public enum ChangeStatus {

        // 增加、删除、自己改变、移动、因 子节点的改变而改变、非逻辑上改变、无变化
        ADD(1),
        DELETE(1),
        SELF_CHANGE(1),
        MOVE(2),
        CHANGE(2),
        CHANGE_RECORD(3),
        UNCHANGED(4);

        private int priority;
        ChangeStatus(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }

}