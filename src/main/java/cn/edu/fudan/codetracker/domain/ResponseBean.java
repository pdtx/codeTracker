package cn.edu.fudan.codetracker.domain;

import lombok.*;

import java.io.Serializable;

/**
 * description:
 * @author fancying
 * create: 2019-11-02 10:38
 **/

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ResponseBean implements Serializable {

    private int code;
    private String msg;
    private Object data;

}