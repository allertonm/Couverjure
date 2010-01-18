package org.couverjure.jni;

public class NativeHelper64 {
    static {
        System.loadLibrary("couverjure");
    }

    public NativeHelper64() {}
    
    public native void initHelper();
    public native void setJavaIvar(long idObject, long ivar, Object object);
    public native void setJavaIvarByName(long idObject, String ivarName, Object object);
    public native void releaseJavaIvar(long idObject, long ivar);
    public native void releaseJavaIvarByName(long idObject, String ivarName);
    public native Object getJavaIvarByName(long idObject, String ivarName);
}
