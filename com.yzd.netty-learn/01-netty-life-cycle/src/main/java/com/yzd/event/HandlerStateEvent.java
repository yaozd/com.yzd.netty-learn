package com.yzd.event;

import lombok.Getter;

/**
 * 自定义用户事件
 * @Author: yaozh
 * @Description:
 */
public class HandlerStateEvent {
    public static final HandlerStateEvent FULSH_DATA_EVENT = new HandlerStateEvent(HandlerState.FLUSH_DATA);
    @Getter
    private final HandlerState state;

    private HandlerStateEvent(HandlerState state) {
        this.state = state;
    }
}
