(ns couverjure.core
  (:import
    (org.couverjure.jna FoundationLibrary ObjectiveCRuntime)
    (org.couverjure.core ID RetainReleaseID)
    (com.sun.jna Native CallbackProxy Pointer)))

; load foundation and objc-runtime libraries
(def foundation (Native/loadLibrary "Foundation" FoundationLibrary))
(def objc-runtime (Native/loadLibrary "Foundation" ObjectiveCRuntime))

(println "Loading Couverjure Core")

; wrap and unwrap ObjC IDs for release-on-finalize
(defn wrap-id [ptr] (RetainReleaseID. ptr))
(defn unwrap-id [id] (.getNativeId id))
(defn retain [id] (doto (wrap-id id) (.retain)))

; this map defines the Java/JNA types that correspond to single-char Objective-C type encodings
(def simple-objc-encodings 
  { \c Byte/TYPE,
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
    \@ Pointer,
    \# Pointer,
    \: Pointer,
    \? Pointer })

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

; make an ObjC method implementation from a function
(defn method-impl [sig fn]
  (let [param-types (into-array Class (map simple-objc-encodings (.substring sig 1)))
        return-type (simple-objc-encodings (.charAt sig 0))]
    (proxy [CallbackProxy] []
      (callback ([args] (apply fn (seq args))))
      (getParameterTypes ([] param-types))
      (getReturnType ([] return-type)))))

; add a named method implementation to a class
(defn add-method [class name sig fn]
  (println "add-method " class " name " name " sig " sig)
  (let [sel (selector name)]
    (.class_addMethod objc-runtime (unwrap-id class) sel (method-impl sig fn) sig)))

; instantiate a class
(defn alloc [class] (wrap-id (.class_createInstance objc-runtime (unwrap-id class) 0)))

; send a message to an object
(defn tell [id name-or-sel & args]
  (println "tell " id " name " name-or-sel " args " args)
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
  (let [sig-return-type (encoding-keyword-mapping return-type)
        [msg arg-types] (read-objc-msg keys-and-arg-types)]
    [msg (str sig-return-type "@:" (apply str (map encoding-keyword-mapping arg-types)))]))

; a helper macro for building objective-c method invocations
(defmacro ... [target & msg]
		(let [[selector-str args] (read-objc-msg msg)]
		     `(tell ~target ~selector-str ~@args)))

; a helper macro for building objective-c method implementations
(defmacro defm [class return-type spec args & body]
  (let [[name sig] (read-objc-method-decl return-type spec)]
    `(add-method ~class ~name ~sig (fn ~args ~@body))))

