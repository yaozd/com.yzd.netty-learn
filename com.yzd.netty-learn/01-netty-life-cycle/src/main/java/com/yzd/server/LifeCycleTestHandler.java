package com.yzd.server;

import com.yzd.event.HandlerState;
import com.yzd.event.HandlerStateEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.ChannelInputShutdownEvent;

import static com.yzd.event.HandlerStateEvent.FULSH_DATA_EVENT;

/**
 * @Author: yaozh
 * @Description:
 */
public class LifeCycleTestHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("逻辑处理器被添加：handlerAdded()");
        super.handlerAdded(ctx);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel 绑定到线程(NioEventLoop)：channelRegistered()");
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel 准备就绪：channelActive()");
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("channel 有数据可读：channelRead()");
        super.channelRead(ctx, msg);
        //
        //自定义用户事件
        //fireUserEventTriggered代表发给下一跳的handler处理事件，非当前handler
        //ctx.fireUserEventTriggered(HandlerStateEvent.FULSH_DATA_EVENT);
        //---------------------------------------------------------------------
        // Return the {@link EventLoop} this {@link Channel} was registered to
        // EventLoop：与当前channel使用同一线程
        ctx.channel().eventLoop().submit(()->{
            try {
                userEventTriggered(ctx, FULSH_DATA_EVENT);
            } catch (Exception e) {
                new RuntimeException(e);
            }
        });
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel 某次数据读完：channelReadComplete()");
        super.channelReadComplete(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel 被关闭：channelInactive()");
        System.out.println("channel 被关闭：channelInactive()-ctx.channel().isActive()"+ctx.channel().isActive());
        super.channelInactive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel 取消线程(NioEventLoop) 的绑定: channelUnregistered()");
        super.channelUnregistered(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("逻辑处理器被移除：handlerRemoved()");
        super.handlerRemoved(ctx);
    }

    //===========================================================
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("channel 发生异常：exceptionCaught()-ctx.channel().isActive():"+ctx.channel().isActive());
        System.out.println("channel 发生异常：exceptionCaught()");
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel 可写更改-高低水位线:channelWritabilityChanged()");
        super.channelWritabilityChanged(ctx);

    }

    /**
     * 使用场景：
     * 心跳检查
     * 如果读通道处于空闲状态，说明没有接收到心跳命令
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        System.out.println("channel 用户事件触发-心跳检查：userEventTriggered():"+evt.getClass());
        if (evt instanceof ChannelInputShutdownEvent) {
            System.out.println("channel 用户事件触发-远程主机强制关闭连接:");
            //远程主机强制关闭连接
            System.out.println("远程主机强制关闭连接后，手动关闭当前连接");
            ctx.channel().close();
        }
        if(evt instanceof HandlerStateEvent){
            //自动flush数据
            HandlerStateEvent handlerStateEvent=(HandlerStateEvent)evt;
            if(HandlerState.FLUSH_DATA.equals(handlerStateEvent.getState())&&ctx.channel().isActive()){
                ctx.flush();
                return;
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}
