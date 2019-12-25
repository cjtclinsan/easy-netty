package com.tc.provider;

import com.tc.api.IRpcService;

/**
 * @author taosh
 * @create 2019-12-25 11:41
 */
public class RpcServiceImpl implements IRpcService {
    public int add(int a, int b) {
        return a + b;
    }

    public int sub(int a, int b) {
        return a - b;
    }

    public int mult(int a, int b) {
        return a * b;
    }

    public int div(int a, int b) {
        return a / b;
    }
}
