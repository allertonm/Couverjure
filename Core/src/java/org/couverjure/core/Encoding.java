package org.couverjure.core;

import com.sun.jna.Pointer;

/**
 * Utility functions for dealing with Objective-C type encoding
 */
public class Encoding {
    public static Class encodingToClass(char encoding) {
        Class c;
        switch (encoding) {
            case 'c': return Byte.TYPE;
            case 'i': return Integer.TYPE;
            case 's': return Short.TYPE;
            case 'l': return Integer.TYPE;
            case 'q': return Long.TYPE;
            case 'C': return Byte.TYPE;
            case 'I': return Integer.TYPE;
            case 'S': return Short.TYPE;
            case 'L': return Integer.TYPE;
            case 'Q': return Long.TYPE;
            case 'f': return Float.TYPE;
            case 'd': return Double.TYPE;
            case 'v': return Void.TYPE;
            case '*': return String.class;
            case '@': return Pointer.class;
            case '#': return Pointer.class;
            case ':': return Pointer.class;
            case '?': return Pointer.class;
            default: return null;
        }
    }

    public static Class[] encodingToClasses(String encoding) {
        final int length = encoding.length();
        Class[] classes = new Class[length];
        for (int i = 0; i < length; i++) {
            classes[i] = encodingToClass(encoding.charAt(i));
        }
        return classes;
    }

    public static String classToEncoding(Class cls) {
        if (cls == Byte.TYPE || cls == Byte.class) return "c";
        if (cls == Integer.TYPE || cls == Integer.class) return "i";
        if (cls == Short.TYPE || cls == Short.class) return "s";
        if (cls == Long.TYPE || cls == Long.class) return "q";
        if (cls == Float.TYPE || cls == Float.class) return "f";
        if (cls == Double.TYPE || cls == Double.class) return "d";
        if (cls == Void.TYPE || cls == Void.class) return "v";
        if (cls == ID.class) return "@";
        if (cls == String.class) return "*";
        if (cls == Pointer.class) return "?";
        return null;
    }
}
