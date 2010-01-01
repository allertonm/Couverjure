package org.couverjure.core;

import com.sun.jna.Pointer;

public class ID {
    protected Pointer nativeId;

    public ID(Pointer nativeId) {
        this.nativeId = nativeId;
    }

    public Pointer getNativeId() {
        return nativeId;
    }
}
