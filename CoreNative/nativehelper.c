/*
 *  nativehelper.c
 *  Native
 *
 *  Created by Mark Allerton on 09-12-19.
 *  Copyright 2009 Business Objects. All rights reserved.
 *
 */

#include "nativehelper.h"
#import <objc/objc-class.h>

#define ADDRESS(x) ((void*)(unsigned long)(x))

// cache class, method and field IDs that we need - these will be setup in initHelper
static jclass jc_Pointer;
static jfieldID jf_Pointer_peer;

#define CacheClass(var, name) { var = (*env)->FindClass(env, name); if (!var) return; }
#define CacheField(var, classvar, name, sig) { var = (*env)->GetFieldID(env, classvar, name, sig); if (!var) return; }

/*
 * Class:     org_couverjure_jni_NativeHelper
 * Method:    initHelper
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_couverjure_jni_NativeHelper_initHelper(JNIEnv *env, jclass jclass) {
	CacheClass(jc_Pointer, "com/sun/jna/Pointer");
	CacheField(jf_Pointer_peer, jc_Pointer, "peer", "J");
}

void* pointerToNative(JNIEnv* env, jobject pointer) {
	return pointer ? ADDRESS((*env)->GetLongField(env, pointer, jf_Pointer_peer)) : NULL;
}

/*
 * Class:     org_couverjure_jni_NativeHelper
 * Method:    setInstanceVar
 * Signature: (Lcom/sun/jna/Pointer;Lcom/sun/jna/Pointer;Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_org_couverjure_jni_NativeHelper_setJavaIvar
(JNIEnv* env, jclass cls, jobject p_id, jobject p_ivar, jobject lobj) {
	id object;
	Ivar ivar;
	ptrdiff_t offset;
	jobject gobj;
	
	if (p_id && p_ivar && lobj) {
		object = pointerToNative(env, p_id);
		ivar = pointerToNative(env, p_ivar);
		offset = ivar_getOffset(ivar);
		gobj = (*env)->NewGlobalRef(env, lobj);
		*((jobject*) (((void*)object) + offset)) = gobj;
	}
}

/*
 * Class:     org_couverjure_jni_NativeHelper
 * Method:    releaseInstanceVar
 * Signature: (Lcom/sun/jna/Pointer;Lcom/sun/jna/Pointer;)V
 */
JNIEXPORT void JNICALL Java_org_couverjure_jni_NativeHelper_releaseJavaIvar
(JNIEnv *env, jclass cls, jobject p_id, jobject p_ivar) {
	id object;
	Ivar ivar;
	ptrdiff_t offset;
	jobject gobj;
	
	if (p_id && p_ivar) {
		object = pointerToNative(env, p_id);
		ivar = pointerToNative(env, p_ivar);
		offset = ivar_getOffset(ivar);
		gobj = *((jobject*) (((void*)object) + offset));
		(*env)->DeleteGlobalRef(env, gobj);
	}
}


