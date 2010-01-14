package org.couverjure.core;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import org.couverjure.jna.Foundation64;
import org.couverjure.jna.FoundationLibrary;
import org.couverjure.jna.ObjectiveCRuntime;
import org.couverjure.jna.ObjectiveCRuntime64;
import org.couverjure.jni.NativeHelper;

import java.lang.reflect.Type;

public class Core64 {
    public Foundation64 foundation;
    public ObjectiveCRuntime64 objcRuntime;

    public static final Core64 CORE = new Core64();

    public Core64() {
        foundation = (Foundation64) Native.loadLibrary("Foundation", Foundation64.class);
        objcRuntime = (ObjectiveCRuntime64) Native.loadLibrary("Foundation", ObjectiveCRuntime64.class);
        NativeHelper.initHelper();
    }

    public ID id(long nativeId) {
        return new RefCountedId64(nativeId);
    }

    public Type pointerType() {
        return Long.TYPE;
    }
}
