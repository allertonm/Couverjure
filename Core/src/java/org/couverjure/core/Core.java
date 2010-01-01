package org.couverjure.core;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import org.couverjure.jna.FoundationLibrary;
import org.couverjure.jna.ObjectiveCRuntime;
import org.couverjure.jni.NativeHelper;

public class Core {
    public static FoundationLibrary foundation;
    public static ObjectiveCRuntime objcRuntime;

    static {
        foundation = (FoundationLibrary) Native.loadLibrary("Foundation", FoundationLibrary.class);
        objcRuntime = (ObjectiveCRuntime) Native.loadLibrary("Foundation", ObjectiveCRuntime.class);
        NativeHelper.initHelper();
    }

    public static ID getClass(String name) {
        return id(objcRuntime.objc_getClass(name));
    }

    public static ID createInstance(ID idClass) {
        return id(objcRuntime.class_createInstance(idClass.getNativeId(), 0));
    }

    private static ID id(Pointer pointer) {
        return new RetainReleaseID(pointer);
    }
}
