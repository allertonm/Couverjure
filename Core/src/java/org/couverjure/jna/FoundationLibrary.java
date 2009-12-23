package org.couverjure.jna;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

/**
 * Created by IntelliJ IDEA.
 * User: mark
 * Date: Dec 18, 2009
 * Time: 4:12:11 PM
 * To change this template use File | Settings | File Templates.
 */
public interface FoundationLibrary extends Library {
    void NSLog(Pointer msg);
    void CFRelease(Pointer ref);
    void CFRetain(Pointer ref);
    Pointer CFStringCreateWithCString(Pointer allocator, String string, int encoding);
}
