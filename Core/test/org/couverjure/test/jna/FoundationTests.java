package org.couverjure.test.jna;

import org.couverjure.jna.FoundationLibrary;
import org.couverjure.jna.ObjectiveCRuntime;
import org.couverjure.jni.NativeHelper;
import com.sun.jna.Callback;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class FoundationTests {
    static FoundationLibrary foundation;
    static ObjectiveCRuntime rt;

    Pointer selInit = rt.sel_registerName("init");
    Pointer selDealloc = rt.sel_registerName("dealloc");
    Pointer selRelease = rt.sel_registerName("release");
    Pointer selLength = rt.sel_registerName("length");
    Pointer selCharAtIndex = rt.sel_registerName("characterAtIndex:");

    @BeforeClass
    public static void loadLibraries() {
        foundation = (FoundationLibrary) Native.loadLibrary("Foundation", FoundationLibrary.class);
        rt = (ObjectiveCRuntime) Native.loadLibrary("Foundation", ObjectiveCRuntime.class);
        NativeHelper.initHelper();
    }

    @Test
    public void testNSLog() {
        foundation = (FoundationLibrary) Native.loadLibrary("Foundation", FoundationLibrary.class);
        Pointer hello = foundation.CFStringCreateWithCString(null, "Hello World", 0);
        foundation.NSLog(hello);
        foundation.CFRelease(hello);
    }

    @Test
    public void testCreateNSStringSubclass() {
        Pointer idClsNSString = rt.objc_getClass("NSString");
        Pointer idClass = rt.objc_allocateClassPair(idClsNSString, "CCHelloString", 0);
        final String hello = "Hello";
        Callback cbLength = new Callback() {
            public int callback(Pointer self, Pointer sel) {
                return 5;
            }
        };
        Callback cbCharAtIndex = new Callback() {
            public int callback(Pointer self, Pointer sel, int index) {
                return hello.charAt(index);
            }
        };
        boolean ok = rt.class_addMethod(idClass, selLength, cbLength, "I@:");
        ok = rt.class_addMethod(idClass, selCharAtIndex, cbCharAtIndex, "S@:I");
        rt.objc_registerClassPair(idClass);
        Pointer nsHello = rt.class_createInstance(idClass, 0);
        rt.objc_msgSend(nsHello, selInit);
        foundation.NSLog(nsHello);
        rt.objc_msgSend(nsHello, selRelease);
    }


    public static int finalizerCount = 0;

    public static class TestStateClass {
        public TestStateClass() {

        }

        public void finalize() throws Throwable {
            synchronized (FoundationTests.class) {
                finalizerCount++;
            }
            super.finalize();
        }
    }

    @Test
    public void testInstanceVariable() {
        final Pointer idClass = createNSStringSubclass(TestStateClass.class);
        final int iterations = 1000;
        finalizerCount = 0;
        for (int i = 0; i < iterations; i++) {
            Pointer nsHello = rt.class_createInstance(idClass, 0);
            rt.objc_msgSend(nsHello, selInit);
            //foundation.NSLog(nsHello);
            rt.objc_msgSend(nsHello, selRelease);
        }
        System.gc();

        synchronized(this) {
            try {
                wait(1000L);
            } catch (InterruptedException e) {
            }
        }
        System.out.println(String.format("finalizerCount = %d", finalizerCount));
        Assert.assertTrue(finalizerCount > 0);
    }

    private Pointer createNSStringSubclass(final Class stateClass) {
        final Pointer idClsNSString = rt.objc_getClass("NSString");
        final Pointer idClass = rt.objc_allocateClassPair(idClsNSString, "CCHelloString2", 0);
        final String hello = "Hello";
        Callback cbLength = new Callback() {
            public int callback(Pointer self, Pointer sel) {
                return 5;
            }
        };
        Callback cbCharAtIndex = new Callback() {
            public int callback(Pointer self, Pointer sel, int index) {
                return hello.charAt(index);
            }
        };
        boolean ok;
        ok = rt.class_addMethod(idClass, selLength, cbLength, "I@:");
        ok = rt.class_addMethod(idClass, selCharAtIndex, cbCharAtIndex, "S@:I");

        ok = rt.class_addIvar(idClass, "jstate", 8, 3, "?");
        {
            Callback cbInit = new Callback() {
                public Pointer callback(Pointer self, Pointer sel) {
                    //System.out.println("cbInit");
                    self = rt.objc_msgSendSuper(new ObjectiveCRuntime.Super(self, idClsNSString), sel);
                    if (self != null) {
                        //System.out.println("setJavaIvar");
                        Pointer ivarState = rt.class_getInstanceVariable(idClass, "jstate");
                        Object state;
                        try {
                            state = stateClass.newInstance();
                            NativeHelper.setJavaIvar(self, ivarState, state);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Assert.fail();
                        }
                    }
                    return self;
                }
            };
            ok = rt.class_addMethod(idClass, selInit, cbInit, "@@:");
        }
        Callback cbDealloc = new Callback() {
            public void callback(Pointer self, Pointer sel) {
                //System.out.println("cbDealloc");
                Pointer ivarState = rt.class_getInstanceVariable(idClass, "jstate");
                NativeHelper.releaseJavaIvar(self, ivarState);
            }
        };
        ok = rt.class_addMethod(idClass, selDealloc, cbDealloc, "v@:");
        rt.objc_registerClassPair(idClass);
        return idClass;
    }
}
