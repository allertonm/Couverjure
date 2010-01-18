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
