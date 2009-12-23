package org.couverjure.jna;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public interface ObjectiveCRuntime extends Library {
    Pointer objc_getClass(String name);
    Pointer objc_allocateClassPair(Pointer idSuperClass, String name, int extraBytes);
    void objc_registerClassPair(Pointer idClass);
    Pointer objc_msgSend(Pointer receiver, Pointer selector);
    Pointer objc_msgSendSuper(Super objc_super, Pointer sel);
    boolean class_addMethod(Pointer idClass, Pointer selName, Callback imp, String types);
    boolean class_addIvar(Pointer idClass, String name, int size, int align, String type);
    Pointer class_createInstance(Pointer idClass, int extraBytes);
    Pointer class_getInstanceVariable(Pointer idClass, String s);
    Pointer sel_registerName(String s);


    public static class Super extends Structure {
        public Pointer receiver;
        public Pointer clazz;

        public Super(Pointer receiver, Pointer clazz) {
            this.receiver = receiver;
            this.clazz = clazz;
        }
    }
}
