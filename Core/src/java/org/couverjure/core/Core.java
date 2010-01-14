package org.couverjure.core;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import org.couverjure.jna.FoundationLibrary;
import org.couverjure.jna.ObjectiveCRuntime;
import org.couverjure.jni.NativeHelper;

import java.lang.reflect.Type;

public class Core {
    public FoundationLibrary foundation;
    public ObjectiveCRuntime objcRuntime;

    public static final Core CORE = new Core();

    public Core() {
        foundation = (FoundationLibrary) Native.loadLibrary("Foundation", FoundationLibrary.class);
        objcRuntime = (ObjectiveCRuntime) Native.loadLibrary("Foundation", ObjectiveCRuntime.class);
        NativeHelper.initHelper();
    }

    public ID id(Pointer nativeId) {
        return new RetainReleaseID(nativeId);
    }

    public Type pointerType() {
        return Pointer.class;
    }
}
