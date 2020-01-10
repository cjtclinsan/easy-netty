package com.optimize.thread;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;

import java.util.concurrent.ExecutionException;

/**
 * @author taosh
 * @create 2020-01-10 14:26
 */
public class Client {
    public static final String SERVER_HOST = "127.0.0.1";

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        new Client().start(8080);
    }

    public void start(int port) throws ExecutionException, InterruptedException {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        final Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new FixedLengthFrameDecoder(Long.BYTES));
                        socketChannel.pipeline().addLast(ClientHandler.INSTANCE);
                    }
                });

        for (int i = 0; i < 1000; i++){
            bootstrap.connect(SERVER_HOST, port).get();
        }
    }
}
