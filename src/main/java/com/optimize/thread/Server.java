package com.optimize.thread;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;

/**
 * @author taosh
 * @create 2020-01-10 14:07
 */
public class Server {
    private static final int port = 8080;

    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        final EventLoopGroup businessGroup = new NioEventLoopGroup(800);

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_REUSEADDR, true);

        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                //自定义长度的解码，每次发送一个long类型的长度数据（这里是时间戳）
                socketChannel.pipeline().addLast(new FixedLengthFrameDecoder(Long.BYTES));
                socketChannel.pipeline().addLast(ServerThreadPoolHandler.INSTANCE);
                socketChannel.pipeline().addLast(businessGroup, ServerHandler.INSTANCE);
            }
        });

        ChannelFuture future = bootstrap.bind(port).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                System.out.println("服务器已启动，端口:"+port);
            }
        });
    }
}
