package org.couverjure.core;

import com.sun.jna.Pointer;

public class RetainReleaseID extends ID {
    public RetainReleaseID(Pointer nativeId) {
        super(nativeId);
    }

    public void retain() {
        Core.foundation.CFRetain(nativeId);
    }

    public void finalize() throws Throwable {
        Core.foundation.CFRelease(nativeId);
        super.finalize();
    }
}
