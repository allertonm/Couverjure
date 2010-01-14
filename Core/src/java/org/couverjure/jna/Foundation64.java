package org.couverjure.jna;

import com.sun.jna.Library;

public interface Foundation64 extends Library {
    void NSLog(long msg);
    void CFRelease(long ref);
    void CFRetain(long ref);
    long CFStringCreateWithCString(long allocator, String string, int encoding);
}
