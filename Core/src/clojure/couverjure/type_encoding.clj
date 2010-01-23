(ns couverjure.type-encoding
  (:use couverjure.parser clojure.set))

;
; Define the data structure used for the syntax tree we are going to build from the Objective-C encoding strings
;

; keyword mappings for single char (primitive) types
(def primitive-encodings {
  \c :char
  \i :int
  \s :short
  \l :long
  \q :longlong
  \C :uchar
  \I :uint
  \S :ushort
  \L :ulong
  \Q :ulonglong
  \f :float
  \d :double
  \B :bool
  \v :void
  \* :char*
  \@ :id
  \# :class
  \: :sel
  \? :unknown
  })

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

; defines a structure for argument/qualifier pairs
(defstruct method-argument :qualifier :element-type)

; defines the structure for the main type tree
; - for what I hope is simplicity's sake I have one struct map that contains all of the possible
; fields, and then different constructor functions for each type

(defstruct element-type :kind :size :structure-name :primitive-type :element-type :element-types)

; constructors for each node type
(defn primitive-type [key]
  (struct-map element-type
    :kind :primitive
    :primitive-type key))
(defn array-type [size type]
  (struct-map element-type
    :kind :array
    :size size
    :element-type type))
(defn structure-type [name types]
  (struct-map element-type
    :kind :structure
    :structure-name name
    :element-types types))
(defn union-type [name types]
  (struct-map element-type
    :kind :union
    :structure-name name
    :element-types types))
(defn pointer-element-type [type]
  (struct-map element-type
    :kind :pointer
    :element-type type))
(defn bitfield-type [size]
  (struct-map element-type
    :kind :bitfield
    :size size))

;
; This is the parser grammar for Objective-C method type encodings
;

; define simple char sets for alpha and digits
(def alpha (in-set (union (char-set \a \z) (char-set \A \Z))))
(def digit (in-set (char-set \0 \9)))

(def primitive (in-set #{ \c \i \s \l \q \C \I \S \L \Q \f \d \B \v \* \@ \# \: \? } #(primitive-type (primitive-encodings %))))
(def qualifier (in-set #{ \r \n \N \o \O \R \V } #(qualifier-encodings %)))

(def number (series digit #(Integer/parseInt (apply str %))))
(def type-name
  (choice [
    (series
      (choice [ alpha digit \_ ]))
    "?" ] ; use string rather than char, so both sides of the choice result in sequences
    #(apply str %)))

(def array-count number)
(def offset number)

; type encoding is recursive, so we have to make this a function rather than a def
(defn type-encoding [in]
  ((choice [
    primitive
    (pattern [ \[ array-count type-encoding \] ]
      (fn [[_ size type _]]
        (array-type size type)))
    (pattern [ \{ type-name \= (option (series type-encoding)) \} ]
      (fn [[_ name _ types _]]
        (structure-type name types)))
    (pattern [ \( type-name \= (option (series type-encoding)) \) ]
      (fn [[_ name _ types _]]
        (union-type name types)))
    (pattern [ \b number ]
      (fn [[_ size]]
        (bitfield-type size)))
    (pattern [ \^ type-encoding ]
      (fn [[_ type]]
        (pointer-element-type type)))
    ]) in))

(def method-argument-encoding
  (pattern [ (option qualifier) type-encoding (option offset) ]
    (fn [[qualifier type _]]
      (struct method-argument qualifier type))))

(def method-signature-encoding (series method-argument-encoding))
