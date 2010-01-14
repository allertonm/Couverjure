(ns couverjure.core
  (:import
    (org.couverjure.jna FoundationLibrary ObjectiveCRuntime)
    (org.couverjure.core Core Core64 ID)
    (com.sun.jna Native CallbackProxy Pointer)))

; load foundation and objc-runtime libraries
(def core (Core64.))
(def foundation (.foundation core))
(def objc-runtime (.objcRuntime core))

(def pointer-type (.pointerType core))

(println "Loading Couverjure Core")

; wrap and unwrap ObjC IDs for release-on-finalize
(defn wrap-id [ptr] (.id core ptr))
(defn unwrap-id [id] (.getNativeId id))
(defn retain [id] (doto (wrap-id id) (.retain)))

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
   \@ pointer-type,
   \# pointer-type,
   \: pointer-type,
   \? pointer-type})

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

(defn to-java-type [kw]
  (simple-objc-encodings (encoding-keyword-mapping kw)))

; converts a type signature from keyword form to an objc runtime signature string
(defn to-objc-sig [sig]
  (apply str (map encoding-keyword-mapping sig)))


; create selectors from strings or keywords (or selectors - is a no-op)
(defn selector [name-or-sel]
  (cond
    (instance? String name-or-sel) (.sel_registerName objc-runtime name-or-sel)
    (keyword? name-or-sel) (.sel_registerName objc-runtime (.replace (.substring (str name-or-sel) 1) \- \:))
    (instance? Pointer name-or-sel) name-or-sel))

; make an ObjC classname from a string or keyword
(defn objc-class-name [name-or-kw]
  (if (keyword? name-or-kw)
    (.substring (str name-or-kw) 1)
    name-or-kw))

; obtain an ObjC class reference
(defn objc-class [name]
  (wrap-id (.objc_getClass objc-runtime (objc-class-name name))))

; obtain the class of an ObjC object
(defn class-of [id] (wrap-id (.object_getClass objc-runtime (unwrap-id id))))

; create but do not register a new ObjC class
(defn new-objc-class [name base-class]
  (wrap-id (.objc_allocateClassPair objc-runtime (unwrap-id base-class) (objc-class-name name) 0)))

; register an ObjC class
(defn register-objc-class [class]
  (.objc_registerClassPair objc-runtime (unwrap-id class)))

(defn wrap-method-arg [arg sig]
  (if (= sig :id) (retain arg) arg))

(defn wrap-method [wrapped-fn sig args]
  (apply
    wrapped-fn
    (for [i (range 0 (count args))]
      (wrap-method-arg (nth args i) (nth sig (inc i))))))

; make an ObjC method implementation from a function
(defn method-callback-proxy [sig fn]
  (let [param-types (into-array Class (map to-java-type (rest sig)))
        return-type (to-java-type (first sig))]
    (proxy [CallbackProxy] []
      (callback ([args]
        (wrap-method fn sig (seq args))))
      (getParameterTypes ([] param-types))
      (getReturnType ([] return-type)))))

; add a named method implementation to a class
(defn add-method [class name sig fn]
  ;(println "add-method " class " name " name " sig " sig)
  (let [sel (selector name)]
    (.class_addMethod objc-runtime (unwrap-id class) sel (method-callback-proxy sig fn) (to-objc-sig sig))))

; instantiate a class
(defn alloc [class] (wrap-id (.class_createInstance objc-runtime (unwrap-id class) 0)))

; send a message to an object
(defn tell [id name-or-sel & args]
  (.objc_msgSend objc-runtime (unwrap-id id) (selector name-or-sel) (to-array args)))

; reads a sequence as an objective-c message in the form <keyword> <arg>? (<keyword> <arg>)*
; - combines the keywords into an obj-C selector name and collects the arguments.
; - used by the ... macro to build actual obj-c alls and can also be used with type signatures
(defn read-objc-msg [msg]
  (let [[_ msg args]
        (reduce (fn [reduced item]
          (let [[counter names args] reduced]
            (if (even? counter)
              [(inc counter) (conj names item) args]
              [(inc counter) names (conj args item)]))) [0 [] []] msg)]
    (if (seq args)
      [(apply str (map #(str (subs (str %) 1) ":") msg)) args]
      [(subs (str (first msg)) 1)])))

; reads a sequence as an objective-c method declaration in the form
; return-type keyword [arg-type [keyword arg-type]*]?
(defn read-objc-method-decl [return-type keys-and-arg-types]
  (let [[msg arg-types] (read-objc-msg keys-and-arg-types)]
    [msg (apply vector (concat [return-type :id :sel] arg-types))]))

; a helper macro for building objective-c method invocations
(defmacro ... [target & msg]
  (let [[selector-str args] (read-objc-msg msg)]
    `(tell ~target ~selector-str ~@args)))

; a helper macro for building objective-c method implementations
(defmacro method [class spec args & body]
  (let [[name sig] (read-objc-method-decl (first spec) (rest spec))]
    `(add-method ~class ~name ~sig (fn [~(symbol "self") ~(symbol "sel") ~@args] ~@body))))

(defmacro implementation [class-name base-class & body]
  `(doto (new-objc-class (objc-class-name ~class-name) ~base-class)
    ~@body
    (register-objc-class)))

(defmacro defimplementation [class-symbol base-class & body]
  `(def ~class-symbol (implementation ~(str class-symbol) ~base-class ~@body)))

