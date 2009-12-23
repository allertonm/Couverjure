
#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/objc/objc-class.h" 1

#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/objc/runtime.h" 1
/*
 * Copyright (c) 1999-2007 Apple Inc.  All Rights Reserved.
 * 
 * @APPLE_LICENSE_HEADER_START@
 * 
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apple Public Source License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at
 * http://www.opensource.apple.com/apsl/ and read it before using this
 * file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 * 
 * @APPLE_LICENSE_HEADER_END@
 */





#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/objc/objc.h" 1
/*
 * Copyright (c) 1999-2007 Apple Inc.  All Rights Reserved.
 * 
 * @APPLE_LICENSE_HEADER_START@
 * 
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apple Public Source License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at
 * http://www.opensource.apple.com/apsl/ and read it before using this
 * file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 * 
 * @APPLE_LICENSE_HEADER_END@
 */
/*
 *	objc.h
 *	Copyright 1988-1996, NeXT Software, Inc.
 */





#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/objc/objc-api.h" 1
/*
 * Copyright (c) 1999-2006 Apple Inc.  All Rights Reserved.
 * 
 * @APPLE_LICENSE_HEADER_START@
 * 
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apple Public Source License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at
 * http://www.opensource.apple.com/apsl/ and read it before using this
 * file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 * 
 * @APPLE_LICENSE_HEADER_END@
 */
// Copyright 1988-1996 NeXT Software, Inc.





#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/AvailabilityMacros.h" 1
/*
     File:       AvailabilityMacros.h
 
     Copyright:  (c) 2001-2005 by Apple Computer, Inc., all rights reserved.

     More Info:  See TechNote 2064

     Contains:   Autoconfiguration of AVAILABLE_ macros for Mac OS X

                 This header enables a developer to specify build time
                 constraints on what Mac OS X versions the resulting
                 application will be run.  There are two bounds a developer
                 can specify:
                 
                      MAC_OS_X_VERSION_MIN_REQUIRED
                      MAC_OS_X_VERSION_MAX_ALLOWED
                      
                The lower bound controls which calls to OS functions will 
                be weak-importing (allowed to be unresolved at launch time).
                The upper bound controls which OS functionality, if used,
                will result in a compiler error because that functionality is
                not available on on any OS is the specifed range.
                
                For example, suppose an application is compiled with:
                
                      MAC_OS_X_VERSION_MIN_REQUIRED = MAC_OS_X_VERSION_10_2
                      MAC_OS_X_VERSION_MAX_ALLOWED  = MAC_OS_X_VERSION_10_3
                     
                and an OS header contains:
                
                     extern void funcA(void) AVAILABLE_MAC_OS_X_VERSION_10_0_AND_LATER;
                     extern void funcB(void) AVAILABLE_MAC_OS_X_VERSION_10_0_AND_LATER_BUT_DEPRECATED_IN_MAC_OS_X_VERSION_10_2;
                     extern void funcC(void) AVAILABLE_MAC_OS_X_VERSION_10_0_AND_LATER_BUT_DEPRECATED_IN_MAC_OS_X_VERSION_10_3;
                     extern void funcD(void) AVAILABLE_MAC_OS_X_VERSION_10_1_AND_LATER;
                     extern void funcE(void) AVAILABLE_MAC_OS_X_VERSION_10_2_AND_LATER;
                     extern void funcF(void) AVAILABLE_MAC_OS_X_VERSION_10_3_AND_LATER;
                     extern void funcG(void) AVAILABLE_MAC_OS_X_VERSION_10_4_AND_LATER;
                     
                     typedef long TypeA DEPRECATED_IN_MAC_OS_X_VERSION_10_0_AND_LATER;
                     typedef long TypeB DEPRECATED_IN_MAC_OS_X_VERSION_10_1_AND_LATER;
                     typedef long TypeC DEPRECATED_IN_MAC_OS_X_VERSION_10_2_AND_LATER;
                     typedef long TypeD DEPRECATED_IN_MAC_OS_X_VERSION_10_3_AND_LATER;
                     typedef long TypeE DEPRECATED_IN_MAC_OS_X_VERSION_10_4_AND_LATER;

                Any application code which uses these declarations will get the following:
                
                                compile         link          run 
                                -------         ------        -------
                     funcA:     normal          normal        normal
                     funcB:     warning         normal        normal
                     funcC:     normal          normal        normal
                     funcD:     normal          normal        normal
                     funcE:     normal          normal        normal
                     funcF:     normal          weak          on 10.3 normal, on 10.2 (&funcF == NULL)
                     funcG:     error           error         n/a
                     typeA:     warning
                     typeB:     warning
                     typeC:     warning
                     typeD:     normal
                     typeE:     normal
                  
  
*/




/*
 * Set up standard Mac OS X versions
 */








/* 
 * If min OS not specified, assume 10.1
 * Note: gcc driver may set _ENVIRONMENT_MAC_OS_X_VERSION_MIN_REQUIRED_ based on MACOSX_DEPLOYMENT_TARGET environment variable
 */

    
        
    
        
            
        
            
        
    


/*
 * if max OS not specified, assume largerof(10.5, min)
 */

    
        
    
        
    


/*
 * Error on bad values
 */

    


    


/*
 * only certain compilers support __attribute__((weak_import))
 */

    

    

    


/*
 * only certain compilers support __attribute__((deprecated))
 */

    

    


/*
 * only certain compilers support __attribute__((unavailable))
 */

    

    




/*
 * AVAILABLE_MAC_OS_X_VERSION_10_0_AND_LATER
 * 
 * Used on functions introduced in Mac OS X 10.0 
 */


/*
 * AVAILABLE_MAC_OS_X_VERSION_10_0_AND_LATER_BUT_DEPRECATED
 * 
 * Used on functions introduced in Mac OS X 10.0, 
 * and deprecated in Mac OS X 10.0
 */


/*
 * DEPRECATED_IN_MAC_OS_X_VERSION_10_0_AND_LATER
 * 
 * Used on types deprecated in Mac OS X 10.0 
 */







/*
 * AVAILABLE_MAC_OS_X_VERSION_10_1_AND_LATER
 * 
 * Used on declarations introduced in Mac OS X 10.1 
 */

    

    

    


/*
 * AVAILABLE_MAC_OS_X_VERSION_10_1_AND_LATER_BUT_DEPRECATED
 * 
 * Used on declarations introduced in Mac OS X 10.1, 
 * and deprecated in Mac OS X 10.1
 */

    

    


/*
 * AVAILABLE_MAC_OS_X_VERSION_10_0_AND_LATER_BUT_DEPRECATED_IN_MAC_OS_X_VERSION_10_1
 * 
 * Used on declarations introduced in Mac OS X 10.0, 
 * but later deprecated in Mac OS X 10.1
 */

    

    


/*
 * DEPRECATED_IN_MAC_OS_X_VERSION_10_1_AND_LATER
 * 
 * Used on types deprecated in Mac OS X 10.1 
 */

    

    








/*
 * AVAILABLE_MAC_OS_X_VERSION_10_2_AND_LATER
 * 
 * Used on declarations introduced in Mac OS X 10.2 
 */

    

    

    


/*
 * AVAILABLE_MAC_OS_X_VERSION_10_2_AND_LATER_BUT_DEPRECATED
 * 
 * Used on declarations introduced in Mac OS X 10.2, 
 * and deprecated in Mac OS X 10.2
 */

    

    


/*
 * AVAILABLE_MAC_OS_X_VERSION_10_0_AND_LATER_BUT_DEPRECATED_IN_MAC_OS_X_VERSION_10_2
 * 
 * Used on declarations introduced in Mac OS X 10.0, 
 * but later deprecated in Mac OS X 10.2
 */

    

    


/*
 * AVAILABLE_MAC_OS_X_VERSION_10_1_AND_LATER_BUT_DEPRECATED_IN_MAC_OS_X_VERSION_10_2
 * 
 * Used on declarations introduced in Mac OS X 10.1, 
 * but later deprecated in Mac OS X 10.2
 */

    

    


/*
 * DEPRECATED_IN_MAC_OS_X_VERSION_10_2_AND_LATER
 * 
 * Used on types deprecated in Mac OS X 10.2 
 */

    

    






/*
 * AVAILABLE_MAC_OS_X_VERSION_10_3_AND_LATER
 * 
 * Used on declarations introduced in Mac OS X 10.3 
 */

    

    

    


/*
 * AVAILABLE_MAC_OS_X_VERSION_10_3_AND_LATER_BUT_DEPRECATED
 * 
 * Used on declarations introduced in Mac OS X 10.3, 
 * and deprecated in Mac OS X 10.3
 */

    

    


/*
 * AVAILABLE_MAC_OS_X_VERSION_10_0_AND_LATER_BUT_DEPRECATED_IN_MAC_OS_X_VERSION_10_3
 * 
 * Used on declarations introduced in Mac OS X 10.0, 
 * but later deprecated in Mac OS X 10.3
 */

    

    


