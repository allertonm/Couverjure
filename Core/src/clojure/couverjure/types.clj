;    Copyright 2010 Mark Allerton. All rights reserved.
;
;    Redistribution and use in source and binary forms, with or without modification, are
;    permitted provided that the following conditions are met:
;
;       1. Redistributions of source code must retain the above copyright notice, this list of
;          conditions and the following disclaimer.
;
;       2. Redistributions in binary form must reproduce the above copyright notice, this list
;          of conditions and the following disclaimer in the documentation and/or other materials
;          provided with the distribution.
;
;    THIS SOFTWARE IS PROVIDED BY MARK ALLERTON ``AS IS'' AND ANY EXPRESS OR IMPLIED
;    WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
;    FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
;    CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
;    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
;    SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
;    ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
;    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
;    ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
;
;    The views and conclusions contained in the software and documentation are those of the
;    authors and should not be interpreted as representing official policies, either expressed
;    or implied, of Mark Allerton.

(ns couverjure.types
  #^{:doc "This module defines the scheme for dealing with Objective-C types" }
  (:import
    (com.sun.jna Pointer)
    (com.sun.jna.ptr
      ByteByReference
      IntByReference
      ShortByReference
      LongByReference
      FloatByReference
      DoubleByReference
      PointerByReference)
    (org.couverjure.core ID)))


; an objc-type is a pairing of an Objective-C type encoding and a java class
; (which is the corresponding class used for JNA native mapping)
(defstruct octype :encoding :java-type)

(defmacro defoctype
  "Defines an octype with the given name, encoding and java-type"
  [name encoding java-type]
  `(def ~name (struct octype ~encoding ~java-type)))

; define the objc-types for all of the primitive types

(defoctype OCChar "c" Byte/TYPE)
(defoctype OCUChar "C" Byte/TYPE)
(defoctype OCInt "i" Integer/TYPE)
(defoctype OCUInt "I" Integer/TYPE)
(defoctype OCShort "s" Short/TYPE)
(defoctype OCUShort "S" Short/TYPE)
(defoctype OCLong "l" Integer/TYPE)
(defoctype OCULong "L" Integer/TYPE)
(defoctype OCLongLong "q" Long/TYPE)
(defoctype OCULongLong "Q" Long/TYPE)
(defoctype OCFloat "f" Float/TYPE)
(defoctype OCDouble "d" Double/TYPE)
(defoctype OCBool "B" Boolean/TYPE)
(defoctype OCVoid "v" Void/TYPE)
(defoctype OCCString "*" String)
(defoctype OCID "@" ID)
(defoctype OCClass "#" Pointer)
(defoctype OCSel ":" Pointer)
(defoctype OCUnknown "?" Pointer)

; The set of 'primitive' octypes - i.e the types that have single character encodings
; Making this information available for parsing purposes (see type-encoding.clj)
(def primitive-octypes
  [OCChar
   OCInt
   OCShort
   OCLong
   OCLongLong
   OCUChar
   OCUInt
   OCUShort
   OCULong
   OCULongLong
   OCFloat
   OCDouble
   OCBool
   OCVoid
   OCCString
   OCID
   OCClass
   OCSel
   OCUnknown])

; define pointer/byref types

(defoctype OCChar* "^c" ByteByReference)
(defoctype OCUChar* "^C" ByteByReference)
(defoctype OCInt* "^i" IntByReference)
(defoctype OCUInt* "^I" IntByReference)
(defoctype OCShort* "^s" ShortByReference)
(defoctype OCUShort* "^S" ShortByReference)
(defoctype OCLong* "^l" IntByReference)
(defoctype OCULong* "^L" IntByReference)
(defoctype OCLongLong* "^q" LongByReference)
(defoctype OCULongLong* "^Q" LongByReference)
(defoctype OCFloat* "^f" FloatByReference)
(defoctype OCDouble* "^d" FloatByReference)
(defoctype OCBool* "^B" ByteByReference)
(defoctype OCCString* "^*" PointerByReference)
; (defoctype ocid-p "^@" IDByReference) ; need to define this on the java side
(defoctype OCClass* "^#" PointerByReference)
(defoctype OCSel* "^:" PointerByReference)
(defoctype OCUnknown* "^?" PointerByReference)

; define mapping from primitive to pointer-to-primitive, can be useful for code-gen
(def to-pointer-octype {
  OCChar OCChar*
  OCInt OCInt*
  OCShort OCShort*
  OCLong OCLong*
  OCLongLong OCLongLong*
  OCUChar OCUChar*
  OCUInt OCUInt*
  OCUShort OCUShort*
  OCULong OCULongLong*
  OCFloat OCFloat*
  OCDouble OCDouble*
  OCBool OCBool*
  ;OCVoid OCVoid*
  OCCString OCCString*
  OCClass OCClass*
  OCSel OCSel*
  OCUnknown OCUnknown*
  })

; some other useful typedefs

; NSInteger/NSUInteger is architecture sensitive.
; It's not enough to just use JNA's NativeLong for this, because the type-encoding changes
; between 32 & 64-bit too ( i/I on 32-bit, q/Q on 64)
; (if arch-64-bit (do
(def NSInteger OCLongLong)
(def NSInteger* OCLongLong*)
(def NSUInteger OCULongLong)
(def NSUInteger* OCULongLong*)
; ) (do
; (def NSInteger oclong)
; (def NSUInteger oclong)))

(def OCUnichar OCUShort)
(def OCUnichar* OCUShort*)

; Define constructor fns for all of the above
; These aren't really necessary for the primitive, non-pointer types, but are here for completeness' sake
(defn occhar [a] (Byte. a))
(defn ocint [a] (Integer. a))
(defn ocshort [a] (Short. a))
(defn oclonglong [a] (Long. a))
(defn ocfloat [a] (Float. a))
(defn ocdouble [a] (Double. a))
(defn occstring [a] (String. a))

; we can just alias the following ctors, since the java types are identical
(def ocuchar occhar)
(def ocuint ocint)
(def ocushort ocshort)
(def oclong ocint)
(def oculong ocint)
(def oculonglong oclonglong)
(def ocbool occhar)

; pointer types in most cases need both zero and one arg constructors
(defn occhar*
  ([] (ByteByReference.))
  ([a] (ByteByReference. a)))

(defn ocint*
  ([] (IntByReference.))
  ([a] (IntByReference. a)))

(defn ocshort*
  ([] (ShortByReference.))
  ([a] (ShortByReference. a)))

(defn oclonglong*
  ([] (LongByReference.))
  ([a] (LongByReference. a)))

(defn ocfloat*
  ([] (FloatByReference.))
  ([a] (FloatByReference. a)))

(defn ocdouble*
  ([] (DoubleByReference.))
  ([a] (DoubleByReference. a)))

; alias the constructors for cases with identical java types
(def ocuchar* occhar*)
(def ocuint* ocint*)
(def ocushort* ocshort*)
(def oclong* ocint*)
(def oculong* ocint*)
(def oculonglong* oclonglong*)
(def ocbool* occhar*)



