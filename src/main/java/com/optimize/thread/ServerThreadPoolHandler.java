package com.optimize.thread;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author taosh
 * @create 2020-01-10 14:21
 */
@ChannelHandler.Sharable
public class ServerThreadPoolHandler extends ServerHandler{
    public static final ChannelHandler INSTANCE = new ServerThreadPoolHandler();

    private static ExecutorService threadPool = Executors.newFixedThreadPool(2000);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {

        final ByteBuf data = Unpooled.directBuffer();
        data.writeBytes(byteBuf);

        threadPool.submit(()->{
            Object result = getResult(data);
            ctx.channel().writeAndFlush(result);
        });
    }
}