/*
 * AVAILABLE_MAC_OS_X_VERSION_10_1_AND_LATER_BUT_DEPRECATED_IN_MAC_OS_X_VERSION_10_3
 * 
 * Used on declarations introduced in Mac OS X 10.1, 
 * but later deprecated in Mac OS X 10.3
 */

    

    


/*
 * AVAILABLE_MAC_OS_X_VERSION_10_2_AND_LATER_BUT_DEPRECATED_IN_MAC_OS_X_VERSION_10_3
 * 
 * Used on declarations introduced in Mac OS X 10.2, 
 * but later deprecated in Mac OS X 10.3
 */

    

    


/*
 * DEPRECATED_IN_MAC_OS_X_VERSION_10_3_AND_LATER
 * 
 * Used on types deprecated in Mac OS X 10.3 
 */

    

    







/*
 * AVAILABLE_MAC_OS_X_VERSION_10_4_AND_LATER
 * 
 * Used on declarations introduced in Mac OS X 10.4 
 */

    

    

    


/*
 * AVAILABLE_MAC_OS_X_VERSION_10_4_AND_LATER_BUT_DEPRECATED
 * 
 * Used on declarations introduced in Mac OS X 10.4, 
 * and deprecated in Mac OS X 10.4
 */

    

    


/*
 * AVAILABLE_MAC_OS_X_VERSION_10_0_AND_LATER_BUT_DEPRECATED_IN_MAC_OS_X_VERSION_10_4
 * 
 * Used on declarations introduced in Mac OS X 10.0, 
 * but later deprecated in Mac OS X 10.4
 */

    

    


/*
 * AVAILABLE_MAC_OS_X_VERSION_10_1_AND_LATER_BUT_DEPRECATED_IN_MAC_OS_X_VERSION_10_4
 * 
 * Used on declarations introduced in Mac OS X 10.1, 
 * but later deprecated in Mac OS X 10.4
 */

    

    


/*
 * AVAILABLE_MAC_OS_X_VERSION_10_2_AND_LATER_BUT_DEPRECATED_IN_MAC_OS_X_VERSION_10_4
 * 
 * Used on declarations introduced in Mac OS X 10.2, 
 * but later deprecated in Mac OS X 10.4
 */

    

    


/*
 * AVAILABLE_MAC_OS_X_VERSION_10_3_AND_LATER_BUT_DEPRECATED_IN_MAC_OS_X_VERSION_10_4
 * 
 * Used on declarations introduced in Mac OS X 10.3, 
 * but later deprecated in Mac OS X 10.4
 */

    

    


/*
 * DEPRECATED_IN_MAC_OS_X_VERSION_10_4_AND_LATER
 * 
 * Used on types deprecated in Mac OS X 10.4 
 */

    

    






/*
 * AVAILABLE_MAC_OS_X_VERSION_10_5_AND_LATER
 * 
 * Used on declarations introduced in Mac OS X 10.5 
 */

    

    

    


/*
 * AVAILABLE_MAC_OS_X_VERSION_10_5_AND_LATER_BUT_DEPRECATED
 * 
 * Used on declarations introduced in Mac OS X 10.5, 
 * and deprecated in Mac OS X 10.5
 */

    

    


/*
 * AVAILABLE_MAC_OS_X_VERSION_10_0_AND_LATER_BUT_DEPRECATED_IN_MAC_OS_X_VERSION_10_5
 * 
 * Used on declarations introduced in Mac OS X 10.0, 
 * but later deprecated in Mac OS X 10.5
 */

    

    


/*
 * AVAILABLE_MAC_OS_X_VERSION_10_1_AND_LATER_BUT_DEPRECATED_IN_MAC_OS_X_VERSION_10_5
 * 
 * Used on declarations introduced in Mac OS X 10.1, 
 * but later deprecated in Mac OS X 10.5
 */

    

    


/*
 * AVAILABLE_MAC_OS_X_VERSION_10_2_AND_LATER_BUT_DEPRECATED_IN_MAC_OS_X_VERSION_10_5
 * 
 * Used on declarations introduced in Mac OS X 10.2, 
 * but later deprecated in Mac OS X 10.5
 */

    

    


/*
 * AVAILABLE_MAC_OS_X_VERSION_10_3_AND_LATER_BUT_DEPRECATED_IN_MAC_OS_X_VERSION_10_5
 * 
 * Used on declarations introduced in Mac OS X 10.3, 
 * but later deprecated in Mac OS X 10.5
 */

    

    


/*
 * AVAILABLE_MAC_OS_X_VERSION_10_4_AND_LATER_BUT_DEPRECATED_IN_MAC_OS_X_VERSION_10_5
 * 
 * Used on declarations introduced in Mac OS X 10.4, 
 * but later deprecated in Mac OS X 10.5
 */

    

    


/*
 * DEPRECATED_IN_MAC_OS_X_VERSION_10_5_AND_LATER
 * 
 * Used on types deprecated in Mac OS X 10.5 
 */

    

    






#line 27 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/objc/objc-api.h" 2

/*
 * OBJC_API_VERSION 0 or undef: Tiger and earlier API only
 * OBJC_API_VERSION 2: Leopard and later API available
 */








/* OBJC2_UNAVAILABLE: unavailable in objc 2.0, deprecated in Leopard */






















#line 30 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/objc/objc.h" 2

#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h" 1
/*
 * Copyright (c) 2000-2004 Apple Computer, Inc. All rights reserved.
 *
 * @APPLE_OSREFERENCE_LICENSE_HEADER_START@
 * 
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apple Public Source License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. The rights granted to you under the License
 * may not be used to create, or enable the creation or redistribution of,
 * unlawful or unlicensed copies of an Apple operating system, or to
 * circumvent, violate, or enable the circumvention or violation of, any
 * terms of an Apple operating system software license agreement.
 * 
 * Please obtain a copy of the License at
 * http://www.opensource.apple.com/apsl/ and read it before using this file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 * 
 * @APPLE_OSREFERENCE_LICENSE_HEADER_END@
 */
/* Copyright (c) 1995 NeXT Computer, Inc. All Rights Reserved */
/*-
 * Copyright (c) 1982, 1986, 1991, 1993, 1994
 *	The Regents of the University of California.  All rights reserved.
 * (c) UNIX System Laboratories, Inc.
 * All or some portions of this file are derived from material licensed
 * to the University of California by American Telephone and Telegraph
 * Co. or Unix System Laboratories, Inc. and are reproduced herein with
 * the permission of UNIX System Laboratories, Inc.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *	This product includes software developed by the University of
 *	California, Berkeley and its contributors.
 * 4. Neither the name of the University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 *	@(#)types.h	8.4 (Berkeley) 1/21/94
 */





#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/appleapiopts.h" 1
/*
 * Copyright (c) 2002 Apple Computer, Inc. All rights reserved.
 *
 * @APPLE_OSREFERENCE_LICENSE_HEADER_START@
 * 
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apple Public Source License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. The rights granted to you under the License
 * may not be used to create, or enable the creation or redistribution of,
 * unlawful or unlicensed copies of an Apple operating system, or to
 * circumvent, violate, or enable the circumvention or violation of, any
 * terms of an Apple operating system software license agreement.
 * 
 * Please obtain a copy of the License at
 * http://www.opensource.apple.com/apsl/ and read it before using this file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 * 
 * @APPLE_OSREFERENCE_LICENSE_HEADER_END@
 */




































#line 72 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h" 2



#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/cdefs.h" 1
/*
 * Copyright (c) 2000-2007 Apple Inc. All rights reserved.
 *
 * @APPLE_OSREFERENCE_LICENSE_HEADER_START@
 * 
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apple Public Source License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. The rights granted to you under the License
 * may not be used to create, or enable the creation or redistribution of,
 * unlawful or unlicensed copies of an Apple operating system, or to
 * circumvent, violate, or enable the circumvention or violation of, any
 * terms of an Apple operating system software license agreement.
 * 
 * Please obtain a copy of the License at
 * http://www.opensource.apple.com/apsl/ and read it before using this file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 * 
 * @APPLE_OSREFERENCE_LICENSE_HEADER_END@
 */
/* Copyright 1995 NeXT Computer, Inc. All rights reserved. */
/*
 * Copyright (c) 1991, 1993
 *	The Regents of the University of California.  All rights reserved.
 *
 * This code is derived from software contributed to Berkeley by
 * Berkeley Software Design, Inc.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *	This product includes software developed by the University of
 *	California, Berkeley and its contributors.
 * 4. Neither the name of the University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 *	@(#)cdefs.h	8.8 (Berkeley) 1/9/95
 */












