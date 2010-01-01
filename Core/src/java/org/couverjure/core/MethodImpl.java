package org.couverjure.core;

import clojure.lang.ArraySeq;
import clojure.lang.IFn;
import com.sun.jna.CallbackProxy;


public class MethodImpl implements CallbackProxy {
    private Class[] parameterTypes;
    private Class returnType;
    private IFn fn;

    public MethodImpl(String signature, IFn fn) {
        parameterTypes = Encoding.encodingToClasses(signature.substring(1));
        returnType = Encoding.encodingToClass(signature.charAt(0));
        this.fn = fn;
    }

    public Object callback(Object[] args) {
        try {
            switch (args.length) {
                case 0:
                    return fn.invoke();
                case 1:
                    return fn.invoke(args[0]);
                case 2:
                    return fn.invoke(args[0], args[1]);
                case 3:
                    return fn.invoke(args[0], args[1], args[2]);
                case 4:
                    return fn.invoke(args[0], args[2], args[3], args[4]);
                case 5:
                    return fn.invoke(args[0], args[2], args[3], args[4], args[5]);
                case 6:
                    return fn.invoke(args[0], args[2], args[3], args[4], args[5], args[6]);
                case 7:
                    return fn.invoke(args[0], args[2], args[3], args[4], args[5], args[6], args[7]);
                case 8:
                    return fn.invoke(args[0], args[2], args[3], args[4], args[5], args[6], args[7], args[8]);
                case 9:
                    return fn.invoke(args[0], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9]);
                case 10:
                    return fn.invoke(args[0], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10]);
                case 11:
                    return fn.invoke(args[0], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11]);
                case 12:
                    return fn.invoke(args[0], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12]);
                case 13:
                    return fn.invoke(args[0], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13]);
                case 14:
                    return fn.invoke(args[0], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14]);
                case 15:
                    return fn.invoke(args[0], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15]);
                case 16:
                    return fn.invoke(args[0], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16]);
                case 17:
                    return fn.invoke(args[0], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17]);
                case 18:
                    return fn.invoke(args[0], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18]);
                default:
                    return fn.applyTo(ArraySeq.create(args));
            }
        } catch (Exception e) {
            // can we set objective-c error here?
        }
        return null;
    }

    public Class[] getParameterTypes() {
        return parameterTypes;
    }

    public Class getReturnType() {
        return returnType;
    }
}