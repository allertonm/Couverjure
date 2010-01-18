package org.couverjure.jna;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.LongByReference;

public interface ObjectiveCRuntime64 extends Library {
    long objc_getClass(String name);
    long objc_allocateClassPair(long idSuperClass, String name, int extraBytes);
    void objc_registerClassPair(long idClass);
    long objc_msgSend(long receiver, long selector);
    long objc_msgSend(long receiver, long selector, Object a1);
    long objc_msgSend(long receiver, long selector, Object... args);
    long objc_msgSendSuper(Super objc_super, long sel, Object... args);

    boolean class_addMethod(long idClass, long selName, Callback imp, String types);
    boolean class_addIvar(long idClass, String name, int size, int align, String type);
    long class_createInstance(long idClass, int extraBytes);
    long class_getInstanceMethod(long idClass, long sel);
    long class_getClassMethod(long idClass, Pointer selector);
    Pointer class_getInstanceVariable(long idClass, String s);
    long class_getSuperclass(long idClass);

    long sel_registerName(String s);

    String method_getTypeEncoding(long method);

    long object_getClass(long object);
    long object_getInstanceVariable(long object, String name, LongByReference outValue);
    long object_setInstanceVariable(long object, String name, long value);

    public static class Super extends Structure {
        public long receiver;
        public long clazz;

        public Super(long receiver, long clazz) {
            this.receiver = receiver;
            this.clazz = clazz;
        }
    }
}