package org.couverjure.core;

public class ID extends FoundationPointer {
    public ID(long address) {
        super(address);
    }

    public ClassID asClassID() {
        return new ClassID(address);
    }

    public boolean asBoolean() {
        return address != 0;
    }

    public int asInt() {
        return (int) address;
    }

    public short asShort() {
        return (short) address;
    }

    public long asLong() {
        return address;
    }
}
