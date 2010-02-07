/*
    Copyright 2010 Mark Allerton. All rights reserved.

    Redistribution and use in source and binary forms, with or without modification, are
    permitted provided that the following conditions are met:

       1. Redistributions of source code must retain the above copyright notice, this list of
          conditions and the following disclaimer.

       2. Redistributions in binary form must reproduce the above copyright notice, this list
          of conditions and the following disclaimer in the documentation and/or other materials
          provided with the distribution.

    THIS SOFTWARE IS PROVIDED BY MARK ALLERTON ``AS IS'' AND ANY EXPRESS OR IMPLIED
    WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
    FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
    CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
    SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
    ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
    ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

    The views and conclusions contained in the software and documentation are those of the
    authors and should not be interpreted as representing official policies, either expressed
    or implied, of Mark Allerton.
*/

package org.couverjure.core;

import com.sun.jna.*;
import com.sun.jna.ptr.LongByReference;
import org.couverjure.core.*;

/**
 * The Foundation class defines the interfaces on Foundation.framework that JNA will proxy to native
 */
public interface Foundation extends Library {
    void NSLog(ID msg);
    void CFRelease(ID ref);
    void CFRetain(ID ref);
    ID CFStringCreateWithCString(Pointer allocator, String string, int encoding);

    Pointer objc_getClass(String name);
    Pointer objc_allocateClassPair(Pointer supercls, String name, int extraBytes);
    void objc_registerClassPair(Pointer cls);

    ID objc_msgSend(Pointer receiver, Pointer selector, Object... args);
    ID objc_msgSendSuper(Super objc_super, Pointer sel, Object... args);

    boolean class_addMethod(Pointer cls, Pointer selName, Callback imp, String types);
    boolean class_addIvar(Pointer cls, String name, int size, int align, String type);
    ID class_createInstance(Pointer idClass, int extraBytes);
    Pointer class_getInstanceMethod(Pointer idClass, Pointer sel);
    Pointer class_getClassMethod(Pointer idClass, Pointer selector);
    Pointer class_getInstanceVariable(Pointer idClass, String s);
    Pointer class_getSuperclass(Pointer idClass);
    Pointer class_copyMethodList(Pointer idClass, LongByReference outCount);

    Pointer sel_registerName(String s);
    String sel_getName(Pointer sel);

    String method_getTypeEncoding(Pointer method);
    Pointer method_getName(Pointer method);

    Pointer object_getClass(ID object);
    //Pointer object_getInstanceVariable(ID object, String name, PointerByReference outValue);
    //Pointer object_setInstanceVariable(long object, String name, long value);

    public static class Super extends Structure {
        public ID receiver;
        public Pointer supercls;

        public Super(ID receiver, Pointer supercls) {
            this.receiver = receiver;
            this.supercls = supercls;
        }
    }

    public static class NSRange extends Structure {
        public static final String ENCODING = "^{NSRange=QQ}";

        public NSRange(long location, long length) {
            this.location = location;
            this.length = length;
        }

        public NSRange() {
        }

        public long location;
        public long length;

        public static class ByValue extends NSRange implements Structure.ByValue {
            public static final String ENCODING = "{NSRange=QQ}";

            public ByValue() {
            }

            public ByValue(long location, long length) {
                super(location, length);
            }
        }

        public static class ByReference extends NSRange implements Structure.ByReference {
            public static final String ENCODING = "^^{NSRange}";

            public ByReference() {
            }

            public ByReference(long location, long length) {
                super(location, length);
            }
        }
    }
}
