package com.tc.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * @author taosh
 * @create 2019-12-25 11:38
 */
@Data
public class InvokerProtocol implements Serializable {
    //服务名
    private String className;
    //方法名
    private String methodName;
    //形参列表
    private Class<?> [] params;
    //实参列表
    private Object[] values;
}
