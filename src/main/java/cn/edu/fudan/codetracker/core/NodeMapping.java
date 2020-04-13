package cn.edu.fudan.codetracker.core;

import cn.edu.fudan.codetracker.domain.projectinfo.BaseNode;

/**
 * description: 此类主要负责节点间的映射
 *
 * @author fancying
 * create: 2020-03-18 09:04
 **/
public interface NodeMapping {

    /**
     * 两个节点树的映射
     * @param preRoot 前一个版本的根结点
     * @param curRoot 当前版本的根节点
     */
    void subTreeMapping(BaseNode preRoot, BaseNode curRoot);

    /**
     * 设置两个节点的映射状态
     * @param preRoot 前一个版本的根结点
     * @param curRoot 当前版本的根节点
     */
    static void setNodeMapped(BaseNode preRoot, BaseNode curRoot) {
        if (preRoot == null && curRoot == null) {
            return;
        } else if (preRoot == null) {
            curRoot.setMapping(true);
        } else if (curRoot == null) {
            preRoot.setMapping(true);
        } else {
            preRoot.setMapping(true);
            curRoot.setMapping(true);
            preRoot.setNextMappingBaseNode(curRoot);
            curRoot.setPreMappingBaseNode(preRoot);
        }
    }

}
