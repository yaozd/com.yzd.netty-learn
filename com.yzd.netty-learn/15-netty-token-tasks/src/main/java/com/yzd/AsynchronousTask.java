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

    public boolean execute() {
        return taskFunction.check(param);
    }
}
