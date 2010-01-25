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

(ns couverjure.core
  (:import
    (org.couverjure.core Core ID ClassID Selector MethodImplProxy)
    (com.sun.jna Native CallbackProxy Pointer)))

; load foundation and objc-runtime libraries
(def core (Core.))

;
; Dealing with architecture specifics
;

; Use the instance of Core we created to resolve all of the JNA library interfaces
; - this allows core to load arch specific JNA interfaces, but at the expense of preventing
; us from adding type hints to the JNA calls - which means performance of these calls will
; be slow.
(def foundation (.foundation core))
(def objc-runtime (.foundation core))
(def native-helper (.nativeHelper core))

; get arch specific values from the core
;(def pointer-type (.pointerType core))
(def super-type (.superType core))
(def id-type (.idType core))

(println "Loading Couverjure Core")

;
; managing reference counts for if references
;

(defn wrap-id [ptr] (if ptr (.id core ptr) nil))
;(defn unwrap-id [id] (if id (.getNativeId id) 0))
(defn unwrap-id [id] id)
(defn retain [id] (if id (doto (wrap-id id) (.retain)) nil))


;
; handling Objective-C method type signatures/encodings
;

; this map defines the Java/JNA types that correspond to single-char Objective-C type encodings
(def simple-objc-encodings
  {\c Byte/TYPE,
   \i Integer/TYPE,
   \s Short/TYPE,
   \l Integer/TYPE,
   \q Long/TYPE,
   \C Byte/TYPE,
   \I Integer/TYPE,
   \S Short/TYPE,
   \L Integer/TYPE,
   \Q Long/TYPE,
   \f Float/TYPE,
   \d Double/TYPE,
   \B Boolean/TYPE,
   \v Void/TYPE,
   \* String,
;   \@ pointer-type,
;   \# pointer-type,
;   \: pointer-type,
;   \? pointer-type
  \@ ID
  \# ClassID
  \: Selector
  \? Pointer
  })

; map keywords to signature characters
(def encoding-keyword-mapping
  {
    ; these are (for now) just the 64-bit encodings
    :bool \B
    :char \c
    :uchar \C
    :short \s
    :ushort \S
    :unichar \S
    :int \i
    :uint \I
    :long \q
    :ulong \Q
    :longlong \q
    :ulonglong \Q
    :nsinteger \q
    :nsuinteger \Q
    :float \f
    :double \d
    :longdouble \d
    :void \v
    :char-* \*
    :id \@
    :class \#
    :sel \:
    :unknown \?
    })

(defn to-java-type
  "Converts a signature keyword to a java type"
  [kw]
  (simple-objc-encodings (encoding-keyword-mapping kw)))

(defn to-objc-sig
  "Converts a type signature from keyword form to an objc runtime signature string"
  [sig]
  (apply str (map encoding-keyword-mapping sig)))

;
; Dealing with conversions of Objective-C identifiers
;

(defn selector
  "Create method selectors from strings or keywords (or selectors - is a no-op)"
  [name-or-sel]
  (cond
    (instance? String name-or-sel) (.sel_registerName objc-runtime name-or-sel)
    (keyword? name-or-sel) (.sel_registerName objc-runtime (.replace (.substring (str name-or-sel) 1) \- \:))
    (instance? Pointer name-or-sel) name-or-sel))

(defn to-name
  "make a simple name (i.e not a selector name) from a string or keyword"
  [name-or-kw]
  (if (keyword? name-or-kw)
    (.substring (str name-or-kw) 1)
    name-or-kw))

(defn to-write-accessor-name
  "make a write accessor name from the given identifier"
  [prop-name-or-kw]
  (let [name (to-name prop-name-or-kw)
        capitalized (str (Character/toUpperCase (first name)) (subs name 1))]
    (str "set" capitalized ":")))

;
; Obtaining class references
;

(defn objc-class
  "Get a reference to the named class"
  [name]
  (wrap-id (.objc_getClass objc-runtime (to-name name))))

(defn class-of
  "Get the class of the given object"
  [id]
  (wrap-id (.object_getClass objc-runtime (unwrap-id id))))

;
; Creating and registering new classes
;

(defn new-objc-class
  "Create but do not register a new ObjC class"
  [name base-class]
  (wrap-id (.objc_allocateClassPair objc-runtime (unwrap-id base-class) (to-name name) 0)))

(defn register-objc-class
  "Register a class created using new-objc-class"
  [class]
  (.objc_registerClassPair objc-runtime (unwrap-id class)))

;
; Working with instance variables for class implementations
;

(defn get-ivar
  "Gets the value of the named ivar as a java object"
  [wrapped-id ivar-name]
  (let [id (unwrap-id wrapped-id)]
    (.getJavaIvarByName native-helper id ivar-name)))

