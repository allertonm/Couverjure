(ns couverjure.type-encoding
  (:use
    couverjure.parser
    clojure.set)
  (:import
    (com.sun.jna Pointer)
    (org.couverjure.core ID)))

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

;
; this map defines the Java/JNA types for the primitive types
(def primitive-java-types {
  :char Byte/TYPE,
  :int Integer/TYPE,
  :short Short/TYPE,
  :long Integer/TYPE,
  :longlong Long/TYPE,
  :uchar Byte/TYPE,
  :uint Integer/TYPE,
  :ushort Short/TYPE,
  :ulong Integer/TYPE,
  :ulonglong Long/TYPE,
  :float Float/TYPE,
  :double Double/TYPE,
  :bool Boolean/TYPE,
  :void Void/TYPE,
  :char* String,
  :id ID
  :class Pointer
  :selector Pointer
  :unknown Pointer
  })


; produce reversed maps from primitives and qualifiers maps

(def encode-primitives
  (let [keys (keys primitive-encodings)]
    (zipmap (map primitive-encodings keys) keys)))

(def encode-qualifiers
  (let [keys (keys qualifier-encodings)]
    (zipmap (map qualifier-encodings keys) keys)))

; defines a structure for argument/qualifier pairs
(defstruct method-argument :qualifier :type)

; defines the structure for the main type tree
; - for what I hope is simplicity's sake I have one struct map that contains all of the possible
; fields, and then different constructor functions for each type

(defstruct objc-type :kind :size :name :type :fields :qualifier)

;
; constructors for each node type
;

(defn primitive-type [key]
  (struct-map objc-type
    :kind :primitive
    :type key))
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
(defn arg-type [qualifier type]
  (struct-map objc-type
    :kind :arg
    :type type))

;
; This is the parser grammar for Objective-C method type encodings
;

; define simple char sets for alpha and digits
(def alpha (in-set (union (char-set \a \z) (char-set \A \Z))))
(def digit (in-set (char-set \0 \9)))

(def primitive (in-set #{\c \i \s \l \q \C \I \S \L \Q \f \d \B \v \* \@ \# \: \?} #(primitive-type (primitive-encodings %))))
(def qualifier (in-set #{\r \n \N \o \O \R \V} #(qualifier-encodings %)))

(def identifier
  (series
    (choice [alpha digit \_])
    #(apply str %)))

(def type-name
  (choice [
    identifier
    (single-char \? #(:no-name))
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
    (fn [[qualifier type _]]
      (arg-type qualifier type))))

(def method-signature-encoding (series method-argument-encoding))

;
; re-encoding methods for the tree structure built above
;

(defmulti encode :kind)

(defmethod encode :primitive [p]
  (str (encode-primitives (:type p))))

(defmethod encode :array [a]
  (str \[ (:size a) (encode (:type a)) \]))

(defmethod encode :field [f]
  (str (when-not (= :nothing (:name f)) (str \" (:name f) \") "") (encode (:type f))))

(defn- encode-type-name [tn]
  (let [name (:name tn)]
    (when-not (= :no-name name) name "?")))

(defmethod encode :structure [s]
  (str \{ (encode-type-name s) \= (apply str (map encode (:fields s))) \}))

(defmethod encode :union [s]
  (str \( (encode-type-name s) \= (apply str (map encode (:fields s))) \)))

(defmethod encode :pointer [p]
  (str \^ (encode (:type p))))

(defmethod encode :bitfield [b]
  (str \b (:size b)))

(defmethod encode :arg [a]
  (str (encode-qualifiers (:qualifier a)) (encode (:type a))))


