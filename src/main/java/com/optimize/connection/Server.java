package com.optimize.connection;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author taosh
 * @create 2020-01-10 14:59
 */
public class Server {
    public static final int BEGIN_PORT = 8080;
    public static final int N_PORT = 8100;

    public void start(int beginPort, int nPort){
        System.out.println("server starting...");

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_REUSEADDR, true);

        bootstrap.childHandler(new ConnectionCountHandler());

        for(int i = 0; i <= (nPort - beginPort); i++){
            final int port = beginPort + i;

            bootstrap.bind(port).addListener((ChannelFutureListener) channelFuture -> {
                System.out.println("服务器已启动,端口:"+port);
            });
        }

        System.out.println("server started!!!");
    }

    public static void main(String[] args) {
        new Server().start(BEGIN_PORT, N_PORT);
    }
}
