package org.couverjure.core;

import com.sun.jna.Pointer;
import org.couverjure.core.ID;

public class IvarHelper {
    static {
        System.loadLibrary("couverjure");
    }

    public IvarHelper() {}

    public native void initHelper();
    public native void setJavaIvar(ID idObject, Pointer ivar, Object object);
    public native void setJavaIvarByName(ID idObject, String ivarName, Object object);
    public native void releaseJavaIvar(ID idObject, Pointer ivar);
    public native void releaseJavaIvarByName(ID idObject, String ivarName);
    public native Object getJavaIvarByName(ID idObject, String ivarName);
}
