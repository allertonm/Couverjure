package org.couverjure.core;

import com.sun.jna.Pointer;

public class RetainReleaseID implements ID {
    protected Pointer nativeId;

    public RetainReleaseID(Pointer nativeId) {
        this.nativeId = nativeId;
    }

    public void retain() {
        Core.CORE.foundation.CFRetain(nativeId);
    }

    public void finalize() throws Throwable {
        Core.CORE.foundation.CFRelease(nativeId);
        super.finalize();
    }

    public Pointer getNativeId() {
        return nativeId;
    }
}
