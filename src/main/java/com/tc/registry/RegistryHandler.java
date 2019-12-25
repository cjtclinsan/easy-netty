package com.tc.registry;

import com.tc.protocol.InvokerProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author taosh
 * @create 2019-12-25 13:12
 */
public class RegistryHandler extends ChannelInboundHandlerAdapter {
    private List<String> classNames = new ArrayList<String>();

    private static ConcurrentHashMap<String, Object> registryMap = new ConcurrentHashMap<String, Object>();

    public RegistryHandler() {
        //1，根据一个包名将所有符合条件的class全部扫描，放入一个容器   分布式，读配置文件
        scannerClass("com.tc.provider");

        //2，给每一个对应的class起一个唯一名字，作为服务名称，保存到容器中
        doRegistry();

    }

    //注册
    private void doRegistry() {
        if( classNames.isEmpty() ){
            return;
        }else {
            for (String className : classNames) {
                try {
                    Class<?> clazz = Class.forName(className);
                    Class<?> i = clazz.getInterfaces()[0];
                    String serviceName = i.getName();

                    //这边应该存网络路径，从配置文件读取
                    //在调用的时候去解析，这里直接使用反射调用
                    registryMap.put(serviceName, clazz.newInstance());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    //正常来说，应该是读配置文件
    private void scannerClass(String packageName) {
        URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());

        for (File file : classPath.listFiles()) {
            if( file.isDirectory() ){
                scannerClass(packageName+"."+file.getName());
            }else {
                classNames.add(packageName+"."+file.getName().replace(".class", "").trim() );
            }
        }
    }

    /**
     * 有客户端连上时，回调
     * 3，当有客户端连接过来，就会获取协议内容InvokerProtocol的对象
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object result = new Object();

        InvokerProtocol reuqest = (InvokerProtocol) msg;

        //4，去注册好的容器中找到符合条件的服务
        if( registryMap.containsKey(reuqest.getClassName()) ){
            Object service = registryMap.get(reuqest.getClassName());
            Method method = service.getClass().getMethod(reuqest.getMethodName(), reuqest.getParams());
            result = method.invoke(service, reuqest.getValues());
        }

        //5，通过远程调用provider得到返回结果，回复给服务端
        ctx.write(result);
        ctx.flush();
        ctx.close();
    }

    /**
     * 连接异常时，回调
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
