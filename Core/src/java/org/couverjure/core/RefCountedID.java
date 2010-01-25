package org.couverjure.core;


public class RefCountedID extends ID {
    public RefCountedID(long address) {
        super(address);
    }

    public void retain() {
        Core.CORE.foundation.CFRetain(this);
    }

    public void finalize() throws Throwable {
        Core.CORE.foundation.CFRelease(this);
        super.finalize();
    }
}
