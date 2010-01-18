(ns couverjure.core
  (:import
    (org.couverjure.core Core64 ID)
    (com.sun.jna Native CallbackProxy Pointer)))

; load foundation and objc-runtime libraries
(def core (Core64.))
(def foundation (.foundation core))
(def objc-runtime (.objcRuntime core))
(def native-helper (.nativeHelper core))

(def pointer-type (.pointerType core))
(def super-type (.superType core))
(def id-type (.idType core))

(println "Loading Couverjure Core")

; wrap and unwrap ObjC IDs for release-on-finalize
(defn wrap-id [ptr] (if ptr (.id core ptr) nil))
(defn unwrap-id [id] (if id (.getNativeId id) 0))
(defn retain [id] (if id (doto (wrap-id id) (.retain)) nil))

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

; make a simple name (i.e not a selector name) from a string or keyword
(defn to-name [name-or-kw]
  (if (keyword? name-or-kw)
    (.substring (str name-or-kw) 1)
    name-or-kw))

; obtain an ObjC class reference
(defn objc-class [name]
  (wrap-id (.objc_getClass objc-runtime (to-name name))))

; obtain the class of an ObjC object
(defn class-of [id] (wrap-id (.object_getClass objc-runtime (unwrap-id id))))

; create but do not register a new ObjC class
(defn new-objc-class [name base-class]
  (wrap-id (.objc_allocateClassPair objc-runtime (unwrap-id base-class) (to-name name) 0)))

; register an ObjC class
(defn register-objc-class [class]
  (.objc_registerClassPair objc-runtime (unwrap-id class)))

(defn wrap-method-arg [arg sig]
  (if (= sig :id) (retain arg) arg))

(defn wrap-method [wrapped-fn sig args]
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
      (println (format "Caught exception %s "))
      (.printStackTrace e)
      nil)))

; make an ObjC method implementation from a function
(defn method-callback-proxy [sig fn]
  (let [param-types (into-array Class (map to-java-type (rest sig)))
        return-type (to-java-type (first sig))]
    ;(println "method-callback-proxy %s rt: %s" (str sig) return-type)
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
(defn send-msg [id name-or-sel & args]
  (.objc_msgSend objc-runtime (unwrap-id id) (selector name-or-sel) (to-array args)))

; managing instance variables
(defn get-ivar [wrapped-id ivar-name]
  (let [id (unwrap-id wrapped-id)]
    (.getJavaIvarByName native-helper id ivar-name)))

(defn init-ivar [wrapped-id ivar-name value]
  (let [id (unwrap-id wrapped-id)]
    (.setJavaIvarByName native-helper id ivar-name value)))

(defn release-ivar [wrapped-id ivar-name]
  (let [id (unwrap-id wrapped-id)]
    (.releaseJavaIvarByName native-helper id ivar-name)))


; sends a message to an object, introspecting at runtime to discover the method signature
; and coercing arguments and return value correctly
(defn dynamic-send-msg [wrapped-id-or-super name-or-sel & args]
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
      \B (not= 0 raw-result)
      raw-result)
    ))

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
(defmacro >> [target & msg]
  (let [[selector-str args] (read-objc-msg msg)]
    `(dynamic-send-msg ~target ~selector-str ~@args)))

(defn super [wrapped-id]
  (let [receiver (unwrap-id wrapped-id)
        receiver-class (.object_getClass objc-runtime receiver)
        super-class (.class_getSuperclass objc-runtime receiver-class)]
    (.makeSuper core receiver super-class)))

(defmacro >>super [target & msg]
  (let [[selector-str args] (read-objc-msg msg)]
    `(dynamic-send-msg (super ~target) ~selector-str ~@args)))

; a helper macro for building objective-c method implementations
(defmacro method [class-def spec args & body]
  (let [[name sig] (read-objc-method-decl (first spec) (rest spec))]
    `(add-method (:class ~class-def) ~name ~sig
      (fn [~(symbol "self") ~(symbol "sel") ~@args]
        ;(let [~(symbol "properties") (get-ivar ~(symbol "self") (:state-ivar-name ~class-def))]
          ~@body))))

(defmacro ivar [class-def name]
  `(.class_addIvar objc-runtime (unwrap-id (:class ~class-def)) ~(to-name name) (.pointerSize core) (.pointerAlign core) "?"))

(defn write-accessor-name [prop-name-or-kw]
  (let [name (to-name prop-name-or-kw)
        capitalized (str (Character/toUpperCase (first name)) (subs name 1))]
    (str "set" capitalized ":")))

(defn property [class-def name ref-or-atom]
  (let [properties (fn [self] (get-ivar self (:state-ivar-name class-def)))]
    (add-method (:class class-def) (to-name name) [:id :id :sel]
      (fn [self sel] (deref (name (properties self)))))
    (condp = ref-or-atom
      :atom
      (add-method (:class class-def) (write-accessor-name name) [:void :id :sel :id]
        (fn [self sel id]
          (reset! (name (properties self)) id)))
      :ref
      (add-method (:class class-def) (write-accessor-name name) [:void :id :sel :id]
        (fn [self sel id]
          (dosync (ref-set (name (properties self)) id)))))
    ))
  ;`(dosync (alter (:properties ~class-def) conj { :name ~name :opt ~ref-or-atom })))

(defmacro implementation2 [class-name base-class & body]
  `(doto (new-objc-class (to-name ~class-name) ~base-class)
    ~@body
    (register-objc-class)))

(defmacro implementation [class-name base-class & body]
  `(let [new-class# (new-objc-class (to-name ~class-name) ~base-class)
         state-ivar-name# (str (gensym))
         ok# (.class_addIvar objc-runtime (unwrap-id new-class#) state-ivar-name# (.pointerSize core) (.pointerAlign core) "?")
         class-def# { :class new-class# :state-ivar-name state-ivar-name#  }
         ~(symbol "properties") (fn [self#] (get-ivar self# state-ivar-name#))
         ~(symbol "init") (fn [self# initial-state#] (init-ivar self# state-ivar-name# initial-state#))]
    (doto class-def#
      ~@body)
    ;(generate-property-accessors class-def#)
    (method class-def# [:void :dealloc] [] (release-ivar ~(symbol "self") state-ivar-name#))
    (register-objc-class new-class#)
    new-class#))

(defmacro defimplementation [class-symbol base-class & body]
  `(def ~class-symbol (implementation ~(str class-symbol) ~base-class ~@body)))

