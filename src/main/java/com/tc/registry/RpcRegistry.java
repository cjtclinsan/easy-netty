package com.tc.registry;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * @author taosh
 * @create 2019-12-25 11:43
 */
public class RpcRegistry {
    private int port;

    public RpcRegistry(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        //ServerSocket或ServerSocketChannel
        //netty 基于nio实现的
        //Selector主线程         Work线程

        //主线程池   Selector
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //子线程池    具体对应客户端的处理逻辑
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //在netty中，把业务逻辑处理归总到了一个队列中，包含了各种处理逻辑，
                            // 这些处理逻辑在netty中封装成一个对象->无锁化任务队列   Pipline
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            //对处理逻辑的封装
                            //对自定义内容进行编、解码
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0 , 4));
                            //自定义编码器
                            pipeline.addLast(new LengthFieldPrepender(4));
                            //实参处理
                            pipeline.addLast("encoder", new ObjectEncoder());
                            pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));

                            //前面完成对数据的解析
                            //这一步执行自己的逻辑
                            //1,注册 给每一个对象起一个名字，对外提供服务的名字
                            //2,服务位置做一个登记
                            pipeline.addLast(new RegistryHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            //正式启动服务，相当于用一个死循环在轮询
            ChannelFuture future = server.bind(port).sync();
            System.out.println("TCRpcRegistry start, listen port:"+port);
            future.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        try {
            new RpcRegistry(8080).start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

