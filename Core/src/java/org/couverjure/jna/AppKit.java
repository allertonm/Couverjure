package org.couverjure.jna;

import com.sun.jna.Library;

public interface AppKit extends Library {
    int NSApplicationMain(int argc, String[] argv);
}
