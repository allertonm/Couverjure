package org.couverjure.jna;

import com.sun.jna.*;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import org.couverjure.core.*;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Jan 24, 2010
 * Time: 12:18:17 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Foundation extends Library {
    void NSLog(FoundationPointer msg);
    void CFRelease(FoundationPointer ref);
    void CFRetain(FoundationPointer ref);
    ID CFStringCreateWithCString(FoundationPointer allocator, String string, int encoding);

    // ObjC runtime methods

    ClassID objc_getClass(String name);
    ClassID objc_allocateClassPair(ClassID idSuperClass, String name, int extraBytes);
    void objc_registerClassPair(ClassID idClass);

    ID objc_msgSend(ID receiver, Selector selector, Object... args);
    ID objc_msgSendSuper(Super objc_super, Selector sel, Object... args);

    boolean class_addMethod(ClassID idClass, Selector selName, Callback imp, String types);
    boolean class_addIvar(ClassID idClass, String name, int size, int align, String type);
    ID class_createInstance(ClassID idClass, int extraBytes);
    Method class_getInstanceMethod(ClassID idClass, Selector sel);
    Method class_getClassMethod(ClassID idClass, Selector selector);
    Ivar class_getInstanceVariable(ClassID idClass, String s);
    ClassID class_getSuperclass(ClassID idClass);
    Pointer class_copyMethodList(ClassID idClass, LongByReference outCount);

    Selector sel_registerName(String s);
    String sel_getName(Selector sel);

    String method_getTypeEncoding(Method method);
    Selector method_getName(Method method);

    ClassID object_getClass(ID object);
    //Pointer object_getInstanceVariable(ID object, String name, PointerByReference outValue);
    //Pointer object_setInstanceVariable(long object, String name, long value);

    public static class Super extends Structure {
        public ID receiver;
        public ID clazz;

        public Super(ID receiver, ID clazz) {
            this.receiver = receiver;
            this.clazz = clazz;
        }
    }

    public interface MethodCallbackProxy extends CallbackProxy {
    }
}
