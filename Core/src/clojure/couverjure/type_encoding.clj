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

(ns couverjure.type-encoding
  (:use
    couverjure.parser
    couverjure.types
    clojure.set)
  (:import
    (com.sun.jna Pointer)
    (org.couverjure.core ID)))

;
; Define the data structure used for the syntax tree we are going to build from the Objective-C encoding strings
;

; the list of single character encodings
(def primitive-encoding-chars
  ; everything in primitive-octypes is guaranteed to have a single-char encoding string,
  ; so we just take 'first' to get into character form
  (for [t primitive-octypes] (first (:encoding t))))

; generate map of single character encodings to octypes from the list of 'primitive' octypes
(def primitive-encodings
  (zipmap primitive-encoding-chars primitive-octypes))

; keyword mappings for argument qualifier encodings
(def qualifier-encodings {
  \r :const
  \n :in
  \N :inout
  \o :out
  \O :bycopy
  \R :byref
  \V :oneway
  })


; produce reversed map from the qualifiers map

(def encode-qualifiers
  (let [keys (keys qualifier-encodings)]
    (zipmap (map qualifier-encodings keys) keys)))

; defines the structure for the main type tree
; - for what I hope is simplicity's sake I have one struct map that contains all of the possible
; fields, and then different constructor functions for each type

(defstruct objc-type :kind :size :name :type :fields :qualifier)

;
; constructors for each node type
;

(defn primitive-type [octype]
  (struct-map objc-type
    :kind :primitive
    :type octype))
(defn array-type [size type]
  (struct-map objc-type
    :kind :array
    :size size
    :type type))
(defn field-type [name type]
  (struct-map objc-type
    :kind :field
    :name name
    :type type))
(defn structure-type [name fields]
  (struct-map objc-type
    :kind :structure
    :name name
    :fields fields))
(defn union-type [name fields]
  (struct-map objc-type
    :kind :union
    :name name
    :fields fields))
(defn pointer-element-type [type]
  (struct-map objc-type
    :kind :pointer
    :type type))
(defn bitfield-type [size]
  (struct-map objc-type
    :kind :bitfield
    :size size))
(defn arg-type [qualifier type offset]
  (struct-map objc-type
    :kind :arg
    :qualifier qualifier
    :type type
    ; we don't (yet) use offset for anything, but it allows us to easily check that we reencode correctly
    :size offset))

;
; This is the parser grammar for Objective-C method type encodings
;

; define simple char sets for alpha and digits
(def alpha (in-set (union (char-set \a \z) (char-set \A \Z))))
(def digit (in-set (char-set \0 \9)))

(def primitive (in-set (set primitive-encoding-chars) #(primitive-type (primitive-encodings %))))
(def qualifier (in-set #{\r \n \N \o \O \R \V} #(qualifier-encodings %)))

(def identifier
  (series
    (choice [alpha digit \_])
    #(apply str %)))

(def type-name
  (choice [
    identifier
    (single-char \? (fn [x] :no-name))
    ]))

(def field-name
  (pattern [\" identifier \"]
    (fn [[_ name _]] name)))

(declare type-encoding)

(def number (series digit #(Integer/parseInt (apply str %))))

(def array-count number)
(def offset number)

(declare field)

; type encoding is recursive, so we have to make this a function rather than a def
(defn type-encoding [in]
  ((choice [
    primitive
    (pattern [\[ array-count type-encoding \]]
      (fn [[_ size type _]]
        (array-type size type)))
    (pattern [\{ type-name \= (option (series field)) \}]
      (fn [[_ name _ types _]]
        (structure-type name types)))
    (pattern [\( type-name \= (option (series field)) \)]
      (fn [[_ name _ types _]]
        (union-type name types)))
    (pattern [\b number]
      (fn [[_ size]]
        (bitfield-type size)))
    (pattern [\^ type-encoding]
      (fn [[_ type]]
        (pointer-element-type type)))
    ]) in))

(def field
  (pattern [(option field-name) type-encoding]
    (fn [[name type]] (field-type name type))))

(def method-argument-encoding
  (pattern [(option qualifier) type-encoding (option offset)]
    (fn [[qualifier type offset]]
      (arg-type qualifier type offset))))

(def method-signature-encoding (series method-argument-encoding))

;
; re-encoding methods for the tree structure built above
;

(defmulti encode :kind)

(defmethod encode :primitive [p]
  (:encoding (:type p)))

(defmethod encode :array [a]
  (str \[ (:size a) (encode (:type a)) \]))

(defmethod encode :field [f]
  (str (if (option? (:name f)) (str \" (:name f) \") "") (encode (:type f))))

(defn- encode-type-name [tn]
  (let [name (:name tn)]
    (if (= :no-name name) "?" name)))

(defmethod encode :structure [s]
  (str \{ (encode-type-name s) \= (if (option? (:fields s)) (apply str (map encode (:fields s)))) \}))

(defmethod encode :union [s]
  (str \( (encode-type-name s) \= (apply str (map encode (:fields s))) \)))

(defmethod encode :pointer [p]
  (str \^ (encode (:type p))))

(defmethod encode :bitfield [b]
  (str \b (:size b)))

(defmethod encode :arg [a]
  (str
    (if (option? (:qualifier a)) (encode-qualifiers (:qualifier a)))
    (encode (:type a))
    (if (option? (:size a)) (:size a))))



