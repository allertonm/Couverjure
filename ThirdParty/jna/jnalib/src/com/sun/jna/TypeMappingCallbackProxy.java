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
 * TypeMappingCallbackProxy is an implementation of CallbackProxy that handles argument
 * and return type marshalling in the same way as done for implementations of Callback.
 * The abstract method typeMappedCallback will be invoked with the mapped arguments.
 *
 * This is intended for use by dynamic languages where JNA's standard marshalling
 * for structures etc makes sense.
 */
public abstract class TypeMappingCallbackProxy implements CallbackProxy {
    private Class returnType;
    private Class[] parameterTypes;
    private ToNativeConverter toNative;
    private FromNativeConverter[] fromNative;

    public TypeMappingCallbackProxy(Class returnType, Class[] parameterTypes, TypeMapper mapper) {
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;

        fromNative = new FromNativeConverter[parameterTypes.length];
        if (NativeMapped.class.isAssignableFrom(returnType)) {
            toNative = NativeMappedConverter.getInstance(returnType);
        } else if (mapper != null) {
            toNative = mapper.getToNativeConverter(returnType);
        }
        for (int i = 0; i < fromNative.length; i++) {
            if (NativeMapped.class.isAssignableFrom(parameterTypes[i])) {
                fromNative[i] = new NativeMappedConverter(parameterTypes[i]);
            } else if (mapper != null) {
                fromNative[i] = mapper.getFromNativeConverter(parameterTypes[i]);
            }
        }
    }

    /**
     * Subclasses should override typeMappedCallback rather than callback - this
     * method will be invoked with the mapped arguments, and its return value will also
     * be marshalled using the type mapper.
     * @param args, mapped using the type mapper
     * @return return value
     */
    public abstract Object typeMappedCallback(Object[] args);

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

            result = convertResult(typeMappedCallback(callbackArgs));

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
     * This implementation returns the parameter types supplied to the constructor
     * @return the classes of the callback parameters
     */
    public Class[] getParameterTypes() {
        return (Class[]) parameterTypes.clone();
    }

    /**
     * This implementation returns the return type supplied to the constructor
     * @return the return type class
     */
    public Class getReturnType() {
        return returnType;
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

    /**
     * Subclasses that hold an actual Callback reference may override this method
     * to call Native.getCallbackExceptionHandler().uncaughtException directly with
     * the callback.
     * @param e
     */
    protected void handleUncaughtException(Throwable e) {
        // This is to pacify the existing exception handlers - which expect a Callback 
        Callback cb = new Callback() {
            public String toString() {
                return TypeMappingCallbackProxy.this.toString();
            }
        };
        Native.getCallbackExceptionHandler().uncaughtException(cb, e);
    }
}