/*
 * The __CONCAT macro is used to concatenate parts of symbol names, e.g.
 * with "#define OLD(foo) __CONCAT(old,foo)", OLD(foo) produces oldfoo.
 * The __CONCAT macro is a bit tricky -- make sure you don't put spaces
 * in between its arguments.  __CONCAT can also concatenate double-quoted
 * strings produced by the __STRING macro, but this only works with ANSI C.
 */












































/*
 * GCC1 and some versions of GCC2 declare dead (non-returning) and
 * pure (no side effects) functions using "volatile" and "const";
 * unfortunately, these then cause warnings under "-ansi -pedantic".
 * GCC2 uses a new, peculiar __attribute__((attrs)) style.  All of
 * these work for GNU C++ (modulo a slight glitch in the C++ grammar
 * in the distribution version of 2.5.5).
 */

	











/* __dead and __pure are depreciated.  Use __dead2 and __pure2 instead */





/* Delete pseudo-keywords wherever they are not available or needed. */










/*
 * GCC 2.95 provides `__restrict' as an extension to C90 to support the
 * C99-specific `restrict' type qualifier.  We happen to use `__restrict' as
 * a way to define the `restrict' type qualifier without disturbing older
 * software that is unaware of C99 keywords.
 */








/*
 * Compiler-dependent macros to declare that functions take printf-like
 * or scanf-like arguments.  They are null except for versions of gcc
 * that are known to support the features properly.  Functions declared
 * with these attributes will cause compilation warnings if there is a
 * mismatch between the format string and subsequent function parameter
 * types.
 */




























/*
 * COMPILATION ENVIRONMENTS
 *
 * DEFAULT	By default newly complied code will get POSIX APIs plus
 *		Apple API extensions in scope.
 *
 *		Most users will use this compilation environment to avoid
 *		behavioural differences between 32 and 64 bit code.
 *
 * LEGACY	Defining _NONSTD_SOURCE will get pre-POSIX APIs plus Apple
 *		API extensions in scope.
 *
 *		This is generally equivalent to the Tiger release compilation
 *		environment, except that it cannot be applied to 64 bit code;
 *		its use is discouraged.
 *
 *		We expect this environment to be deprecated in the future.
 *
 * STRICT	Defining _POSIX_C_SOURCE or _XOPEN_SOURCE restricts the
 *		available APIs to exactly the set of APIs defined by the
 *		corresponding standard, based on the value defined.
 *
 *		A correct, portable definition for _POSIX_C_SOURCE is 200112L.
 *		A correct, portable definition for _XOPEN_SOURCE is 600L.
 *
 *		Apple API extensions are not visible in this environment,
 *		which can cause Apple specific code to fail to compile,
 *		or behave incorrectly if prototypes are not in scope or
 *		warnings about missing prototypes are not enabled or ignored.
 *
 * In any compilation environment, for correct symbol resolution to occur,
 * function prototypes must be in scope.  It is recommended that all Apple
 * tools users add etiher the "-Wall" or "-Wimplicit-function-declaration"
 * compiler flags to their projects to be warned when a function is being
 * used without a prototype in scope.
 */

/*
 * The __DARWIN_ALIAS macros are used to do symbol renaming; they allow
 * legacy code to use the old symbol, thus maintiang binary compatability
 * while new code can use a standards compliant version of the same function.
 *
 * __DARWIN_ALIAS is used by itself if the function signature has not
 * changed, it is used along with a #ifdef check for __DARWIN_UNIX03
 * if the signature has changed.  Because the __LP64__ enviroment
 * only supports UNIX03 sementics it causes __DARWIN_UNIX03 to be
 * defined, but causes __DARWIN_ALIAS to do no symbol mangling.
 *
 * As a special case, when XCode is used to target a specific version of the
 * OS, the manifest constant __ENVIRONMENT_MAC_OS_X_VERSION_MIN_REQUIRED__
 * will be defined by the compiler, with the digits representing major version
 * time 100 + minor version times 10 (e.g. 10.5 := 1050).  If we are targetting
 * pre-10.5, and it is the default compilation environment, revert the
 * compilation environment to pre-__DARWIN_UNIX03.
 */







































/*
 * symbol suffixes used for symbol versioning
 */





































/*
 * symbol versioning macros
 */















/*
 * POSIX.1 requires that the macros we test be defined before any standard
 * header file is included.  This permits us to convert values for feature
 * testing, as necessary, using only _POSIX_C_SOURCE.
 *
 * Here's a quick run-down of the versions:
 *  defined(_POSIX_SOURCE)		1003.1-1988
 *  _POSIX_C_SOURCE == 1L		1003.1-1990
 *  _POSIX_C_SOURCE == 2L		1003.2-1992 C Language Binding Option
 *  _POSIX_C_SOURCE == 199309L		1003.1b-1993
 *  _POSIX_C_SOURCE == 199506L		1003.1c-1995, 1003.1i-1995,
 *					and the omnibus ISO/IEC 9945-1: 1996
 *  _POSIX_C_SOURCE == 200112L		1003.1-2001
 *
 * In addition, the X/Open Portability Guide, which is now the Single UNIX
 * Specification, defines a feature-test macro which indicates the version of
 * that specification, and which subsumes _POSIX_C_SOURCE.
 */

/* Deal with IEEE Std. 1003.1-1990, in which _POSIX_C_SOURCE == 1L. */





/* Deal with IEEE Std. 1003.2-1992, in which _POSIX_C_SOURCE == 2L. */





/* Deal with various X/Open Portability Guides and Single UNIX Spec. */










/*
 * Deal with all versions of POSIX.  The ordering relative to the tests above is
 * important.
 */




/*
 * long long is not supported in c89 (__STRICT_ANSI__), but g++ -ansi and
 * c99 still want long longs.  While not perfect, we allow long longs for
 * g++.
 */




/*
 * Long double compatibility macro allow selecting variant symbols based
 * on the old (compatible) 64-bit long doubles, or the new 128-bit
 * long doubles.  This applies only to ppc; i386 already has long double
 * support, while ppc64 doesn't have any backwards history.
 */























/*
 * Deprecation macro
 */






/*****************************************
 *  Public darwin-specific feature macros
 *****************************************/

/*
 * _DARWIN_FEATURE_LONG_DOUBLE_IS_DOUBLE indicates when the long double type
 * is the same as the double type (ppc only)
 */




/*
 * _DARWIN_FEATURE_UNIX_CONFORMANCE indicates whether UNIX conformance is on,
 * and specifies the conformance level (3 is SUSv3)
 */




/*
 * _DARWIN_FEATURE_64_BIT_INODE indicates that the ino_t type is 64-bit, and
 * structures modified for 64-bit inodes (like struct stat) will be used.
 */






#line 74 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h" 2

/* Machine type dependent parameters. */

#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/machine/types.h" 1
/*
 * Copyright (c) 2000-2007 Apple Inc. All rights reserved.
 *
 * @APPLE_OSREFERENCE_LICENSE_HEADER_START@
 * 
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apple Public Source License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. The rights granted to you under the License
 * may not be used to create, or enable the creation or redistribution of,
 * unlawful or unlicensed copies of an Apple operating system, or to
 * circumvent, violate, or enable the circumvention or violation of, any
 * terms of an Apple operating system software license agreement.
 * 
 * Please obtain a copy of the License at
 * http://www.opensource.apple.com/apsl/ and read it before using this file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 * 
 * @APPLE_OSREFERENCE_LICENSE_HEADER_END@
 */
/*
 * Copyright 1995 NeXT Computer, Inc. All rights reserved.
 */







#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/types.h" 1
/*
 * Copyright (c) 2000-2006 Apple Computer, Inc. All rights reserved.
 *
 * @APPLE_OSREFERENCE_LICENSE_HEADER_START@
 * 
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apple Public Source License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. The rights granted to you under the License
 * may not be used to create, or enable the creation or redistribution of,
 * unlawful or unlicensed copies of an Apple operating system, or to
 * circumvent, violate, or enable the circumvention or violation of, any
 * terms of an Apple operating system software license agreement.
 * 
 * Please obtain a copy of the License at
 * http://www.opensource.apple.com/apsl/ and read it before using this file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 * 
 * @APPLE_OSREFERENCE_LICENSE_HEADER_END@
 */
/*
 * Copyright 1995 NeXT Computer, Inc. All rights reserved.
 */
/*-
 * Copyright (c) 1990, 1993
 *	The Regents of the University of California.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *	This product includes software developed by the University of
 *	California, Berkeley and its contributors.
 * 4. Neither the name of the University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 *	@(#)types.h	8.3 (Berkeley) 1/5/94
 */






#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/_types.h" 1
/*
 * Copyright (c) 2000-2003 Apple Computer, Inc. All rights reserved.
 *
 * @APPLE_OSREFERENCE_LICENSE_HEADER_START@
 * 
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apple Public Source License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. The rights granted to you under the License
 * may not be used to create, or enable the creation or redistribution of,
 * unlawful or unlicensed copies of an Apple operating system, or to
 * circumvent, violate, or enable the circumvention or violation of, any
 * terms of an Apple operating system software license agreement.
 * 
 * Please obtain a copy of the License at
 * http://www.opensource.apple.com/apsl/ and read it before using this file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 * 
 * @APPLE_OSREFERENCE_LICENSE_HEADER_END@
 */



