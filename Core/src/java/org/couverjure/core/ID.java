package org.couverjure.core;

import com.sun.jna.Pointer;

public class ID extends Pointer {
    private boolean releaseOnFinalize = true;

    public ID(long peer) {
        super(peer);
    }

    public void releaseOnFinalize() {
        this.releaseOnFinalize = true;
    }

    public void noReleaseOnFinalize() {
        this.releaseOnFinalize = false;
    }

    public boolean asBoolean() {
        noReleaseOnFinalize();
        return peer != 0;
    }

    public int asInt() {
        noReleaseOnFinalize();
        return (int) peer;
    }

    public short asShort() {
        noReleaseOnFinalize();
        return (short) peer;
    }

    public long asLong() {
        noReleaseOnFinalize();
        return peer;
    }

    public long getAddress() {
        return peer;
    }

    public ID retain() {
        if (Core.DEBUG) System.out.println(String.format("ID.retain %s", this));
        Core.CORE.foundation.CFRetain(this);
        return this;
    }

    public void finalize() throws Throwable {
        if (Core.DEBUG) System.out.println(String.format("ID.finalize %s", this));
        if (releaseOnFinalize) {
            Core.CORE.foundation.CFRelease(this);
        }
        super.finalize();
    }

}
