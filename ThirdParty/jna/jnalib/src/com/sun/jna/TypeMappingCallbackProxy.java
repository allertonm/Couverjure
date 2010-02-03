/* Copyright (c) 2007-2010 Timothy Wall & Mark Allerton, All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package com.sun.jna;

/**
 * TypeMappingCallbackProxy is an implementation of CallbackProxy that chains to
 * another CallbackProxy instance and wraps invocations of that chained instance
 * with the same argument and return value marshalling as would be applied for
 * implementations of Callback.
 *
 * This is intended for use by dynamic languages where JNA's standard marshalling
 * for structures etc makes sense.
 */
public abstract class TypeMappingCallbackProxy implements CallbackProxy {
    private CallbackProxy chained;
    private ToNativeConverter toNative;
    private FromNativeConverter[] fromNative;

    public TypeMappingCallbackProxy(CallbackProxy chained, TypeMapper mapper) {
        this.chained = chained;
        Class returnType = chained.getReturnType();
        Class[] argTypes = chained.getParameterTypes();

        fromNative = new FromNativeConverter[argTypes.length];
        if (NativeMapped.class.isAssignableFrom(returnType)) {
            toNative = NativeMappedConverter.getInstance(returnType);
        } else if (mapper != null) {
            toNative = mapper.getToNativeConverter(returnType);
        }
        for (int i = 0; i < fromNative.length; i++) {
            if (NativeMapped.class.isAssignableFrom(argTypes[i])) {
                fromNative[i] = new NativeMappedConverter(argTypes[i]);
            } else if (mapper != null) {
                fromNative[i] = mapper.getFromNativeConverter(argTypes[i]);
            }
        }
    }

    /**
     * Called from native code.  All arguments are in an array of
     * Object as the first argument.  Converts all arguments to types
     * required by the actual callback method signature, and converts
     * the result back into an appropriate native type.
     * This method <em>must not</em> throw exceptions.
     */
    public Object callback(Object[] args) {
        try {
            Object[] callbackArgs = new Object[args.length];
            Class[] parameterTypes = chained.getParameterTypes();

            // convert basic supported types to appropriate Java parameter types
            for (int i = 0; i < args.length; i++) {
                Class type = parameterTypes[i];
                Object arg = args[i];
                if (fromNative[i] != null) {
                    FromNativeContext context = null;
                            // TODO: need to work out the best way to do contexts
                            // new CallbackParameterContext(type, callbackMethod, args, i);
                    callbackArgs[i] = fromNative[i].fromNative(arg, context);
                } else {
                    callbackArgs[i] = convertArgument(arg, type);
                }
            }

            Object result = null;

            result = convertResult(chained.callback(callbackArgs));

            // Synch any structure arguments back to native memory
            for (int i = 0; i < callbackArgs.length; i++) {
                if (callbackArgs[i] instanceof Structure
                        && !(callbackArgs[i] instanceof Structure.ByValue)) {
                    ((Structure) callbackArgs[i]).autoWrite();
                }
            }

            return result;
        }
        catch (Throwable t) {
            handleUncaughtException(t);
            return null;
        }
    }

    /**
     * Convert argument from its basic native type to the given
     * Java parameter type.
     */
    private Object convertArgument(Object value, Class dstType) {
        if (value instanceof Pointer) {
            if (dstType == String.class) {
                value = ((Pointer) value).getString(0);
            } else if (dstType == WString.class) {
                value = new WString(((Pointer) value).getString(0, true));
            } else if (dstType == String[].class
                    || dstType == WString[].class) {
                value = ((Pointer) value).getStringArray(0, dstType == WString[].class);
            } else if (Callback.class.isAssignableFrom(dstType)) {
                value = CallbackReference.getCallback(dstType, (Pointer) value);
            } else if (Structure.class.isAssignableFrom(dstType)) {
                Structure s = Structure.newInstance(dstType);
                // If passed by value, don't hold onto the pointer, which
                // is only valid for the duration of the callback call
                if (Structure.ByValue.class.isAssignableFrom(dstType)) {
                    byte[] buf = new byte[s.size()];
                    ((Pointer) value).read(0, buf, 0, buf.length);
                    s.getPointer().write(0, buf, 0, buf.length);
                } else {
                    s.useMemory((Pointer) value);
                }
                s.read();
                value = s;
            }
        } else if ((boolean.class == dstType || Boolean.class == dstType)
                && value instanceof Number) {
            value = Function.valueOf(((Number) value).intValue() != 0);
        }
        return value;
    }

    private Object convertResult(Object value) {
        if (toNative != null) {
            value = toNative.toNative(value, null); //new CallbackResultContext(callbackMethod));
        }
        if (value == null)
            return null;
        Class cls = value.getClass();
        if (Structure.class.isAssignableFrom(cls)) {
            if (Structure.ByValue.class.isAssignableFrom(cls)) {
                return value;
            }
            return ((Structure) value).getPointer();
        } else if (cls == boolean.class || cls == Boolean.class) {
            return Boolean.TRUE.equals(value) ?
                    Function.INTEGER_TRUE : Function.INTEGER_FALSE;
        } else if (cls == String.class || cls == WString.class) {
            return CallbackReference.getNativeString(value, cls == WString.class);
        } else if (cls == String[].class || cls == WString.class) {
            StringArray sa = cls == String[].class
                    ? new StringArray((String[]) value)
                    : new StringArray((WString[]) value);
            // Delay GC until array itself is GC'd.
            CallbackReference.allocations.put(value, sa);
            return sa;
        } else if (Callback.class.isAssignableFrom(cls)) {
            return CallbackReference.getFunctionPointer((Callback) value);
        }
        return value;
    }

    public Class[] getParameterTypes() {
        return chained.getParameterTypes();
    }

    public Class getReturnType() {
        return chained.getReturnType();
    }

    private void handleUncaughtException(Throwable e) {
        // This is to pacify the existing exception handlers - which expect a Callback 
        Callback cb = new Callback() {
            public String toString() {
                return chained.toString();
            }
        };
        Native.getCallbackExceptionHandler().uncaughtException(cb, e);
    }
}
