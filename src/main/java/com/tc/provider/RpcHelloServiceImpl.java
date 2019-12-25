package com.tc.provider;

import com.tc.api.IRpcHelloService;

/**
 * @author taosh
 * @create 2019-12-25 11:40
 */
public class RpcHelloServiceImpl implements IRpcHelloService {
    public String hello(String name) {
        return "Hello:"+name;
    }
}
