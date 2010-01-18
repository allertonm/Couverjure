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
JNIEXPORT void JNICALL Java_org_couverjure_jni_NativeHelper64_initHelper(JNIEnv *env, jclass jclass) {
	CacheClass(jc_Pointer, "com/sun/jna/Pointer");
	CacheField(jf_Pointer_peer, jc_Pointer, "peer", "J");
}

/*
 * Class:     org_couverjure_jni_NativeHelper
 * Method:    setInstanceVar
 * Signature: (Lcom/sun/jna/Pointer;Lcom/sun/jna/Pointer;Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_org_couverjure_jni_NativeHelper64_setJavaIvar
(JNIEnv* env, jobject jobj, jlong object, jlong ivar, jobject lobj) {
	jobject gobj;
	
	if (object && ivar && lobj) {
		gobj = (*env)->NewGlobalRef(env, lobj);
		object_setIvar((id) object, (Ivar) ivar, (id) gobj);
		//offset = ivar_getOffset((Ivar) ivar);
		//*((jobject*) (((void*)object) + offset)) = gobj;
	}
}

/*
 * Class:     org_couverjure_jni_NativeHelper64
 * Method:    setJavaIvarByName
 * Signature: (JLjava/lang/String;Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_org_couverjure_jni_NativeHelper64_setJavaIvarByName
(JNIEnv *env, jobject jobj, jlong object, jstring jivarName, jobject lobj) {
	const char *ivarName;
	jobject gobj;

	if (object && jivarName) {
		ivarName = (*env)->GetStringUTFChars(env, jivarName, 0);
		gobj = (*env)->NewGlobalRef(env, lobj);
		object_setInstanceVariable((id) object, ivarName, (id) gobj);
	}
}

/*
 * Class:     org_couverjure_jni_NativeHelper
 * Method:    releaseInstanceVar
 * Signature: (Lcom/sun/jna/Pointer;Lcom/sun/jna/Pointer;)V
 */
JNIEXPORT void JNICALL Java_org_couverjure_jni_NativeHelper64_releaseJavaIvar
(JNIEnv *env, jobject jobj, jlong object, jlong ivar) {
	jobject gobj;
	
	if (object && ivar) {
		//offset = ivar_getOffset((Ivar) ivar);
		gobj = (jobject) object_getIvar((id) object, (Ivar) ivar);
		//gobj = *((jobject*) (((void*)object) + offset));
		(*env)->DeleteGlobalRef(env, gobj);
	}
}

/*
 * Class:     org_couverjure_jni_NativeHelper64
 * Method:    releaseJavaIvarByName
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_couverjure_jni_NativeHelper64_releaseJavaIvarByName
(JNIEnv *env, jobject jobj, jlong object, jstring jivarName) {
	const char *ivarName;
	void *value;
	jobject gobj;
	
	if (object && jivarName) {
		ivarName = (*env)->GetStringUTFChars(env, jivarName, 0);
		object_getInstanceVariable((id) object, ivarName, (void**) &value);
		gobj = (jobject) value;
		(*env)->DeleteGlobalRef(env, gobj);
	}
}

/*
 * Class:     org_couverjure_jni_NativeHelper64
 * Method:    getJavaIvarByName
 * Signature: (JLjava/lang/String;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_couverjure_jni_NativeHelper64_getJavaIvarByName
(JNIEnv *env, jobject jobj, jlong object, jstring jivarName) {
	const char *ivarName;
	void *value;
	jobject gobj;
	
	if (object && jivarName) {
		ivarName = (*env)->GetStringUTFChars(env, jivarName, 0);
		object_getInstanceVariable((id) object, ivarName, (void**) &value);
		gobj = (jobject) value;
		return gobj;
	}
	
	return NULL;
}