(defn init-ivar
  "Initializes the named ivar with a java object"
  [wrapped-id ivar-name value]
  (let [id (unwrap-id wrapped-id)]
    (.setJavaIvarByName native-helper id ivar-name value)))

(defn release-ivar
  "Releases the java object associated with the named ivar."
  [wrapped-id ivar-name]
  (let [id (unwrap-id wrapped-id)]
    (.releaseJavaIvarByName native-helper id ivar-name)))

;
; Building method implmentations
;

(defn wrap-method-arg
  "Wrap a single method argument with the given signature"
  [arg sig]
  ;(println "wrap-method-arg " arg " sig " sig)
  (if (= sig :id) (retain arg) arg))

(defn wrap-method
  "This function is invoked first when a method implemntation is invoked - it handles
the necessary coercions of the arguments and return value based on the method signature"
  [wrapped-fn sig args]
  (try
    (let [result
          (apply
            wrapped-fn
            (for [i (range 0 (count args))]
              (wrap-method-arg (nth args i) (nth sig (inc i)))))]
      (condp = (first sig)
        :void nil
        :id (unwrap-id result)
        result))
    (catch Throwable e
      (println (format "Caught exception %s " e))
      (.printStackTrace e)
      nil)))

(defn method-callback-proxy
  "Builds an instance of JNA's CallbackProxy for the given method signature and implementation function"
  [sig fn]
  (let [param-types (into-array Class (map to-java-type (rest sig)))
        return-type (to-java-type (first sig))]
    ;(println "method-callback-proxy: " sig param-types (map str param-types) return-type)
    (proxy [MethodImplProxy] [return-type param-types]
      (method ([args]
        ;(println "callback: " sig param-types (map str param-types) return-type)
        (wrap-method fn sig (seq args)))))))
      ;(getParameterTypes ([] (into-array Class param-types))) ; surprisingly, JNA modifies the array you return, when using TypeMappers
      ;(getReturnType ([] return-type)))))

(defn add-method
  "Add a method to a class with the given name, signature and implementation function"
  [class name sig fn]
  ;(println "add-method " class " name " name " sig " sig)
  (let [sel (selector name)]
    (.class_addMethod objc-runtime (unwrap-id class) sel (method-callback-proxy sig fn) (to-objc-sig sig))))

; the following two functions are used to support the "method" macro and the ">>" family of macros