/*
 * This header file contains integer types.  It's intended to also contain
 * flotaing point and other arithmetic types, as needed, later.
 */


typedef signed char		__int8_t;



typedef unsigned char		__uint8_t;
typedef	short			__int16_t;
typedef	unsigned short		__uint16_t;
typedef int			__int32_t;
typedef unsigned int		__uint32_t;
typedef long long		__int64_t;
typedef unsigned long long	__uint64_t;

typedef long			__darwin_intptr_t;
typedef unsigned int		__darwin_natural_t;

/*
 * The rune type below is declared to be an ``int'' instead of the more natural
 * ``unsigned long'' or ``long''.  Two things are happening here.  It is not
 * unsigned so that EOF (-1) can be naturally assigned to it and used.  Also,
 * it looks like 10646 will be a 31 bit standard.  This means that if your
 * ints cannot hold 32 bits, you will be in trouble.  The reason an int was
 * chosen over a long is that the is*() and to*() routines take ints (says
 * ANSI C), but they use __darwin_ct_rune_t instead of int.  By changing it
 * here, you lose a bit of ANSI conformance, but your programs will still
 * work.
 *
 * NOTE: rune_t is not covered by ANSI nor other standards, and should not
 * be instantiated outside of lib/libc/locale.  Use wchar_t.  wchar_t and
 * rune_t must be the same type.  Also wint_t must be no narrower than
 * wchar_t, and should also be able to hold all members of the largest
 * character set plus one extra value (WEOF). wint_t must be at least 16 bits.
 */

typedef int			__darwin_ct_rune_t;	/* ct_rune_t */

/*
 * mbstate_t is an opaque object to keep conversion state, during multibyte
 * stream conversions.  The content must not be referenced by user programs.
 */
typedef union {
	char		__mbstate8[128];
	long long	_mbstateL;			/* for alignment */
} __mbstate_t;

typedef __mbstate_t		__darwin_mbstate_t;	/* mbstate_t */




typedef int			__darwin_ptrdiff_t;	/* ptr1 - ptr2 */





typedef unsigned long		__darwin_size_t;	/* sizeof() */





typedef void *			__darwin_va_list;	/* va_list */





typedef __darwin_ct_rune_t	__darwin_wchar_t;	/* wchar_t */


typedef __darwin_wchar_t	__darwin_rune_t;	/* rune_t */




typedef __darwin_ct_rune_t	__darwin_wint_t;	/* wint_t */


typedef unsigned long		__darwin_clock_t;	/* clock() */
typedef __uint32_t		__darwin_socklen_t;	/* socklen_t (duh) */
typedef long			__darwin_ssize_t;	/* byte count or error */
typedef long			__darwin_time_t;	/* time() */



#line 69 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/types.h" 2

#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/cdefs.h" 1

#line 70 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/types.h" 2
/*
 * Basic integral types.  Omit the typedef if
 * not possible for a machine/compiler combination.
 */


typedef	signed char		int8_t;

typedef	unsigned char		u_int8_t;


typedef	short			int16_t;

typedef	unsigned short		u_int16_t;


typedef	int			int32_t;

typedef	unsigned int		u_int32_t;


typedef	long long		int64_t;

typedef	unsigned long long	u_int64_t;




typedef int32_t			register_t;




typedef __darwin_intptr_t	intptr_t;



typedef unsigned long int	uintptr_t;



/* These types are used for reserving the largest possible size. */
typedef u_int64_t		user_addr_t;	
typedef u_int64_t		user_size_t;	
typedef int64_t			user_ssize_t;
typedef int64_t			user_long_t;
typedef u_int64_t		user_ulong_t;
typedef int64_t			user_time_t;




/* This defines the size of syscall arguments after copying into the kernel: */
typedef u_int64_t		syscall_arg_t;








#line 37 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/machine/types.h" 2








#line 77 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h" 2

#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h" 1
/*
 * Copyright (c) 2003-2007 Apple Inc. All rights reserved.
 *
 * @APPLE_OSREFERENCE_LICENSE_HEADER_START@
 * 
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apple Public Source License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. The rights granted to you under the License
 * may not be used to create, or enable the creation or redistribution of,
 * unlawful or unlicensed copies of an Apple operating system, or to
 * circumvent, violate, or enable the circumvention or violation of, any
 * terms of an Apple operating system software license agreement.
 * 
 * Please obtain a copy of the License at
 * http://www.opensource.apple.com/apsl/ and read it before using this file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 * 
 * @APPLE_OSREFERENCE_LICENSE_HEADER_END@
 */





#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/cdefs.h" 1

#line 32 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h" 2

#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/machine/_types.h" 1
/*
 * Copyright (c) 2003-2007 Apple Inc. All rights reserved.
 *
 * @APPLE_OSREFERENCE_LICENSE_HEADER_START@
 * 
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apple Public Source License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. The rights granted to you under the License
 * may not be used to create, or enable the creation or redistribution of,
 * unlawful or unlicensed copies of an Apple operating system, or to
 * circumvent, violate, or enable the circumvention or violation of, any
 * terms of an Apple operating system software license agreement.
 * 
 * Please obtain a copy of the License at
 * http://www.opensource.apple.com/apsl/ and read it before using this file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 * 
 * @APPLE_OSREFERENCE_LICENSE_HEADER_END@
 */







#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/_types.h" 1

#line 34 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/machine/_types.h" 2








#line 33 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h" 2

/* pthread opaque structures */






















struct __darwin_pthread_handler_rec
{
	void           (*__routine)(void *);	/* Routine to call */
	void           *__arg;			/* Argument to pass */
	struct __darwin_pthread_handler_rec *__next;
};
struct _opaque_pthread_attr_t { long __sig; char __opaque[36]; };
struct _opaque_pthread_cond_t { long __sig; char __opaque[24]; };
struct _opaque_pthread_condattr_t { long __sig; char __opaque[4]; };
struct _opaque_pthread_mutex_t { long __sig; char __opaque[40]; };
struct _opaque_pthread_mutexattr_t { long __sig; char __opaque[8]; };
struct _opaque_pthread_once_t { long __sig; char __opaque[4]; };
struct _opaque_pthread_rwlock_t { long __sig; char __opaque[124]; };
struct _opaque_pthread_rwlockattr_t { long __sig; char __opaque[12]; };
struct _opaque_pthread_t { long __sig; struct __darwin_pthread_handler_rec  *__cleanup_stack; char __opaque[596]; };

/*
 * Type definitions; takes common type definitions that must be used
 * in multiple header files due to [XSI], removes them from the system
 * space, and puts them in the implementation space.
 */















typedef	__int64_t	__darwin_blkcnt_t;	/* total blocks */
typedef	__int32_t	__darwin_blksize_t;	/* preferred block size */
typedef __int32_t	__darwin_dev_t;		/* dev_t */
typedef unsigned int	__darwin_fsblkcnt_t;	/* Used by statvfs and fstatvfs */
typedef unsigned int	__darwin_fsfilcnt_t;	/* Used by statvfs and fstatvfs */
typedef __uint32_t	__darwin_gid_t;		/* [???] process and group IDs */
typedef __uint32_t	__darwin_id_t;		/* [XSI] pid_t, uid_t, or gid_t*/
typedef __uint64_t	__darwin_ino64_t;	/* [???] Used for 64 bit inodes */



typedef __uint32_t	__darwin_ino_t;		/* [???] Used for inodes */

typedef __darwin_natural_t __darwin_mach_port_name_t; /* Used by mach */
typedef __darwin_mach_port_name_t __darwin_mach_port_t; /* Used by mach */
typedef __uint16_t	__darwin_mode_t;	/* [???] Some file attributes */
typedef __int64_t	__darwin_off_t;		/* [???] Used for file sizes */
typedef __int32_t	__darwin_pid_t;		/* [???] process and group IDs */
typedef struct _opaque_pthread_attr_t
			__darwin_pthread_attr_t; /* [???] Used for pthreads */
typedef struct _opaque_pthread_cond_t
			__darwin_pthread_cond_t; /* [???] Used for pthreads */
typedef struct _opaque_pthread_condattr_t
			__darwin_pthread_condattr_t; /* [???] Used for pthreads */
typedef unsigned long	__darwin_pthread_key_t;	/* [???] Used for pthreads */
typedef struct _opaque_pthread_mutex_t
			__darwin_pthread_mutex_t; /* [???] Used for pthreads */
typedef struct _opaque_pthread_mutexattr_t
			__darwin_pthread_mutexattr_t; /* [???] Used for pthreads */
