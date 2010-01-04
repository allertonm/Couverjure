package org.cocoaclojure.jna;
/**
 * JNA Wrapper for library <b>runtime</b><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.free.fr/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a>, <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public interface RuntimeLibrary extends com.sun.jna.Library {
	public static final java.lang.String JNA_LIBRARY_NAME = com.ochafik.lang.jnaerator.runtime.LibraryExtractor.getLibraryPath("runtime", true, org.cocoaclojure.jna.RuntimeLibrary.class);
	public static final com.sun.jna.NativeLibrary JNA_NATIVE_LIB = com.sun.jna.NativeLibrary.getInstance(org.cocoaclojure.jna.RuntimeLibrary.JNA_LIBRARY_NAME, com.ochafik.lang.jnaerator.runtime.MangledFunctionMapper.DEFAULT_OPTIONS);
	public static final org.cocoaclojure.jna.RuntimeLibrary INSTANCE = (org.cocoaclojure.jna.RuntimeLibrary)com.sun.jna.Native.loadLibrary(org.cocoaclojure.jna.RuntimeLibrary.JNA_LIBRARY_NAME, org.cocoaclojure.jna.RuntimeLibrary.class, com.ochafik.lang.jnaerator.runtime.MangledFunctionMapper.DEFAULT_OPTIONS);
	public static final char _C_ID = '@';
	public static final char _C_CLASS = '#';
	public static final char _C_SEL = ':';
	public static final char _C_CHR = 'c';
	public static final char _C_UCHR = 'C';
	public static final char _C_SHT = 's';
	public static final char _C_USHT = 'S';
	public static final char _C_INT = 'i';
	public static final char _C_UINT = 'I';
	public static final char _C_LNG = 'l';
	public static final char _C_ULNG = 'L';
	public static final char _C_LNG_LNG = 'q';
	public static final char _C_ULNG_LNG = 'Q';
	public static final char _C_FLT = 'f';
	public static final char _C_DBL = 'd';
	public static final char _C_BFLD = 'b';
	public static final char _C_BOOL = 'B';
	public static final char _C_VOID = 'v';
	public static final char _C_UNDEF = '?';
	public static final char _C_PTR = '^';
	public static final char _C_CHARPTR = '*';
	public static final char _C_ATOM = '%';
	public static final char _C_ARY_B = '[';
	public static final char _C_ARY_E = ']';
	public static final char _C_UNION_B = '(';
	public static final char _C_UNION_E = ')';
	public static final char _C_STRUCT_B = '{';
	public static final char _C_STRUCT_E = '}';
	public static final char _C_VECTOR = '!';
	public static final char _C_CONST = 'r';
	public static final int CLS_CLASS = 1;
	public static final int CLS_META = 2;
	public static final int CLS_INITIALIZED = 4;
	public static final int CLS_POSING = 8;
	public static final int CLS_MAPPED = 16;
	public static final int CLS_FLUSH_CACHE = 32;
	public static final int CLS_GROW_CACHE = 64;
	public static final int CLS_NEED_BIND = 128;
	public static final int CLS_METHOD_ARRAY = 256;
	public static final int CLS_JAVA_HYBRID = 512;
	public static final int CLS_JAVA_CLASS = 1024;
	public static final int CLS_INITIALIZING = 2048;
	public static final int CLS_FROM_BUNDLE = 4096;
	public static final int CLS_HAS_CXX_STRUCTORS = 8192;
	public static final int CLS_NO_METHOD_ARRAY = 16384;
	public static final int CLS_HAS_LOAD_METHOD = 32768;
	public static final int CLS_CONSTRUCTING = 65536;
	public static final int CLS_EXT = 131072;
	public static final int OBSOLETE_OBJC_GETCLASSES = 1;
	public static final int OBJC_NEXT_METHOD_LIST = 1;
	public interface handler extends com.sun.jna.Callback {
		void invoke(org.rococoa.ObjCObject id1);
	}
	public interface objc_setClassHandler_arg1_callback extends com.sun.jna.Callback {
		int invoke(com.sun.jna.Pointer charPtr1);
	}
	public interface _alloc_callback extends com.sun.jna.Callback {
		org.rococoa.ObjCObject invoke(org.rococoa.ObjCClass Class1, com.ochafik.lang.jnaerator.runtime.NativeSize size_t1);
	}
	public interface _copy_callback extends com.sun.jna.Callback {
		org.rococoa.ObjCObject invoke(org.rococoa.ObjCObject id1, com.ochafik.lang.jnaerator.runtime.NativeSize size_t1);
	}
	public interface _realloc_callback extends com.sun.jna.Callback {
		org.rococoa.ObjCObject invoke(org.rococoa.ObjCObject id1, com.ochafik.lang.jnaerator.runtime.NativeSize size_t1);
	}
	public interface _dealloc_callback extends com.sun.jna.Callback {
		org.rococoa.ObjCObject invoke(org.rococoa.ObjCObject id1);
	}
	public interface _zoneAlloc_callback extends com.sun.jna.Callback {
		org.rococoa.ObjCObject invoke(org.rococoa.ObjCClass Class1, com.ochafik.lang.jnaerator.runtime.NativeSize size_t1, com.sun.jna.Pointer voidPtr1);
	}
	public interface _zoneRealloc_callback extends com.sun.jna.Callback {
		org.rococoa.ObjCObject invoke(org.rococoa.ObjCObject id1, com.ochafik.lang.jnaerator.runtime.NativeSize size_t1, com.sun.jna.Pointer voidPtr1);
	}
	public interface _zoneCopy_callback extends com.sun.jna.Callback {
		org.rococoa.ObjCObject invoke(org.rococoa.ObjCObject id1, com.ochafik.lang.jnaerator.runtime.NativeSize size_t1, com.sun.jna.Pointer voidPtr1);
	}
	public interface _error_callback extends com.sun.jna.Callback {
		void invoke(org.rococoa.ObjCObject id1, com.sun.jna.Pointer charPtr1);
	}
	org.rococoa.ObjCObject object_copy(org.rococoa.ObjCObject obj, com.ochafik.lang.jnaerator.runtime.NativeSize size);
	org.rococoa.ObjCObject object_dispose(org.rococoa.ObjCObject obj);
	org.rococoa.ObjCClass object_getClass(org.rococoa.ObjCObject obj);
	org.rococoa.ObjCClass object_setClass(org.rococoa.ObjCObject obj, org.rococoa.ObjCClass cls);
	com.sun.jna.Pointer object_getClassName(org.rococoa.ObjCObject obj);
	com.sun.jna.Pointer object_getIndexedIvars(org.rococoa.ObjCObject obj);
	org.rococoa.ObjCObject object_getIvar(org.rococoa.ObjCObject obj, org.cocoaclojure.jna.objc_ivar ivar);
	void object_setIvar(org.rococoa.ObjCObject obj, org.cocoaclojure.jna.objc_ivar ivar, org.rococoa.ObjCObject value);
	@java.lang.Deprecated
	org.cocoaclojure.jna.objc_ivar object_setInstanceVariable(org.rococoa.ObjCObject obj, com.sun.jna.Pointer name, com.sun.jna.Pointer value);
	org.cocoaclojure.jna.objc_ivar object_setInstanceVariable(org.rococoa.ObjCObject obj, java.lang.String name, com.sun.jna.Pointer value);
	@java.lang.Deprecated
	org.cocoaclojure.jna.objc_ivar object_getInstanceVariable(org.rococoa.ObjCObject obj, com.sun.jna.Pointer name, com.sun.jna.ptr.PointerByReference outValue);
	org.cocoaclojure.jna.objc_ivar object_getInstanceVariable(org.rococoa.ObjCObject obj, java.lang.String name, com.sun.jna.ptr.PointerByReference outValue);
	@java.lang.Deprecated
	org.rococoa.ObjCObject objc_getClass(com.sun.jna.Pointer name);
	org.rococoa.ObjCObject objc_getClass(java.lang.String name);
	@java.lang.Deprecated
	org.rococoa.ObjCObject objc_getMetaClass(com.sun.jna.Pointer name);
	org.rococoa.ObjCObject objc_getMetaClass(java.lang.String name);
	@java.lang.Deprecated
	org.rococoa.ObjCObject objc_lookUpClass(com.sun.jna.Pointer name);
	org.rococoa.ObjCObject objc_lookUpClass(java.lang.String name);
	@java.lang.Deprecated
	org.rococoa.ObjCObject objc_getRequiredClass(com.sun.jna.Pointer name);
	org.rococoa.ObjCObject objc_getRequiredClass(java.lang.String name);
	@java.lang.Deprecated
	org.rococoa.ObjCClass objc_getFutureClass(com.sun.jna.Pointer name);
	org.rococoa.ObjCClass objc_getFutureClass(java.lang.String name);
	@java.lang.Deprecated
	void objc_setFutureClass(org.rococoa.ObjCClass cls, com.sun.jna.Pointer name);
	void objc_setFutureClass(org.rococoa.ObjCClass cls, java.lang.String name);
	int objc_getClassList(org.rococoa.ObjCClass buffer, int bufferCount);
	@java.lang.Deprecated
	org.cocoaclojure.jna.RuntimeLibrary.objc_object objc_getProtocol(com.sun.jna.Pointer name);
	org.cocoaclojure.jna.RuntimeLibrary.objc_object objc_getProtocol(java.lang.String name);
	@java.lang.Deprecated
	org.cocoaclojure.jna.RuntimeLibrary.objc_object[] objc_copyProtocolList(com.sun.jna.ptr.IntByReference outCount);
	org.cocoaclojure.jna.RuntimeLibrary.objc_object[] objc_copyProtocolList(java.nio.IntBuffer outCount);
	com.sun.jna.Pointer class_getName(org.rococoa.ObjCClass cls);
	boolean class_isMetaClass(org.rococoa.ObjCClass cls);
	org.rococoa.ObjCClass class_getSuperclass(org.rococoa.ObjCClass cls);
	org.rococoa.ObjCClass class_setSuperclass(org.rococoa.ObjCClass cls, org.rococoa.ObjCClass newSuper);
	int class_getVersion(org.rococoa.ObjCClass cls);
	void class_setVersion(org.rococoa.ObjCClass cls, int version);
	com.ochafik.lang.jnaerator.runtime.NativeSize class_getInstanceSize(org.rococoa.ObjCClass cls);
	@java.lang.Deprecated
	org.cocoaclojure.jna.objc_ivar class_getInstanceVariable(org.rococoa.ObjCClass cls, com.sun.jna.Pointer name);
	org.cocoaclojure.jna.objc_ivar class_getInstanceVariable(org.rococoa.ObjCClass cls, java.lang.String name);
	@java.lang.Deprecated
	org.cocoaclojure.jna.objc_ivar class_getClassVariable(org.rococoa.ObjCClass cls, com.sun.jna.Pointer name);
	org.cocoaclojure.jna.objc_ivar class_getClassVariable(org.rococoa.ObjCClass cls, java.lang.String name);
	@java.lang.Deprecated
	org.cocoaclojure.jna.objc_ivar.ByReference[] class_copyIvarList(org.rococoa.ObjCClass cls, com.sun.jna.ptr.IntByReference outCount);
	org.cocoaclojure.jna.objc_ivar.ByReference[] class_copyIvarList(org.rococoa.ObjCClass cls, java.nio.IntBuffer outCount);
	org.cocoaclojure.jna.objc_method class_getInstanceMethod(org.rococoa.ObjCClass cls, org.rococoa.Selector name);
	org.cocoaclojure.jna.objc_method class_getClassMethod(org.rococoa.ObjCClass cls, org.rococoa.Selector name);
	com.sun.jna.Pointer class_getMethodImplementation(org.rococoa.ObjCClass cls, org.rococoa.Selector name);
	com.sun.jna.Pointer class_getMethodImplementation_stret(org.rococoa.ObjCClass cls, org.rococoa.Selector name);
	boolean class_respondsToSelector(org.rococoa.ObjCClass cls, org.rococoa.Selector sel);
	@java.lang.Deprecated
	org.cocoaclojure.jna.objc_method.ByReference[] class_copyMethodList(org.rococoa.ObjCClass cls, com.sun.jna.ptr.IntByReference outCount);
	org.cocoaclojure.jna.objc_method.ByReference[] class_copyMethodList(org.rococoa.ObjCClass cls, java.nio.IntBuffer outCount);
	boolean class_conformsToProtocol(org.rococoa.ObjCClass cls, org.cocoaclojure.jna.RuntimeLibrary.objc_object protocol);
	@java.lang.Deprecated
	org.cocoaclojure.jna.RuntimeLibrary.objc_object[] class_copyProtocolList(org.rococoa.ObjCClass cls, com.sun.jna.ptr.IntByReference outCount);
	org.cocoaclojure.jna.RuntimeLibrary.objc_object[] class_copyProtocolList(org.rococoa.ObjCClass cls, java.nio.IntBuffer outCount);
	@java.lang.Deprecated
	org.cocoaclojure.jna.RuntimeLibrary.objc_property class_getProperty(org.rococoa.ObjCClass cls, com.sun.jna.Pointer name);
	org.cocoaclojure.jna.RuntimeLibrary.objc_property class_getProperty(org.rococoa.ObjCClass cls, java.lang.String name);
	@java.lang.Deprecated
	org.cocoaclojure.jna.RuntimeLibrary.objc_property[] class_copyPropertyList(org.rococoa.ObjCClass cls, com.sun.jna.ptr.IntByReference outCount);
	org.cocoaclojure.jna.RuntimeLibrary.objc_property[] class_copyPropertyList(org.rococoa.ObjCClass cls, java.nio.IntBuffer outCount);
	com.sun.jna.Pointer class_getIvarLayout(org.rococoa.ObjCClass cls);
	com.sun.jna.Pointer class_getWeakIvarLayout(org.rococoa.ObjCClass cls);
	org.rococoa.ObjCObject class_createInstance(org.rococoa.ObjCClass cls, com.ochafik.lang.jnaerator.runtime.NativeSize extraBytes);
	@java.lang.Deprecated
	org.rococoa.ObjCClass objc_allocateClassPair(org.rococoa.ObjCClass superclass, com.sun.jna.Pointer name, com.ochafik.lang.jnaerator.runtime.NativeSize extraBytes);
	org.rococoa.ObjCClass objc_allocateClassPair(org.rococoa.ObjCClass superclass, java.lang.String name, com.ochafik.lang.jnaerator.runtime.NativeSize extraBytes);
	void objc_registerClassPair(org.rococoa.ObjCClass cls);
	@java.lang.Deprecated
	org.rococoa.ObjCClass objc_duplicateClass(org.rococoa.ObjCClass original, com.sun.jna.Pointer name, com.ochafik.lang.jnaerator.runtime.NativeSize extraBytes);
	org.rococoa.ObjCClass objc_duplicateClass(org.rococoa.ObjCClass original, java.lang.String name, com.ochafik.lang.jnaerator.runtime.NativeSize extraBytes);
	void objc_disposeClassPair(org.rococoa.ObjCClass cls);
	@java.lang.Deprecated
	boolean class_addMethod(org.rococoa.ObjCClass cls, org.rococoa.Selector name, com.sun.jna.Pointer imp, com.sun.jna.Pointer types);
	boolean class_addMethod(org.rococoa.ObjCClass cls, org.rococoa.Selector name, com.sun.jna.Pointer imp, java.lang.String types);
	@java.lang.Deprecated
	com.sun.jna.Pointer class_replaceMethod(org.rococoa.ObjCClass cls, org.rococoa.Selector name, com.sun.jna.Pointer imp, com.sun.jna.Pointer types);
	com.sun.jna.Pointer class_replaceMethod(org.rococoa.ObjCClass cls, org.rococoa.Selector name, com.sun.jna.Pointer imp, java.lang.String types);
	@java.lang.Deprecated
	boolean class_addIvar(org.rococoa.ObjCClass cls, com.sun.jna.Pointer name, com.ochafik.lang.jnaerator.runtime.NativeSize size, byte alignment, com.sun.jna.Pointer types);
	boolean class_addIvar(org.rococoa.ObjCClass cls, java.lang.String name, com.ochafik.lang.jnaerator.runtime.NativeSize size, byte alignment, java.lang.String types);
	boolean class_addProtocol(org.rococoa.ObjCClass cls, org.cocoaclojure.jna.RuntimeLibrary.objc_object protocol);
	@java.lang.Deprecated
	void class_setIvarLayout(org.rococoa.ObjCClass cls, com.sun.jna.Pointer layout);
	void class_setIvarLayout(org.rococoa.ObjCClass cls, java.lang.String layout);
	@java.lang.Deprecated
	void class_setWeakIvarLayout(org.rococoa.ObjCClass cls, com.sun.jna.Pointer layout);
	void class_setWeakIvarLayout(org.rococoa.ObjCClass cls, java.lang.String layout);
	org.rococoa.Selector method_getName(org.cocoaclojure.jna.objc_method m);
	com.sun.jna.Pointer method_getImplementation(org.cocoaclojure.jna.objc_method m);
	com.sun.jna.Pointer method_getTypeEncoding(org.cocoaclojure.jna.objc_method m);
	int method_getNumberOfArguments(org.cocoaclojure.jna.objc_method m);
	com.sun.jna.Pointer method_copyReturnType(org.cocoaclojure.jna.objc_method m);
	com.sun.jna.Pointer method_copyArgumentType(org.cocoaclojure.jna.objc_method m, int index);
	@java.lang.Deprecated
	void method_getReturnType(org.cocoaclojure.jna.objc_method m, com.sun.jna.Pointer dst, com.ochafik.lang.jnaerator.runtime.NativeSize dst_len);
	void method_getReturnType(org.cocoaclojure.jna.objc_method m, java.nio.ByteBuffer dst, com.ochafik.lang.jnaerator.runtime.NativeSize dst_len);
	@java.lang.Deprecated
	void method_getArgumentType(org.cocoaclojure.jna.objc_method m, int index, com.sun.jna.Pointer dst, com.ochafik.lang.jnaerator.runtime.NativeSize dst_len);
	void method_getArgumentType(org.cocoaclojure.jna.objc_method m, int index, java.nio.ByteBuffer dst, com.ochafik.lang.jnaerator.runtime.NativeSize dst_len);
	org.cocoaclojure.jna.objc_method_description method_getDescription(org.cocoaclojure.jna.objc_method m);
	com.sun.jna.Pointer method_setImplementation(org.cocoaclojure.jna.objc_method m, com.sun.jna.Pointer imp);
	void method_exchangeImplementations(org.cocoaclojure.jna.objc_method m1, org.cocoaclojure.jna.objc_method m2);
	com.sun.jna.Pointer ivar_getName(org.cocoaclojure.jna.objc_ivar v);
	com.sun.jna.Pointer ivar_getTypeEncoding(org.cocoaclojure.jna.objc_ivar v);
	com.ochafik.lang.jnaerator.runtime.NativeSize ivar_getOffset(org.cocoaclojure.jna.objc_ivar v);
	com.sun.jna.Pointer property_getName(org.cocoaclojure.jna.RuntimeLibrary.objc_property property);
	com.sun.jna.Pointer property_getAttributes(org.cocoaclojure.jna.RuntimeLibrary.objc_property property);
	boolean protocol_conformsToProtocol(org.cocoaclojure.jna.RuntimeLibrary.objc_object proto, org.cocoaclojure.jna.RuntimeLibrary.objc_object other);
	boolean protocol_isEqual(org.cocoaclojure.jna.RuntimeLibrary.objc_object proto, org.cocoaclojure.jna.RuntimeLibrary.objc_object other);
	com.sun.jna.Pointer protocol_getName(org.cocoaclojure.jna.RuntimeLibrary.objc_object p);
	org.cocoaclojure.jna.objc_method_description.ByValue protocol_getMethodDescription(org.cocoaclojure.jna.RuntimeLibrary.objc_object p, org.rococoa.Selector aSel, boolean isRequiredMethod, boolean isInstanceMethod);
	@java.lang.Deprecated
	org.cocoaclojure.jna.objc_method_description protocol_copyMethodDescriptionList(org.cocoaclojure.jna.RuntimeLibrary.objc_object p, boolean isRequiredMethod, boolean isInstanceMethod, com.sun.jna.ptr.IntByReference outCount);
	org.cocoaclojure.jna.objc_method_description protocol_copyMethodDescriptionList(org.cocoaclojure.jna.RuntimeLibrary.objc_object p, boolean isRequiredMethod, boolean isInstanceMethod, java.nio.IntBuffer outCount);
	@java.lang.Deprecated
	org.cocoaclojure.jna.RuntimeLibrary.objc_property protocol_getProperty(org.cocoaclojure.jna.RuntimeLibrary.objc_object proto, com.sun.jna.Pointer name, boolean isRequiredProperty, boolean isInstanceProperty);
	org.cocoaclojure.jna.RuntimeLibrary.objc_property protocol_getProperty(org.cocoaclojure.jna.RuntimeLibrary.objc_object proto, java.lang.String name, boolean isRequiredProperty, boolean isInstanceProperty);
	@java.lang.Deprecated
	org.cocoaclojure.jna.RuntimeLibrary.objc_property[] protocol_copyPropertyList(org.cocoaclojure.jna.RuntimeLibrary.objc_object proto, com.sun.jna.ptr.IntByReference outCount);
	org.cocoaclojure.jna.RuntimeLibrary.objc_property[] protocol_copyPropertyList(org.cocoaclojure.jna.RuntimeLibrary.objc_object proto, java.nio.IntBuffer outCount);
	@java.lang.Deprecated
	org.cocoaclojure.jna.RuntimeLibrary.objc_object[] protocol_copyProtocolList(org.cocoaclojure.jna.RuntimeLibrary.objc_object proto, com.sun.jna.ptr.IntByReference outCount);
	org.cocoaclojure.jna.RuntimeLibrary.objc_object[] protocol_copyProtocolList(org.cocoaclojure.jna.RuntimeLibrary.objc_object proto, java.nio.IntBuffer outCount);
	@java.lang.Deprecated
	com.sun.jna.ptr.PointerByReference objc_copyImageNames(com.sun.jna.ptr.IntByReference outCount);
	com.sun.jna.ptr.PointerByReference objc_copyImageNames(java.nio.IntBuffer outCount);
	com.sun.jna.Pointer class_getImageName(org.rococoa.ObjCClass cls);
	@java.lang.Deprecated
	com.sun.jna.ptr.PointerByReference objc_copyClassNamesForImage(com.sun.jna.Pointer image, com.sun.jna.ptr.IntByReference outCount);
	com.sun.jna.ptr.PointerByReference objc_copyClassNamesForImage(java.lang.String image, java.nio.IntBuffer outCount);
	com.sun.jna.Pointer sel_getName(org.rococoa.Selector sel);
	@java.lang.Deprecated
	org.rococoa.Selector sel_getUid(com.sun.jna.Pointer str);
	org.rococoa.Selector sel_getUid(java.lang.String str);
	@java.lang.Deprecated
	org.rococoa.Selector sel_registerName(com.sun.jna.Pointer str);
	org.rococoa.Selector sel_registerName(java.lang.String str);
	boolean sel_isEqual(org.rococoa.Selector lhs, org.rococoa.Selector rhs);
	void objc_enumerationMutation(org.rococoa.ObjCObject id1);
	void objc_setEnumerationMutationHandler(org.cocoaclojure.jna.RuntimeLibrary.handler arg1);
	void objc_setForwardHandler(com.sun.jna.Pointer fwd, com.sun.jna.Pointer fwd_stret);
	boolean sel_isMapped(org.rococoa.Selector sel);
	org.rococoa.ObjCObject object_copyFromZone(org.rococoa.ObjCObject anObject, com.ochafik.lang.jnaerator.runtime.NativeSize nBytes, com.sun.jna.Pointer z);
	org.rococoa.ObjCObject object_realloc(org.rococoa.ObjCObject anObject, com.ochafik.lang.jnaerator.runtime.NativeSize nBytes);
	org.rococoa.ObjCObject object_reallocFromZone(org.rococoa.ObjCObject anObject, com.ochafik.lang.jnaerator.runtime.NativeSize nBytes, com.sun.jna.Pointer z);
	com.sun.jna.Pointer objc_getClasses();
	void objc_addClass(org.rococoa.ObjCClass myClass);
	void objc_setClassHandler(org.cocoaclojure.jna.RuntimeLibrary.objc_setClassHandler_arg1_callback arg1);
	void objc_setMultithreaded(boolean flag);
	org.rococoa.ObjCObject class_createInstanceFromZone(org.rococoa.ObjCClass Class1, com.ochafik.lang.jnaerator.runtime.NativeSize idxIvars, com.sun.jna.Pointer z);
	void class_addMethods(org.rococoa.ObjCClass Class1, org.cocoaclojure.jna.objc_method_list objc_method_listPtr1);
	void class_removeMethods(org.rococoa.ObjCClass Class1, org.cocoaclojure.jna.objc_method_list objc_method_listPtr1);
	org.rococoa.ObjCClass class_poseAs(org.rococoa.ObjCClass imposter, org.rococoa.ObjCClass original);
	int method_getSizeOfArguments(org.cocoaclojure.jna.objc_method m);
	@java.lang.Deprecated
	int method_getArgumentInfo(org.cocoaclojure.jna.objc_method m, int arg, com.sun.jna.ptr.PointerByReference type, com.sun.jna.ptr.IntByReference offset);
	int method_getArgumentInfo(org.cocoaclojure.jna.objc_method m, int arg, java.lang.String type[], java.nio.IntBuffer offset);
	boolean class_respondsToMethod(org.rococoa.ObjCClass Class1, org.rococoa.Selector SEL1);
	com.sun.jna.Pointer class_lookupMethod(org.rococoa.ObjCClass Class1, org.rococoa.Selector SEL1);
	@java.lang.Deprecated
	org.rococoa.ObjCClass objc_getOrigClass(com.sun.jna.Pointer name);
	org.rococoa.ObjCClass objc_getOrigClass(java.lang.String name);
	org.cocoaclojure.jna.objc_method_list class_nextMethodList(org.rococoa.ObjCClass Class1, com.sun.jna.ptr.PointerByReference voidPtrPtr1);
	public static final com.ochafik.lang.jnaerator.runtime.globals.GlobalCallback<_alloc_callback> _alloc = new com.ochafik.lang.jnaerator.runtime.globals.GlobalCallback<_alloc_callback>(org.cocoaclojure.jna.RuntimeLibrary.JNA_NATIVE_LIB, _alloc_callback.class, "_alloc");
	public static final com.ochafik.lang.jnaerator.runtime.globals.GlobalCallback<_copy_callback> _copy = new com.ochafik.lang.jnaerator.runtime.globals.GlobalCallback<_copy_callback>(org.cocoaclojure.jna.RuntimeLibrary.JNA_NATIVE_LIB, _copy_callback.class, "_copy");
	public static final com.ochafik.lang.jnaerator.runtime.globals.GlobalCallback<_realloc_callback> _realloc = new com.ochafik.lang.jnaerator.runtime.globals.GlobalCallback<_realloc_callback>(org.cocoaclojure.jna.RuntimeLibrary.JNA_NATIVE_LIB, _realloc_callback.class, "_realloc");
	public static final com.ochafik.lang.jnaerator.runtime.globals.GlobalCallback<_dealloc_callback> _dealloc = new com.ochafik.lang.jnaerator.runtime.globals.GlobalCallback<_dealloc_callback>(org.cocoaclojure.jna.RuntimeLibrary.JNA_NATIVE_LIB, _dealloc_callback.class, "_dealloc");
	public static final com.ochafik.lang.jnaerator.runtime.globals.GlobalCallback<_zoneAlloc_callback> _zoneAlloc = new com.ochafik.lang.jnaerator.runtime.globals.GlobalCallback<_zoneAlloc_callback>(org.cocoaclojure.jna.RuntimeLibrary.JNA_NATIVE_LIB, _zoneAlloc_callback.class, "_zoneAlloc");
	public static final com.ochafik.lang.jnaerator.runtime.globals.GlobalCallback<_zoneRealloc_callback> _zoneRealloc = new com.ochafik.lang.jnaerator.runtime.globals.GlobalCallback<_zoneRealloc_callback>(org.cocoaclojure.jna.RuntimeLibrary.JNA_NATIVE_LIB, _zoneRealloc_callback.class, "_zoneRealloc");
	public static final com.ochafik.lang.jnaerator.runtime.globals.GlobalCallback<_zoneCopy_callback> _zoneCopy = new com.ochafik.lang.jnaerator.runtime.globals.GlobalCallback<_zoneCopy_callback>(org.cocoaclojure.jna.RuntimeLibrary.JNA_NATIVE_LIB, _zoneCopy_callback.class, "_zoneCopy");
	public static final com.ochafik.lang.jnaerator.runtime.globals.GlobalCallback<_error_callback> _error = new com.ochafik.lang.jnaerator.runtime.globals.GlobalCallback<_error_callback>(org.cocoaclojure.jna.RuntimeLibrary.JNA_NATIVE_LIB, _error_callback.class, "_error");
	/// Pointer to unknown (opaque) type
	public static class objc_property extends com.sun.jna.PointerType {
		public objc_property(com.sun.jna.Pointer pointer) {
			super(pointer);
		}
		public objc_property() {
			super();
		}
	}
	/// Pointer to unknown (opaque) type
	public static class objc_object extends com.sun.jna.PointerType {
		public objc_object(com.sun.jna.Pointer pointer) {
			super(pointer);
		}
		public objc_object() {
			super();
		}
	}
}