package org.couverjure.core;

import com.sun.jna.*;
import org.couverjure.jna.Foundation;
import org.couverjure.jna.FoundationTypeMapper;

import java.lang.reflect.*;
import java.lang.reflect.Method;

public abstract class MethodImplProxy implements CallbackProxy {
    public static TypeMapper TYPE_MAPPER = FoundationTypeMapper.getTypeMapper();
    public static boolean DEBUG = false;

    private int arity;
    private Class returnType;
    private Class[] parameterTypes;
    private FromNativeConverter[] paramConverters;
    private ToNativeConverter resultConverter;
    private String debugName;

    //private java.lang.reflect.Method callbackMethod;
    //private ToNativeConverter toNative;
    //private FromNativeConverter[] fromNative;

    public MethodImplProxy(String debugName, Class returnType, Class[] parameterTypes) {
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.arity = parameterTypes.length;
        this.debugName = debugName;
        initConverters();
    }

    private void initConverters() {
        paramConverters = new FromNativeConverter[arity];
        for (int i = 0; i < arity; i++) {
            Class paramType = parameterTypes[i];
            paramConverters[i] = TYPE_MAPPER.getFromNativeConverter(paramType);
        }
        resultConverter = TYPE_MAPPER.getToNativeConverter(returnType);
    }

    public Object callback(Object[] nativeArgs) {
        try {
            if (DEBUG) System.out.println("MethodImplProxy.callback: " + debugName); 
            Object[] convertedArgs = new Object[arity];

            for (int i = 0; i < arity; i++) {
                if (paramConverters[i] != null) {
                    convertedArgs[i] = paramConverters[i].fromNative(nativeArgs[i], null);
                } else {
                    convertedArgs[i] = nativeArgs[i];
                }
            }

            Object result = method(convertedArgs);

            if (resultConverter != null) {
                return resultConverter.toNative(result, null);
            } else {
                return result;
            }
        } catch (Throwable t) {
            System.out.println(String.format("Caught exception %s in implementation of method %s", t, debugName));
            if (DEBUG) t.printStackTrace();
            return null;
        }
    }

    public abstract Object method(Object[] args);

    public Class[] getParameterTypes() {
        return parameterTypes.clone();
    }

    public Class getReturnType() {
        return returnType;
    }

    public void finalize() {
        System.out.println("In MethodImplProxy.finalize! Ooops.");
    }
}
