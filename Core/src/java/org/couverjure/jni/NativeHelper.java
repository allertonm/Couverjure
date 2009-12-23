package org.couverjure.jni;

import com.sun.jna.Pointer;

public class NativeHelper {
    static {
        System.loadLibrary("couverjure");
    }
    public static native void initHelper();
    public static native void setJavaIvar(Pointer idObject, Pointer ivar, Object object);
    public static native void releaseJavaIvar(Pointer idObject, Pointer ivar);
}