(defn read-objc-msg
  "reads a sequence as an objective-c message in the form <keyword> <arg>? (<keyword> <arg>)*
combines the keywords into an obj-C selector name and collects the arguments.
used by the ... macro to build actual obj-c alls and can also be used with type signatures"
  [msg]
  (let [[_ msg args]
        (reduce (fn [reduced item]
          (let [[counter names args] reduced]
            (if (even? counter)
              [(inc counter) (conj names item) args]
              [(inc counter) names (conj args item)]))) [0 [] []] msg)]
    (if (seq args)
      [(apply str (map #(str (subs (str %) 1) ":") msg)) args]
      [(subs (str (first msg)) 1)])))

(defn read-objc-method-decl
  "reads a sequence as an objective-c method declaration in the form
return-type keyword [arg-type [keyword arg-type]*]?"
  [return-type keys-and-arg-types]
  (let [[msg arg-types] (read-objc-msg keys-and-arg-types)]
    [msg (apply vector (concat [return-type :id :sel] arg-types))]))

(defmacro method
  "Builds an invocation of add-method, reading the method signature 'objC style' as a set of key/type pairs.
Also builds the implementation function using the supplied argument list and body, adding the implied
'self' and 'sel' arguments and ensuring access to the object's state.
This macro is intended to be used in the scope of an implementation block, if not, callers must synthesize
the class-def structure (see the implentation macro def)"
  [class-def spec args & body]
  (let [[name sig] (read-objc-method-decl (first spec) (rest spec))]
    `(add-method (:class ~class-def) ~name ~sig
      (fn [~(symbol "self") ~(symbol "sel") ~@args]
        ;(let [~(symbol "properties") (get-ivar ~(symbol "self") (:state-ivar-name ~class-def))]
        ~@body))))

(defn property
  "Builds a pair of read and write accessor methods for a given property name, which will get or
set property values to the object's state map. In order to be modifiable, the things in the map
must be either refs or atoms - specify which using the final argument.
This macro is intended to be used in the scope of an implementation block, if not, callers must synthesize
the class-def structure (see the implentation macro def)"
  [class-def name ref-or-atom]
  (let [properties (fn [self] (get-ivar self (:state-ivar-name class-def)))]
    (add-method (:class class-def) (to-name name) [:id :id :sel]
      (fn [self sel] (deref (name (properties self)))))
    (condp = ref-or-atom
      :atom
      (add-method (:class class-def) (to-write-accessor-name name) [:void :id :sel :id]
        (fn [self sel id]
          (reset! (name (properties self)) id)))
      :ref
      (add-method (:class class-def) (to-write-accessor-name name) [:void :id :sel :id]
        (fn [self sel id]
          (dosync (ref-set (name (properties self)) id)))))
    ))

;
; Creating class implementations
;

(defmacro implementation
  "Creates a class implementation, without binding it.
The body of the implementation should consist of a set of (method) or (property) blocks."
  [class-name base-class & body]
  `(let [new-class# (new-objc-class (to-name ~class-name) ~base-class)
         state-ivar-name# (str (gensym))
         ok# (.class_addIvar objc-runtime (unwrap-id new-class#) state-ivar-name# (.pointerSize core) (.pointerAlign core) "?")
         class-def# {:class new-class# :state-ivar-name state-ivar-name#}
         ~(symbol "properties") (fn [self#] (get-ivar self# state-ivar-name#))
         ~(symbol "init") (fn [self# initial-state#] (init-ivar self# state-ivar-name# initial-state#))]
    (doto class-def#
      ~@body)
    (method class-def# [:void :dealloc] [] (release-ivar ~(symbol "self") state-ivar-name#))
    (register-objc-class new-class#)
    new-class#))

(defmacro defimplementation
  "Creates and binds a class implementation.

  The body of the implementation should consist of a set of (method) or (property) blocks."
  [class-symbol base-class & body]
  `(def ~class-symbol (implementation ~(str class-symbol) ~base-class ~@body)))

;
; Working with instances
;

(defn alloc
  "instantiate a class"
  [class] (wrap-id (.class_createInstance objc-runtime (unwrap-id class) 0)))

;
; Method dispatch
;

(defn dynamic-send-msg
  "Sends a message to an object, introspecting at runtime to discover the method signature
and coercing arguments and return value correctly
This is currently the core mechanism for message dispatch"
  [wrapped-id-or-super name-or-sel & args]
  (let [super? (instance? super-type wrapped-id-or-super)
        sel (selector name-or-sel)
        super (if super? wrapped-id-or-super)
        id (when-not super? (unwrap-id wrapped-id-or-super))
        target-class
        (if super?
          (.clazz super)
          (.object_getClass objc-runtime id))
        target-method (.class_getInstanceMethod objc-runtime target-class sel)
        _ (if (= target-method 0) (throw (Exception. (format "Method %s not found" name-or-sel))))
        ; the replaceAll here is a complete hack, but will get us by for now
        ; see thread at http://lists.apple.com/archives/objc-language/2009/Apr/msg00141.html
        objc-sig (.replaceAll (.method_getTypeEncoding objc-runtime target-method) "\\d" "")
        ;dummy (println "objc-sig: " objc-sig)
        arg-sig (drop 3 objc-sig)
        wrapped-args
        (for [i (range 0 (count args))]
          (let [arg (nth args i)]
            (condp = (nth arg-sig i)
              \@ (unwrap-id arg)
              \# (unwrap-id arg)
              arg)))
        wrapped-args-array (to-array wrapped-args)
        raw-result
        (if super?
          (.objc_msgSendSuper objc-runtime super sel wrapped-args-array)
          (.objc_msgSend objc-runtime id sel wrapped-args-array))]
    (condp = (first objc-sig)
      \@ (retain raw-result)
      \# (retain raw-result)
      \B (.asBoolean raw-result)
      \s (.asShort raw-result)
      \S (.asShort raw-result)
      \i (.asInt raw-result)
      \I (.asInt raw-result)
      \l (.asInt raw-result)
      \L (.asInt raw-result)
      \q (.asLong raw-result)
      \Q (.asLong raw-result)
      raw-result)
    ))

(defn super
  "Obtain a reference to the 'super' object for this instance. Send messages to this
object to send to superclass."
  [wrapped-id]
  (let [receiver (unwrap-id wrapped-id)
        receiver-class (.object_getClass objc-runtime receiver)
        super-class (.class_getSuperclass objc-runtime receiver-class)]
    (.makeSuper core receiver super-class)))

(defmacro >>
  "Builds a call to dynamic-send-message, compiling the keys from the series of key/expression pairs
into the method selector."
  [target & msg]
  (let [[selector-str args] (read-objc-msg msg)]
    `(dynamic-send-msg ~target ~selector-str ~@args)))

(defmacro >>super
  "This is a shortcut for (>> (super self) ...)"
  [target & msg]
  (let [[selector-str args] (read-objc-msg msg)]
    `(dynamic-send-msg (super ~target) ~selector-str ~@args)))



