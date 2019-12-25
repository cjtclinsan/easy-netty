package com.tc.consumer.proxy;

import com.tc.protocol.InvokerProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author taosh
 * @create 2019-12-25 13:52
 */
public class RpcProxy {
    public static <T> T create(Class<?> clazz){
        MethodProxy proxy = new MethodProxy(clazz);

        Class<?>[] interfaces = clazz.isInterface() ?
                new Class[]{clazz} : clazz.getInterfaces();

        T result = (T) Proxy.newProxyInstance(clazz.getClassLoader(), interfaces, proxy);
        return result;
    }

    /**
     * 将本地调用 通过代理的形式变成网络调用
     */
    private static class MethodProxy implements InvocationHandler{
        private Class<?> clazz;

        public MethodProxy(Class<?> clazz) {
            this.clazz = clazz;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            //如果传进来是一个已实现的具体类
            if( Object.class.equals(method.getDeclaringClass()) ){
                try {
                    return method.invoke(this, args);
                }catch (Throwable t){
                    t.printStackTrace();
                }
            }else {
                return rpcInvoker(proxy, method, args);
            }

            return null;
        }

        /**
         * 实现接口的方法
         * @param proxy
         * @param method
         * @param args
         * @return
         */
        private Object rpcInvoker(Object proxy, Method method, Object[] args) {
            //像构造一个协议的内容（消息）
            InvokerProtocol msg = new InvokerProtocol();
            msg.setClassName(this.clazz.getName());
            msg.setMethodName(method.getName());
            msg.setParams(method.getParameterTypes());
            msg.setValues(args);

            final RpcProxyHandler proxyHandler = new RpcProxyHandler();
            //发起网络请求
            EventLoopGroup workGroup = new NioEventLoopGroup();
            try {
                Bootstrap client = new Bootstrap();
                client.group(workGroup)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                ChannelPipeline pipeline = socketChannel.pipeline();
                                //对处理逻辑的封装
                                //对自定义内容进行编、解码
                                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0 , 4));
                                //自定义编码器
                                pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                                //实参处理
                                pipeline.addLast("encoder", new ObjectEncoder());
                                pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));

                                pipeline.addLast("handler", proxyHandler);
                            }
                        });
                ChannelFuture future = client.connect("localhost", 8080).sync();
                future.channel().writeAndFlush(msg).sync();
                future.channel().closeFuture().sync();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                workGroup.shutdownGracefully();
            }

            return proxyHandler.getResponse();
        }
    }
}
