package com.tc.consumer;

import com.tc.api.IRpcHelloService;
import com.tc.api.IRpcService;
import com.tc.consumer.proxy.RpcProxy;
import com.tc.provider.RpcHelloServiceImpl;
import com.tc.provider.RpcServiceImpl;

/**
 * @author taosh
 * @create 2019-12-25 13:48
 */
public class RpcConsumer {


    public static void main(String[] args) {
        IRpcHelloService helloService = RpcProxy.create(IRpcHelloService.class);
        //本地调用
//        IRpcHelloService helloService = new RpcHelloServiceImpl();
        System.out.println(helloService.hello("tccc"));
//
        IRpcService service = RpcProxy.create(IRpcService.class);
        System.out.println("6 + 3 = "+service.add(6, 3));
        System.out.println("6 - 3 = "+service.sub(6, 3));
        System.out.println("6 * 3 = "+service.mult(6, 3));
        System.out.println("6 / 3 = "+service.div(6, 3));
    }
}
