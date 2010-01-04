(ns couverjure.core
  (:import
    (org.couverjure.jna FoundationLibrary ObjectiveCRuntime)
    (org.couverjure.core ID RetainReleaseID)
    (com.sun.jna Native CallbackProxy Pointer)))

; load foundation and objc-runtime libraries
(def foundation (Native/loadLibrary "Foundation" FoundationLibrary))
(def objc-runtime (Native/loadLibrary "Foundation" ObjectiveCRuntime))

; wrap and unwrap ObjC IDs for release-on-finalize
(defn wrap-id [ptr] (RetainReleaseID. ptr))
(defn unwrap-id [id] (.getNativeId id))

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
  (let [sel (selector name)]
    (.class_addMethod objc-runtime (unwrap-id class) sel (method-impl sig fn) sig)))

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

; a helper macro for building objective-c method invocations
(defmacro ... [target & msg]
		(let [[selector-str args] (read-objc-msg msg)]
		     `(tell ~target ~selector-str ~@args)))