typedef struct _opaque_pthread_once_t
			__darwin_pthread_once_t; /* [???] Used for pthreads */
typedef struct _opaque_pthread_rwlock_t
			__darwin_pthread_rwlock_t; /* [???] Used for pthreads */
typedef struct _opaque_pthread_rwlockattr_t
			__darwin_pthread_rwlockattr_t; /* [???] Used for pthreads */
typedef struct _opaque_pthread_t
			*__darwin_pthread_t;	/* [???] Used for pthreads */
typedef __uint32_t	__darwin_sigset_t;	/* [???] signal set */
typedef __int32_t	__darwin_suseconds_t;	/* [???] microseconds */
typedef __uint32_t	__darwin_uid_t;		/* [???] user IDs */
typedef __uint32_t	__darwin_useconds_t;	/* [???] microseconds */
typedef	unsigned char	__darwin_uuid_t[16];



#line 78 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h" 2


#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/machine/endian.h" 1
/*
 * Copyright (c) 2000-2007 Apple Inc. All rights reserved.
 *
 * @APPLE_OSREFERENCE_LICENSE_HEADER_START@
 * 
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apple Public Source License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. The rights granted to you under the License
 * may not be used to create, or enable the creation or redistribution of,
 * unlawful or unlicensed copies of an Apple operating system, or to
 * circumvent, violate, or enable the circumvention or violation of, any
 * terms of an Apple operating system software license agreement.
 * 
 * Please obtain a copy of the License at
 * http://www.opensource.apple.com/apsl/ and read it before using this file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 * 
 * @APPLE_OSREFERENCE_LICENSE_HEADER_END@
 */
/*
 * Copyright 1995 NeXT Computer, Inc. All rights reserved.
 */







#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/endian.h" 1
/*
 * Copyright (c) 2000-2002 Apple Computer, Inc. All rights reserved.
 *
 * @APPLE_OSREFERENCE_LICENSE_HEADER_START@
 * 
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apple Public Source License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. The rights granted to you under the License
 * may not be used to create, or enable the creation or redistribution of,
 * unlawful or unlicensed copies of an Apple operating system, or to
 * circumvent, violate, or enable the circumvention or violation of, any
 * terms of an Apple operating system software license agreement.
 * 
 * Please obtain a copy of the License at
 * http://www.opensource.apple.com/apsl/ and read it before using this file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 * 
 * @APPLE_OSREFERENCE_LICENSE_HEADER_END@
 */
/*
 * Copyright 1995 NeXT Computer, Inc. All rights reserved.
 */
/*
 * Copyright (c) 1987, 1991, 1993
 *	The Regents of the University of California.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *	This product includes software developed by the University of
 *	California, Berkeley and its contributors.
 * 4. Neither the name of the University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 *	@(#)endian.h	8.1 (Berkeley) 6/11/93
 */





#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/cdefs.h" 1

#line 69 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/endian.h" 2
/*
 * Define _NOQUAD if the compiler does NOT support 64-bit integers.
 */
/* #define _NOQUAD */

/*
 * Define the order of 32-bit words in 64-bit words.
 */



/*
 * Definitions for byte order, according to byte significance from low
 * address to high.
 */















#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_endian.h" 1
/*
 * Copyright (c) 2004, 2006 Apple Computer, Inc. All rights reserved.
 *
 * @APPLE_OSREFERENCE_LICENSE_HEADER_START@
 * 
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apple Public Source License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. The rights granted to you under the License
 * may not be used to create, or enable the creation or redistribution of,
 * unlawful or unlicensed copies of an Apple operating system, or to
 * circumvent, violate, or enable the circumvention or violation of, any
 * terms of an Apple operating system software license agreement.
 * 
 * Please obtain a copy of the License at
 * http://www.opensource.apple.com/apsl/ and read it before using this file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 * 
 * @APPLE_OSREFERENCE_LICENSE_HEADER_END@
 */

/*
 * Copyright (c) 1995 NeXT Computer, Inc. All rights reserved.
 * Copyright (c) 2000-2002 Apple Computer, Inc. All rights reserved.
 *
 * @APPLE_OSREFERENCE_LICENSE_HEADER_START@
 * 
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apple Public Source License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. The rights granted to you under the License
 * may not be used to create, or enable the creation or redistribution of,
 * unlawful or unlicensed copies of an Apple operating system, or to
 * circumvent, violate, or enable the circumvention or violation of, any
 * terms of an Apple operating system software license agreement.
 * 
 * Please obtain a copy of the License at
 * http://www.opensource.apple.com/apsl/ and read it before using this file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 * 
 * @APPLE_OSREFERENCE_LICENSE_HEADER_END@
 */
/*
 * Copyright (c) 1987, 1991, 1993
 *	The Regents of the University of California.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *	This product includes software developed by the University of
 *	California, Berkeley and its contributors.
 * 4. Neither the name of the University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */





#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/cdefs.h" 1

#line 93 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_endian.h" 2

/*
 * Macros for network/external number representation conversion.
 */



























#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/libkern/_OSByteOrder.h" 1
/*
 * Copyright (c) 2006 Apple Computer, Inc. All rights reserved.
 *
 * @APPLE_OSREFERENCE_LICENSE_HEADER_START@
 * 
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apple Public Source License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. The rights granted to you under the License
 * may not be used to create, or enable the creation or redistribution of,
 * unlawful or unlicensed copies of an Apple operating system, or to
 * circumvent, violate, or enable the circumvention or violation of, any
 * terms of an Apple operating system software license agreement.
 * 
 * Please obtain a copy of the License at
 * http://www.opensource.apple.com/apsl/ and read it before using this file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 * 
 * @APPLE_OSREFERENCE_LICENSE_HEADER_END@
 */




/*
 * This header is normally included from <libkern/OSByteOrder.h>.  However,
 * <sys/_endian.h> also includes this in the case of little-endian
 * architectures, so that we can map OSByteOrder routines to the hton* and ntoh*
 * macros.  This results in the asymmetry below; we only include
 * <libkern/arch/_OSByteOrder.h> for little-endian architectures.
 */


#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h" 1

#line 40 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/libkern/_OSByteOrder.h" 2

/* Macros for swapping constant values in the preprocessing stage. */
























#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/libkern/i386/_OSByteOrder.h" 1
/*
 * Copyright (c) 2006 Apple Computer, Inc. All rights reserved.
 *
 * @APPLE_OSREFERENCE_LICENSE_HEADER_START@
 * 
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apple Public Source License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. The rights granted to you under the License
 * may not be used to create, or enable the creation or redistribution of,
 * unlawful or unlicensed copies of an Apple operating system, or to
 * circumvent, violate, or enable the circumvention or violation of, any
 * terms of an Apple operating system software license agreement.
 * 
 * Please obtain a copy of the License at
 * http://www.opensource.apple.com/apsl/ and read it before using this file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 * 
 * @APPLE_OSREFERENCE_LICENSE_HEADER_END@
 */














/* Generic byte swapping functions. */

static inline
__uint16_t
_OSSwapInt16(
    __uint16_t        _data
)
{
    return ((_data << 8) | (_data >> 8));
}

static inline
__uint32_t
_OSSwapInt32(
    __uint32_t        _data
)
{
    __asm__ ("bswap   %0" : "+r" (_data));
    return _data;
}


static inline
__uint64_t
_OSSwapInt64(
    __uint64_t        _data
)
{
    __asm__ ("bswap   %%eax\n\t"
             "bswap   %%edx\n\t" 
             "xchgl   %%eax, %%edx"
             : "+A" (_data));
    return _data;
}




    


    
    







#line 66 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/libkern/_OSByteOrder.h" 2

































    


    





    


    





    


    













#line 121 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_endian.h" 2
















#line 91 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/i386/endian.h" 2




#line 37 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/machine/endian.h" 2








#line 80 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h" 2


typedef	unsigned char		u_char;
typedef	unsigned short		u_short;
typedef	unsigned int		u_int;

typedef	unsigned long		u_long;


typedef	unsigned short		ushort;		/* Sys V compatibility */
typedef	unsigned int		uint;		/* Sys V compatibility */


typedef	u_int64_t		u_quad_t;	/* quads */
typedef	int64_t			quad_t;
typedef	quad_t *		qaddr_t;

typedef	char *			caddr_t;	/* core address */
typedef	int32_t			daddr_t;	/* disk address */


typedef	__darwin_dev_t		dev_t;		/* device number */



typedef	u_int32_t		fixpt_t;	/* fixed point number */


typedef	__darwin_blkcnt_t	blkcnt_t;




typedef	__darwin_blksize_t	blksize_t;




typedef __darwin_gid_t		gid_t;





typedef	__uint32_t		in_addr_t;	/* base type for internet address */




typedef	__uint16_t		in_port_t;



typedef	__darwin_ino_t		ino_t;		/* inode number */





typedef	__darwin_ino64_t	ino64_t;	/* 64bit inode number */






