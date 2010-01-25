package org.couverjure.core;

public class FoundationPointer {
    protected long address;

    public FoundationPointer(long address) {
        this.address = address;
    }

    public long getAddress() {
        return address;
    }
}
