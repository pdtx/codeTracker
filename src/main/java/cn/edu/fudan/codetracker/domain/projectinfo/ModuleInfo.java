package cn.edu.fudan.codetracker.domain.projectinfo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * description:
 *
 * @author fancying
 * create: 2020-03-20 19:39
 **/
@Setter
@Getter
@Builder
@NoArgsConstructor
public class ModuleInfo extends BaseNode {

    private String name;

    public ModuleInfo(String name) {
        this.name = name;
    }

}