typedef	__int32_t		key_t;		/* IPC key (for Sys V IPC) */



typedef	__darwin_mode_t		mode_t;




typedef	__uint16_t		nlink_t;	/* link count */





typedef __darwin_id_t		id_t;		/* can hold pid_t, gid_t, or uid_t */



typedef __darwin_pid_t		pid_t;




typedef __darwin_off_t		off_t;



typedef	int32_t			segsz_t;	/* segment size */
typedef	int32_t			swblk_t;	/* swap offset */


typedef __darwin_uid_t		uid_t;		/* user id */









/* Major, minor numbers, dev_t's. */

/*
 * These lowercase macros tend to match member functions in some C++ code,
 * so for C++, we must use inline functions instead.
 */

static inline __int32_t  major(__uint32_t _x)
{
	return (__int32_t)(((__uint32_t)_x >> 24) & 0xff);
}

static inline __int32_t  minor(__uint32_t _x)
{
	return (__int32_t)((_x) & 0xffffff);
}

static inline dev_t  makedev(__uint32_t _major, __uint32_t _minor)
{
	return (dev_t)(((_major) << 24) | (_minor));
}












typedef	__darwin_clock_t	clock_t;




/* DO NOT REMOVE THIS COMMENT: fixincludes needs to see
 * _GCC_SIZE_T */
typedef __darwin_size_t		size_t;




typedef	__darwin_ssize_t	ssize_t;




typedef	__darwin_time_t		time_t;




typedef __darwin_useconds_t	useconds_t;




typedef __darwin_suseconds_t	suseconds_t;



/*
 * This code is present here in order to maintain historical backward
 * compatability, and is intended to be removed at some point in the
 * future; please include <sys/select.h> instead.
 */


#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_structs.h" 1
/*
 * Copyright (c) 2004-2006 Apple Computer, Inc. All rights reserved.
 *
 * @APPLE_OSREFERENCE_LICENSE_HEADER_START@
 * 
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apple Public Source License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. The rights granted to you under the License
 * may not be used to create, or enable the creation or redistribution of,
 * unlawful or unlicensed copies of an Apple operating system, or to
 * circumvent, violate, or enable the circumvention or violation of, any
 * terms of an Apple operating system software license agreement.
 * 
 * Please obtain a copy of the License at
 * http://www.opensource.apple.com/apsl/ and read it before using this file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 * 
 * @APPLE_OSREFERENCE_LICENSE_HEADER_END@
 */


#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/cdefs.h" 1

#line 31 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_structs.h" 2

#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h" 1

#line 32 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_structs.h" 2














































	
	
	










	
	










	
	










	
	














	
	
	
	
	
	

	















	
	
	
	
	
	









/*
 * Select uses bit masks of file descriptors in longs.  These macros
 * manipulate such bit fields (the filesystem macros use chars).  The
 * extra protection here is to permit application redefinition above
 * the default size.
 */









extern "C" {
typedef	struct fd_set {
	__int32_t	fds_bits[(((1024) + (((sizeof(__int32_t) * 8)) - 1)) / ((sizeof(__int32_t) * 8)))];
} fd_set;
}

/* This inline avoids argument side-effect issues with FD_ISSET() */
static inline int
__darwin_fd_isset(int _n, struct fd_set *_p)
{
	return (_p->fds_bits[_n/(sizeof(__int32_t) * 8)] & (1<<(_n % (sizeof(__int32_t) * 8))));
}












































#line 188 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h" 2




typedef __int32_t	fd_mask;

/*
 * Select uses bit masks of file descriptors in longs.  These macros
 * manipulate such bit fields (the filesystem macros use chars).  The
 * extra protection here is to permit application redefinition above
 * the default size.
 */













































typedef __darwin_pthread_attr_t		pthread_attr_t;



typedef __darwin_pthread_cond_t		pthread_cond_t;



typedef __darwin_pthread_condattr_t	pthread_condattr_t;



typedef __darwin_pthread_mutex_t	pthread_mutex_t;



typedef __darwin_pthread_mutexattr_t	pthread_mutexattr_t;



typedef __darwin_pthread_once_t		pthread_once_t;



typedef __darwin_pthread_rwlock_t	pthread_rwlock_t;



typedef __darwin_pthread_rwlockattr_t	pthread_rwlockattr_t;



typedef __darwin_pthread_t		pthread_t;






typedef __darwin_pthread_key_t		pthread_key_t;


/* statvfs and fstatvfs */


typedef __darwin_fsblkcnt_t		fsblkcnt_t;




typedef __darwin_fsfilcnt_t		fsfilcnt_t;




#line 31 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/objc/objc.h" 2


typedef struct objc_class *Class;
typedef struct objc_object {
    Class isa;
} *id;


typedef struct objc_selector 	*SEL;    
typedef id 			(*IMP)(id, SEL, ...); 
typedef signed char		BOOL; 
// BOOL is explicitly signed so @encode(BOOL) == "c" rather than "C" 
// even if -funsigned-char is used.


















extern "C" const char *sel_getName(SEL sel);
extern "C" SEL sel_registerName(const char *str);
extern "C" const char *object_getClassName(id obj);
extern "C" void *object_getIndexedIvars(id obj);




// The following declarations are provided here for source compatibility.


    
    


    typedef int arith_t;
    typedef unsigned uarith_t;



extern "C" BOOL sel_isMapped(SEL sel);
extern "C" SEL sel_getUid(const char *str);

typedef char *STR;











#line 27 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/objc/runtime.h" 2


#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/types.h" 1

#line 29 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/objc/runtime.h" 2

#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/stdarg.h" 1
/* This file is public domain.  */
/* GCC uses its own copy of this header */


#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/stdarg.h" 1

#line 6 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/stdarg.h" 2






#line 30 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/objc/runtime.h" 2

#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/stdint.h" 1
/*
 * Copyright (c) 2000, 2001, 2003, 2004, 2008 Apple Computer, Inc.
 * All rights reserved.
 */










/* from ISO/IEC 988:1999 spec */

/* 7.18.1.1 Exact-width integer types */






















typedef unsigned char         uint8_t;




typedef unsigned short       uint16_t;




typedef unsigned int         uint32_t;




typedef unsigned long long   uint64_t;


/* 7.18.1.2 Minimum-width integer types */
typedef int8_t           int_least8_t;
typedef int16_t         int_least16_t;
typedef int32_t         int_least32_t;
typedef int64_t         int_least64_t;
typedef uint8_t         uint_least8_t;
typedef uint16_t       uint_least16_t;
typedef uint32_t       uint_least32_t;
typedef uint64_t       uint_least64_t;


/* 7.18.1.3 Fastest-width integer types */
typedef int8_t            int_fast8_t;
typedef int16_t          int_fast16_t;
typedef int32_t          int_fast32_t;
typedef int64_t          int_fast64_t;
typedef uint8_t          uint_fast8_t;
typedef uint16_t        uint_fast16_t;
typedef uint32_t        uint_fast32_t;
typedef uint64_t        uint_fast64_t;


/* 7.18.1.4 Integer types capable of holding object pointers */












/* 7.18.1.5 Greatest-width integer types */





typedef long long                intmax_t;








typedef unsigned long long      uintmax_t;



/* 7.18.2 Limits of specified-width integer types:
 *   These #defines specify the minimum and maximum limits
 *   of each of the types declared above.
 */


/* 7.18.2.1 Limits of exact-width integer types */







   /*
      Note:  the literal "most negative int" cannot be written in C --
      the rules in the standard (section 6.4.4.1 in C99) will give it
      an unsigned type, so INT32_MIN (and the most negative member of
      any larger signed type) must be written via a constant expression.
   */








/* 7.18.2.2 Limits of minimum-width integer types */















/* 7.18.2.3 Limits of fastest minimum-width integer types */















/* 7.18.2.4 Limits of integer types capable of holding object pointers */















/* 7.18.2.5 Limits of greatest-width integer types */





/* 7.18.3 "Other" */








/* We have no sig_atomic_t yet, so no SIG_ATOMIC_{MIN,MAX}.
   Should end up being {-127,127} or {0,255} ... or bigger.
   My bet would be on one of {U}INT32_{MIN,MAX}. */















/* WCHAR_MIN should be 0 if wchar_t is an unsigned type and
   (-WCHAR_MAX-1) if wchar_t is a signed type.  Unfortunately,
   it turns out that -fshort-wchar changes the signedness of
   the type. */














/* 7.18.4 Macros for integer constants */















#line 31 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/objc/runtime.h" 2

#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/stddef.h" 1
/*
 * Copyright (c) 2000 Apple Computer, Inc. All rights reserved.
 *
 * @APPLE_LICENSE_HEADER_START@
 * 
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apple Public Source License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at
 * http://www.opensource.apple.com/apsl/ and read it before using this
 * file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 * 
 * @APPLE_LICENSE_HEADER_END@
 */
