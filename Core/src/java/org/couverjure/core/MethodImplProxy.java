package org.couverjure.core;

import com.sun.jna.*;
import org.couverjure.jna.Foundation;
import org.couverjure.jna.FoundationTypeMapper;

import java.lang.reflect.*;
import java.lang.reflect.Method;

public abstract class MethodImplProxy implements CallbackProxy {
    public static TypeMapper TYPE_MAPPER = FoundationTypeMapper.getTypeMapper();

    private int arity;
    private Class returnType;
    private Class[] parameterTypes;
    private FromNativeConverter[] paramConverters;
    private ToNativeConverter resultConverter;

    //private java.lang.reflect.Method callbackMethod;
    //private ToNativeConverter toNative;
    //private FromNativeConverter[] fromNative;

    public MethodImplProxy(Class returnType, Class[] parameterTypes) {
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.arity = parameterTypes.length;
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
            //Native.getCallbackExceptionHandler().uncaughtException(null, t);
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
}
