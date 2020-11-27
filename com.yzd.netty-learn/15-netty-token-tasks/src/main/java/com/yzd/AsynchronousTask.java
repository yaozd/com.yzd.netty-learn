package com.yzd;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author: yaozh
 * @Description:
 */
@Getter
@Setter
public class AsynchronousTask {
    private Object param;
    private TaskFunction taskFunction;

    /**
     * Boolean 有3个状态：null(未知)，true,false
     * @return
     */
    public Boolean execute() {
        return taskFunction.check(param);
    }
}
