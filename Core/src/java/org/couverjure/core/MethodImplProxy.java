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
import org.couverjure.core.FoundationTypeMapper;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class MethodImplProxy extends TypeMappingCallbackProxy {
    public static TypeMapper TYPE_MAPPER = FoundationTypeMapper.getTypeMapper();

    private static Set<MethodImplProxy> proxies = new HashSet<MethodImplProxy>();

    private String debugName;

    // we keep references to all MethodImpls created because otherwise
    // refs will only be held on the native side, leaving them free to be GC'd
    // TODO: do we need a mechanism for releasing a method impl?
    private static void retain(MethodImplProxy proxy) {
        synchronized (proxies) {
            proxies.add(proxy);
        }
    }

    public MethodImplProxy(String debugName, Class returnType, Class[] parameterTypes) {
        super(returnType, parameterTypes, TYPE_MAPPER);
        this.debugName = debugName;
        retain(this);
    }

    public void finalize() {
        System.out.println("In MethodImplProxy.finalize! Ooops.");
    }
}
