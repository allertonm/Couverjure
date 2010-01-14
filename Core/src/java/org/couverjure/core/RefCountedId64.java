package org.couverjure.core;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Jan 14, 2010
 * Time: 12:23:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class RefCountedId64 implements ID {
    private long nativeId;

    public RefCountedId64(long nativeId) {
        this.nativeId = nativeId;
    }

    public void retain() {
        Core64.CORE.foundation.CFRetain(nativeId);
    }

    public void finalize() throws Throwable {
        Core64.CORE.foundation.CFRelease(nativeId);
        super.finalize();
    }

    public long getNativeId() {
        return nativeId;
    }
}