/*	$OpenBSD: stddef.h,v 1.2 1997/09/21 10:45:52 niklas Exp $	*/
/*	$NetBSD: stddef.h,v 1.4 1994/10/26 00:56:26 cgd Exp $	*/

/*-
 * Copyright (c) 1990 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *	This product includes software developed by the University of
 *	California, Berkeley and its contributors.
 * 4. Neither the name of the University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 *	@(#)stddef.h	5.5 (Berkeley) 4/3/91
 */










#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/_types.h" 1
/*
 * Copyright (c) 2004 Apple Computer, Inc. All rights reserved.
 *
 * @APPLE_LICENSE_HEADER_START@
 * 
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apple Public Source License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at
 * http://www.opensource.apple.com/apsl/ and read it before using this
 * file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 * 
 * @APPLE_LICENSE_HEADER_END@
 */





#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/sys/_types.h" 1

#line 27 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/_types.h" 2

typedef	int		__darwin_nl_item;
typedef	int		__darwin_wctrans_t;



typedef	unsigned long	__darwin_wctype_t;

















#line 70 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/stddef.h" 2




typedef	__darwin_ptrdiff_t	ptrdiff_t;

























typedef	__darwin_wint_t		wint_t;































#line 32 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/objc/runtime.h" 2

#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/AvailabilityMacros.h" 1

#line 33 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/objc/runtime.h" 2


/* Types */

typedef struct objc_method *Method;
typedef struct objc_ivar *Ivar;
typedef struct objc_category *Category;
typedef struct objc_property *objc_property_t;

struct objc_class {
    Class isa;


    Class super_class                                        ;
    const char *name                                         ;
    long version                                             ;
    long info                                                ;
    long instance_size                                       ;
    struct objc_ivar_list *ivars                             ;
    struct objc_method_list **methodLists                    ;
    struct objc_cache *cache                                 ;
    struct objc_protocol_list *protocols                     ;


} ;
/* Use `Class` instead of `struct objc_class *` */




typedef struct objc_object Protocol;


struct objc_method_description {
	SEL name;
	char *types;
};
struct objc_method_description_list {
        int count;
        struct objc_method_description list[1];
};

struct objc_protocol_list {
    struct objc_protocol_list *next;
    long count;
    Protocol *list[1];
};


/* Functions */

extern "C" id object_copy(id obj, size_t size);
extern "C" id object_dispose(id obj);

extern "C" Class object_getClass(id obj) 
     ;
extern "C" Class object_setClass(id obj, Class cls) 
     ;

extern "C" const char *object_getClassName(id obj);
extern "C" void *object_getIndexedIvars(id obj);

extern "C" id object_getIvar(id obj, Ivar ivar) 
     ;
extern "C" void object_setIvar(id obj, Ivar ivar, id value) 
     ;

extern "C" Ivar object_setInstanceVariable(id obj, const char *name, void *value);
extern "C" Ivar object_getInstanceVariable(id obj, const char *name, void **outValue);

extern "C" id objc_getClass(const char *name);
extern "C" id objc_getMetaClass(const char *name);
extern "C" id objc_lookUpClass(const char *name);
extern "C" id objc_getRequiredClass(const char *name);
extern "C" Class objc_getFutureClass(const char *name) 
     ;
extern "C" void objc_setFutureClass(Class cls, const char *name) 
     ;
extern "C" int objc_getClassList(Class *buffer, int bufferCount);

extern "C" Protocol *objc_getProtocol(const char *name)
     ;
extern "C" Protocol **objc_copyProtocolList(unsigned int *outCount)
     ;

extern "C" const char *class_getName(Class cls) 
     ;
extern "C" BOOL class_isMetaClass(Class cls) 
     ;
extern "C" Class class_getSuperclass(Class cls) 
     ;
extern "C" Class class_setSuperclass(Class cls, Class newSuper) 
      ;

extern "C" int class_getVersion(Class cls);
extern "C" void class_setVersion(Class cls, int version);

extern "C" size_t class_getInstanceSize(Class cls) 
     ;

extern "C" Ivar class_getInstanceVariable(Class cls, const char *name);
extern "C" Ivar class_getClassVariable(Class cls, const char *name) 
     ;
extern "C" Ivar *class_copyIvarList(Class cls, unsigned int *outCount) 
     ;

extern "C" Method class_getInstanceMethod(Class cls, SEL name);
extern "C" Method class_getClassMethod(Class cls, SEL name);
extern "C" IMP class_getMethodImplementation(Class cls, SEL name) 
     ;
extern "C" IMP class_getMethodImplementation_stret(Class cls, SEL name) 
     ;
extern "C" BOOL class_respondsToSelector(Class cls, SEL sel) 
     ;
extern "C" Method *class_copyMethodList(Class cls, unsigned int *outCount) 
     ;

extern "C" BOOL class_conformsToProtocol(Class cls, Protocol *protocol) 
     ;
extern "C" Protocol **class_copyProtocolList(Class cls, unsigned int *outCount)
     ;

extern "C" objc_property_t class_getProperty(Class cls, const char *name)
     ;
extern "C" objc_property_t *class_copyPropertyList(Class cls, unsigned int *outCount)
     ;

extern "C" const char *class_getIvarLayout(Class cls)
     ;
extern "C" const char *class_getWeakIvarLayout(Class cls)
     ;

extern "C" id class_createInstance(Class cls, size_t extraBytes);

extern "C" Class objc_allocateClassPair(Class superclass, const char *name, 
                                         size_t extraBytes) 
     ;
extern "C" void objc_registerClassPair(Class cls) 
     ;
extern "C" Class objc_duplicateClass(Class original, const char *name, 
                                      size_t extraBytes) 
     ;
extern "C" void objc_disposeClassPair(Class cls) 
     ;

extern "C" BOOL class_addMethod(Class cls, SEL name, IMP imp, 
                                 const char *types) 
     ;
extern "C" IMP class_replaceMethod(Class cls, SEL name, IMP imp, 
                                    const char *types) 
     ;
extern "C" BOOL class_addIvar(Class cls, const char *name, size_t size, 
                               uint8_t alignment, const char *types) 
     ;
extern "C" BOOL class_addProtocol(Class cls, Protocol *protocol) 
     ;
extern "C" void class_setIvarLayout(Class cls, const char *layout)
     ;
extern "C" void class_setWeakIvarLayout(Class cls, const char *layout)
     ;


extern "C" SEL method_getName(Method m) 
     ;
extern "C" IMP method_getImplementation(Method m) 
     ;
extern "C" const char *method_getTypeEncoding(Method m) 
     ;

extern "C" unsigned int method_getNumberOfArguments(Method m);
extern "C" char *method_copyReturnType(Method m) 
     ;
extern "C" char *method_copyArgumentType(Method m, unsigned int index) 
     ;
extern "C" void method_getReturnType(Method m, char *dst, size_t dst_len) 
     ;
extern "C" void method_getArgumentType(Method m, unsigned int index, 
                                        char *dst, size_t dst_len) 
     ;
extern "C" struct objc_method_description *method_getDescription(Method m) 
     ;

extern "C" IMP method_setImplementation(Method m, IMP imp) 
     ;
extern "C" void method_exchangeImplementations(Method m1, Method m2) 
     ;

extern "C" const char *ivar_getName(Ivar v) 
     ;
extern "C" const char *ivar_getTypeEncoding(Ivar v) 
     ;
extern "C" ptrdiff_t ivar_getOffset(Ivar v) 
     ;

extern "C" const char *property_getName(objc_property_t property) 
     ;
extern "C" const char *property_getAttributes(objc_property_t property) 
     ;

extern "C" BOOL protocol_conformsToProtocol(Protocol *proto, Protocol *other)
     ;
extern "C" BOOL protocol_isEqual(Protocol *proto, Protocol *other)
     ;
extern "C" const char *protocol_getName(Protocol *p)
     ;
extern "C" struct objc_method_description protocol_getMethodDescription(Protocol *p, SEL aSel, BOOL isRequiredMethod, BOOL isInstanceMethod)
     ;
extern "C" struct objc_method_description *protocol_copyMethodDescriptionList(Protocol *p, BOOL isRequiredMethod, BOOL isInstanceMethod, unsigned int *outCount)
     ;
extern "C" objc_property_t protocol_getProperty(Protocol *proto, const char *name, BOOL isRequiredProperty, BOOL isInstanceProperty)
     ;
extern "C" objc_property_t *protocol_copyPropertyList(Protocol *proto, unsigned int *outCount)
     ;
extern "C" Protocol **protocol_copyProtocolList(Protocol *proto, unsigned int *outCount)
     ;

extern "C" const char **objc_copyImageNames(unsigned int *outCount) 
     ;
extern "C" const char *class_getImageName(Class cls) 
     ;
extern "C" const char **objc_copyClassNamesForImage(const char *image, 
                                                     unsigned int *outCount) 
     ;

