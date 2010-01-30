 /*
     Copyright 2010 Mark Allerton. All rights reserved.

     Redistribution and use in source and binary forms, with or without modification, are
     permitted provided that the following conditions are met:

        1. Redistributions of source code must retain the above copyright notice, this list of
           conditions and the following disclaimer.

        2. Redistributions in binary form must reproduce the above copyright notice, this list
           of conditions and the following disclaimer in the documentation and/or other materials
           provided with the distribution.

     THIS SOFTWARE IS PROVIDED BY MARK ALLERTON ``AS IS'' AND ANY EXPRESS OR IMPLIED
     WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
     FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
     CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
     CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
     SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
     ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
     NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
     ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

     The views and conclusions contained in the software and documentation are those of the
     authors and should not be interpreted as representing official policies, either expressed
     or implied, of Mark Allerton.
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
 * Returns the value of the 'peer' field of a JNA 'Pointer' object
 */
void* pointerToNative(JNIEnv* env, jobject pointer) {
	return pointer ? ADDRESS((*env)->GetLongField(env, pointer, jf_Pointer_peer)) : NULL;
}

/*
 * Class:     org_couverjure_core_IvarHelper
 * Method:    initHelper
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_couverjure_core_IvarHelper_initHelper
(JNIEnv *env, jobject obj) {
	CacheClass(jc_Pointer, "com/sun/jna/Pointer");
	CacheField(jf_Pointer_peer, jc_Pointer, "peer", "J");
}

/*
 * Class:     org_couverjure_jni_NativeHelper
 * Method:    setInstanceVar
 * Signature: (Lcom/sun/jna/Pointer;Lcom/sun/jna/Pointer;Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_org_couverjure_core_IvarHelper_setJavaIvar
(JNIEnv* env, jobject jobj, jobject object, jobject ivar, jobject lobj) {
	jobject gobj;
	id _object;
	Ivar _ivar;

	if (object && ivar && lobj) {
		_object = pointerToNative(env, object);
		_ivar = pointerToNative(env, ivar);
		if (_object && _ivar) {
			gobj = (*env)->NewGlobalRef(env, lobj);
			object_setIvar(_object, _ivar, (id) gobj);
		}
	}
}

/*
 * Class:     org_couverjure_jni_NativeHelper64
 * Method:    setJavaIvarByName
 * Signature: (JLjava/lang/String;Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_org_couverjure_core_IvarHelper_setJavaIvarByName
(JNIEnv *env, jobject jobj, jobject object, jstring jivarName, jobject lobj) {
	const char *ivarName;
	jobject gobj;
	id _object;

	if (object && jivarName) {
		_object = pointerToNative(env, object);
		if (_object) {
			ivarName = (*env)->GetStringUTFChars(env, jivarName, 0);
			gobj = (*env)->NewGlobalRef(env, lobj);
			object_setInstanceVariable(_object, ivarName, (id) gobj);
		}
	}
}

/*
 * Class:     org_couverjure_jni_NativeHelper
 * Method:    releaseInstanceVar
 * Signature: (Lcom/sun/jna/Pointer;Lcom/sun/jna/Pointer;)V
 */
JNIEXPORT void JNICALL Java_org_couverjure_core_IvarHelper_releaseJavaIvar
(JNIEnv *env, jobject jobj, jobject object, jobject ivar) {
	jobject gobj;
	id _object;
	Ivar _ivar;
	
	if (object && ivar) {
		_object = pointerToNative(env, object);
		_ivar = pointerToNative(env, ivar);
		if (_object && _ivar) {
			gobj = (jobject) object_getIvar((id) object, (Ivar) ivar);
			(*env)->DeleteGlobalRef(env, gobj);
		}
	}
}

/*
 * Class:     org_couverjure_jni_NativeHelper64
 * Method:    releaseJavaIvarByName
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_couverjure_core_IvarHelper_releaseJavaIvarByName
(JNIEnv *env, jobject jobj, jobject object, jstring jivarName) {
	const char *ivarName;
	void *value;
	jobject gobj;
	id _object;
	
	if (object && jivarName) {
		_object = pointerToNative(env, object);
		if (_object) {
			ivarName = (*env)->GetStringUTFChars(env, jivarName, 0);
			object_getInstanceVariable(_object, ivarName, (void**) &value);
			gobj = (jobject) value;
			(*env)->DeleteGlobalRef(env, gobj);
		}
	}
}

/*
 * Class:     org_couverjure_jni_NativeHelper64
 * Method:    getJavaIvarByName
 * Signature: (JLjava/lang/String;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_couverjure_core_IvarHelper_getJavaIvarByName
(JNIEnv *env, jobject jobj, jobject object, jstring jivarName) {
	const char *ivarName;
	void *value;
	jobject gobj;
	id _object;
	
	if (object && jivarName) {
		_object = pointerToNative(env, object);
		if (_object) {
			ivarName = (*env)->GetStringUTFChars(env, jivarName, 0);
			object_getInstanceVariable(_object, ivarName, (void**) &value);
			gobj = (jobject) value;
			return gobj;
		}
	}
	
	return NULL;
}




