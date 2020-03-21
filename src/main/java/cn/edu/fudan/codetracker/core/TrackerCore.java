package cn.edu.fudan.codetracker.core;

import cn.edu.fudan.codetracker.domain.projectinfo.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import static org.springframework.data.util.CastUtils.cast;

/**
 * description: 每个项目一个实例
 *
 * @author fancying
 * create: 2020-03-20 19:32
 **/
@Data
@Slf4j
@NoArgsConstructor
public class TrackerCore {

    private Map<String, BaseNode> projectStructureTree;

    /**
     * 每个线程单独持有一个 CommonInfo
     *  CommonInfo 存储的是某个版本的项目结构树上所有节点共享的信息
     */
    private static final ThreadLocal<CommonInfo> COMMON_INFO_THREAD_LOCAL = new ThreadLocal<>();

    private void mappingModule() {

    }

    public void mapping(ModuleInfo preRoot, ModuleInfo curRoot) {

        // TODO 参数为两个相同的 moduleInfo
        for (BaseNode baseNode : curRoot.getChildren()) {
            PackageNode prePackageNode = cast(findSimilarNode(preRoot.getChildren(), baseNode));
            PackageNode curPackageNode = cast(baseNode);
            // TODO 新增
            if (prePackageNode == null) {
                AddHandler.getInstance().subTreeMapping(prePackageNode, curPackageNode);
            } else {
                // TODO change: logical physical
                changeMapping(prePackageNode, curPackageNode);
                NodeMapping.setNodeMapped(preRoot, curRoot);
            }
        }
        preRoot.getChildren()
                .stream()
                .filter(node -> !(node.isMapping()))
                .forEach(preNode -> DeleteHandler.getInstance().subTreeMapping(preNode, null));

    }


    private void changeMapping(PackageNode prePackageNode, PackageNode curPackageNode) {

    }

    private BaseNode findSimilarNode(List<? extends BaseNode> nodeList, BaseNode target) {
        // FIXME
        return target;
    }



    public static void remove() {
        COMMON_INFO_THREAD_LOCAL.remove();
    }


}