extern "C" const char *sel_getName(SEL sel);
extern "C" SEL sel_getUid(const char *str);
extern "C" SEL sel_registerName(const char *str);
extern "C" BOOL sel_isEqual(SEL lhs, SEL rhs) 
     ;

extern "C" void objc_enumerationMutation(id) 
     ;
extern "C" void objc_setEnumerationMutationHandler(void (*handler)(id)) 
     ;

extern "C" void objc_setForwardHandler(void *fwd, void *fwd_stret) 
     ;

































/* Obsolete types */






// class is not a metaclass

// class is a metaclass

// class's +initialize method has completed

// class is posing

// unused

// class and subclasses need cache flush during image loading

// method cache should grow when full

// unused

// methodLists is array of method lists

// the JavaBridge constructs classes with these markers


// thread-safe +initialize

// bundle unloading

// C++ ivar support

// Lazy method list arrays

// +load implementation

// objc_allocateClassPair API

// class compiled with bigger class structure



struct objc_category {
    char *category_name                                      ;
    char *class_name                                         ;
    struct objc_method_list *instance_methods                ;
    struct objc_method_list *class_methods                   ;
    struct objc_protocol_list *protocols                     ;
}                                                            ;


struct objc_ivar {
    char *ivar_name                                          ;
    char *ivar_type                                          ;
    int ivar_offset                                          ;

    

}                                                            ;

struct objc_ivar_list {
    int ivar_count                                           ;

    

    /* variable length structure */
    struct objc_ivar ivar_list[1]                            ;
}                                                            ;


struct objc_method {
    SEL method_name                                          ;
    char *method_types                                       ;
    IMP method_imp                                           ;
}                                                            ;

struct objc_method_list {
    struct objc_method_list *obsolete                        ;

    int method_count                                         ;

    

    /* variable length structure */
    struct objc_method method_list[1]                        ;
}                                                            ;


typedef struct objc_symtab *Symtab                           ;

struct objc_symtab {
    unsigned long sel_ref_cnt                                ;
    SEL *refs                                                ;
    unsigned short cls_def_cnt                               ;
    unsigned short cat_def_cnt                               ;
    void *defs[1] /* variable size */                        ;
}                                                            ;


typedef struct objc_cache *Cache                             ;









struct objc_cache {
    unsigned int mask /* total = mask + 1 */                 ;
    unsigned int occupied                                    ;
    Method buckets[1]                                        ;
} /* GrP fixme should be OBJC2_UNAVAILABLE, but isn't because of spurious warnings in [super ...] calls */;


typedef struct objc_module *Module                           ;

struct objc_module {
    unsigned long version                                    ;
    unsigned long size                                       ;
    const char *name                                         ;
    Symtab symtab                                            ;
}                                                            ;








/* Obsolete functions */

extern "C" BOOL sel_isMapped(SEL sel)                       ;

extern "C" id object_copyFromZone(id anObject, size_t nBytes, void *z) ;
extern "C" id object_realloc(id anObject, size_t nBytes)    ;
extern "C" id object_reallocFromZone(id anObject, size_t nBytes, void *z) ;


extern "C" void *objc_getClasses(void)                      ;
extern "C" void objc_addClass(Class myClass)                ;
extern "C" void objc_setClassHandler(int (*)(const char *)) ;
extern "C" void objc_setMultithreaded (BOOL flag)           ;

extern "C" id class_createInstanceFromZone(Class, size_t idxIvars, void *z) ;

extern "C" void class_addMethods(Class, struct objc_method_list *) ;
extern "C" void class_removeMethods(Class, struct objc_method_list *) ;

extern "C" Class class_poseAs(Class imposter, Class original) ;

extern "C" unsigned int method_getSizeOfArguments(Method m) ;
extern "C" unsigned method_getArgumentInfo(struct objc_method *m, int arg, const char **type, int *offset) ;

extern "C" BOOL class_respondsToMethod(Class, SEL)          ;
extern "C" IMP class_lookupMethod(Class, SEL)               ;
extern "C" Class objc_getOrigClass(const char *name)        ;

extern "C" struct objc_method_list *class_nextMethodList(Class, void **) ;
// usage for nextMethodList
//
// void *iterator = 0;
// struct objc_method_list *mlist;
// while ( mlist = class_nextMethodList( cls, &iterator ) )
//    ;

extern "C" id (*_alloc)(Class, size_t)                      ;
extern "C" id (*_copy)(id, size_t)                          ;
extern "C" id (*_realloc)(id, size_t)                       ;
extern "C" id (*_dealloc)(id)                               ;
extern "C" id (*_zoneAlloc)(Class, size_t, void *)          ;
extern "C" id (*_zoneRealloc)(id, size_t, void *)           ;
extern "C" id (*_zoneCopy)(id, size_t, void *)              ;
extern "C" void (*_error)(id, const char *, va_list)        ;



#line 3 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/objc/objc-class.h" 2

#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/objc/message.h" 1
/*
 * Copyright (c) 1999-2007 Apple Inc.  All Rights Reserved.
 * 
 * @APPLE_LICENSE_HEADER_START@
 * 
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apple Public Source License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at
 * http://www.opensource.apple.com/apsl/ and read it before using this
 * file.
 * 
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 * 
 * @APPLE_LICENSE_HEADER_END@
 */


#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/objc/objc.h" 1

#line 26 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/objc/message.h" 2

#line 1 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/objc/runtime.h" 1

#line 27 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/objc/message.h" 2


struct objc_super {
    id receiver;

    

    Class super_class;

    /* super_class is the first class to search */
};


/* Basic Messaging Primitives
 *
 * On some architectures, use objc_msgSend_stret for some struct return types.
 * On some architectures, use objc_msgSend_fpret for some float return types.
 *
 * These functions must be cast to an appropriate function pointer type 
 * before being called. 
 */
extern "C" id objc_msgSend(id self, SEL op, ...);
extern "C" id objc_msgSendSuper(struct objc_super *super, SEL op, ...);


/* Struct-returning Messaging Primitives
 *
 * Use these functions to call methods that return structs on the stack. 
 * On some architectures, some structures are returned in registers. 
 * Consult your local function call ABI documentation for details.
 * 
 * These functions must be cast to an appropriate function pointer type 
 * before being called. 
 */




/* For compatibility with old objc-runtime.h header */
extern "C" id objc_msgSend_stret(id self, SEL op, ...);
extern "C" id objc_msgSendSuper_stret(struct objc_super *super, SEL op, ...);







/* Floating-point-returning Messaging Primitives
 * 
 * Use these functions to call methods that return floating-point values 
 * on the stack. 
 * Consult your local function call ABI documentation for details.
 * 
 * ppc:    objc_msgSend_fpret not used
 * ppc64:  objc_msgSend_fpret not used
 * i386:   objc_msgSend_fpret used for `float`, `double`, `long double`.
 * x86-64: objc_msgSend_fpret used for `long double`.
 *
 * These functions must be cast to an appropriate function pointer type 
 * before being called. 
 */

extern "C" double objc_msgSend_fpret(id self, SEL op, ...);
/* Use objc_msgSendSuper() for fp-returning messages to super. */
/* See also objc_msgSendv_fpret() below. */







/* Direct Method Invocation Primitives
 * Use these functions to call the implementation of a given Method.
 * This is faster than calling method_getImplementation() and method_getName().
 *
 * The receiver must not be nil.
 *
 * These functions must be cast to an appropriate function pointer type 
 * before being called. 
 */
extern "C" id method_invoke(id receiver, Method m, ...) 
     ;
extern "C" void method_invoke_stret(id receiver, Method m, ...) 
     ;


/* Variable-argument Messaging Primitives
 *
 * Use these functions to call methods with a list of arguments, such 
 * as the one passed to forward:: .
 *
 * The contents of the argument list are architecture-specific. 
 * Consult your local function call ABI documentation for details.
 * 
 * These functions must be cast to an appropriate function pointer type 
 * before being called, except for objc_msgSendv_stret() which must not 
 * be cast to a struct-returning type.
 */

typedef void* marg_list;

extern "C" id objc_msgSendv(id self, SEL op, size_t arg_size, marg_list arg_frame) ;
extern "C" void objc_msgSendv_stret(void *stretAddr, id self, SEL op, size_t arg_size, marg_list arg_frame) ;
/* Note that objc_msgSendv_stret() does not return a structure type, 
 * and should not be cast to do so. This is unlike objc_msgSend_stret() 
 * and objc_msgSendSuper_stret().
 */

extern "C" double objc_msgSendv_fpret(id self, SEL op, unsigned arg_size, marg_list arg_frame) ;



/* The following marg_list macros are of marginal utility. They
 * are included for compatibility with the old objc-class.h header. */


















	














#line 4 "/Developer/SDKs/MacOSX10.5.sdk/usr/include/objc/objc-class.h" 2
