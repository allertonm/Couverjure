package org.couverjure.core;

import com.sun.jna.Native;
import com.sun.jna.ptr.LongByReference;
import org.couverjure.jna.Foundation64;
import org.couverjure.jna.ObjectiveCRuntime64;
import org.couverjure.jni.NativeHelper64;

import java.lang.reflect.Type;

public class Core64 {
    public Foundation64 foundation;
    public ObjectiveCRuntime64 objcRuntime;
    public NativeHelper64 nativeHelper;
    public Type pointerType = Long.TYPE;
    public Type superType = ObjectiveCRuntime64.Super.class;
    public Type idType = RefCountedId64.class;
    public long pointerSize = 8;
    public int pointerAlign = 3;

    public static final Core64 CORE = new Core64();

    public Core64() {
        foundation = (Foundation64) Native.loadLibrary("Foundation", Foundation64.class);
        objcRuntime = (ObjectiveCRuntime64) Native.loadLibrary("Foundation", ObjectiveCRuntime64.class);
        nativeHelper = new NativeHelper64();
        nativeHelper.initHelper();
    }

    public ID id(long nativeId) {
        return new RefCountedId64(nativeId);
    }

    public ObjectiveCRuntime64.Super makeSuper(long receiver, long clazz) {
        return new ObjectiveCRuntime64.Super(receiver, clazz); 
    }

    public LongByReference pointerByReference() {
        return new LongByReference();
    }
}
