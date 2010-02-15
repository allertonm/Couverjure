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
  (:use couverjure.types couverjure.type-encoding)
  (:import
    (org.couverjure.core Core Foundation FoundationTypeMapper Foundation$Super ID MethodImplProxy)
    (com.sun.jna Native CallbackProxy TypeMappingCallbackProxy Pointer)))

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
(def native-helper (.ivarHelper core))

(println "Loading Couverjure Core")

;
; Dealing with conversions of Objective-C identifiers
;

(defn selector
  "Create method selectors from strings or keywords (or selectors - is a no-op)"
  [name-or-sel]
  (cond
    (instance? String name-or-sel) (.sel_registerName foundation name-or-sel)
    (keyword? name-or-sel) (.sel_registerName foundation (.replace (.substring (str name-or-sel) 1) \- \:))
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
  (.objc_getClass foundation (to-name name)))

(defn class-of
  "Get the class of the given object"
  [id]
  (.object_getClass foundation id))

;
; Creating and registering new classes
;

(defn new-objc-class
  "Create but do not register a new ObjC class"
  [name base-class]
  (.objc_allocateClassPair foundation base-class (to-name name) 0))

(defn register-objc-class
  "Register a class created using new-objc-class"
  [class]
  (.objc_registerClassPair foundation class))

;
; Working with instance variables for class implementations
;

(defn get-ivar
  "Gets the value of the named ivar as a java object"
  [id ivar-name]
  ;(println "get-ivar: " id)
  (.getJavaIvarByName native-helper id ivar-name))

(defn init-ivar
  "Initializes the named ivar with a java object"
  [id ivar-name value]
  ;(println "init-ivar: " id)
  (.setJavaIvarByName native-helper id ivar-name value))

(defn release-ivar
  "Releases the java object associated with the named ivar."
  [id ivar-name]
  ;(println "release-ivar: " id)
  (.releaseJavaIvarByName native-helper id ivar-name))

;
; Building method implmentations
;

(defn wrap-method
  "This function is invoked first when a method implemntation is invoked - it handles
the necessary coercions of the arguments and return value based on the method signature"
  [wrapped-fn sig args]
  (let [result
        (apply
          wrapped-fn
          (for [arg args]
            (if (instance? ID arg) (.retainAndReleaseOnFinalize arg) arg)))]
    (if (= :void (first sig)) nil result)))

(defn method-callback-proxy
  "Builds an instance of JNA's CallbackProxy for the given method signature and implementation function"
  [name sig fn]
  (let [param-types (into-array Class (map :java-type (rest sig)))
        return-type (:java-type (first sig))]
    ;(println "method-callback-proxy: " name " sig " sig " args " (map to-java-type (rest sig)))
      (proxy [MethodImplProxy] [(str name) return-type param-types]
        (typeMappedCallback ([args]
          (wrap-method fn sig (seq args)))))
      ))

(defn add-method
  "Add a method to a class with the given name, signature and implementation function"
  [class name sig fn]
  ;(println "add-method " class " name " name " sig " sig)
  (let [sel (selector name)
        objc-sig (apply str (map :encoding sig))]
    ;(println "add-method: " name " sig " objc-sig)
    (.class_addMethod foundation class sel (method-callback-proxy name sig fn) objc-sig)))

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
    [msg (apply vector (concat [return-type OCID OCSel] arg-types))]))

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
    (add-method (:class class-def) (to-name name) [OCID OCID OCSel]
      (fn [self sel] (deref (name (properties self)))))
    (condp = ref-or-atom
      :atom
      (add-method (:class class-def) (to-write-accessor-name name) [OCVoid OCID OCSel OCID]
        (fn [self sel id]
          (reset! (name (properties self)) id)))
      :ref
      (add-method (:class class-def) (to-write-accessor-name name) [OCVoid OCID OCSel OCID]
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
         ok# (.class_addIvar foundation new-class# state-ivar-name# (.pointerSize core) (.pointerAlign core) "?")
         class-def# {:class new-class# :state-ivar-name state-ivar-name#}
         ~(symbol "properties") (fn [self#] (get-ivar self# state-ivar-name#))
         ~(symbol "init") (fn [self# initial-state#] (init-ivar self# state-ivar-name# initial-state#))]
    (doto class-def#
      ~@body)
    (method class-def# [OCVoid :dealloc] [] (release-ivar ~(symbol "self") state-ivar-name#))
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
  [class] (.class_createInstance foundation class 0))

;
; Method dispatch
;

(defn coerce-return-value
  "Coerces an 'id' return value from objc_msgSend/SendSuper to the appropriate type,
  or set releaseOnFinalize and retain"
  [value type needs-retain?]
  (if (= (:kind type) :primitive)
    (let [primitive (:type type)]
        (condp (fn [set prim] (set prim)) primitive
          #{OCID}
          (if needs-retain?
            (.retainAndReleaseOnFinalize value)
            (.releaseOnFinalize value))
          #{OCClass OCSel} value
          #{OCChar OCUChar} (.asByte value)
          #{OCShort OCUShort} (.asShort value)
          #{OCInt OCUInt OCLong OCULong} (.asInt value)
          #{OCLongLong OCULongLong} (.asLong value)
          #{OCFloat} (.asFloat value)
          #{OCDouble} (.asDouble value)
          #{OCCString} (.asString value)
          #{OCVoid} nil))))

(defn needs-retain?
  "Given a selector name (for a method whose return type is 'id') determines whether the
  object should be retained or not."
  [sel-name]
  (not (.startsWith sel-name "init"))) ; need to improve this test

(defn send-msg [id sel args]
  "Low level message send to object - does not coerce return types or handle super"
  (let [args-array (to-array args)]
    (.objc_msgSend foundation id sel args-array)))

(defn send-super [super sel args]
  "Low level message send to super - does not coerce return types"
  (let [args-array (to-array args)]
    (.objc_msgSendSuper foundation super sel args-array)))

(defn dynamic-send-msg
  "Sends a message to an object, introspecting at runtime to discover the method signature
  and coercing arguments and return value correctly
  This is currently the core mechanism for message dispatch"
  [id-or-super selector-str & args]
  (let [super?
        (instance? Foundation$Super id-or-super)
        sel
        (selector selector-str)
        target-class
        (if super?
          (.supercls id-or-super)
          (.object_getClass foundation id-or-super))
        target-method
        (.class_getInstanceMethod foundation target-class sel)
        _ (if (= target-method 0) (throw (Exception. (format "Method %s not found" selector-str))))
        method-encoding
        (.method_getTypeEncoding foundation target-method)
        return-sig
        (:type (first (method-argument-encoding method-encoding))) ; parse first arg from encoding
        raw-result
        (if super?
          (send-super id-or-super sel args)
          (send-msg id-or-super sel args))]
    (coerce-return-value raw-result return-sig (needs-retain? selector-str))))

(defn super
  "Obtain a reference to the 'super' object for this instance. Send messages to this
object to send to superclass."
  [receiver]
  (let [receiver-class (.object_getClass foundation receiver)
        super-class (.class_getSuperclass foundation receiver-class)]
    (Foundation$Super. receiver super-class)))

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

;
; Macro assistance for autorelease pools
;

(def NSAutoreleasePool (objc-class "NSAutoreleasePool"))

(defmacro with-autorelease-pool
  "Wraps the body in a block that creates and releases an NSAutoreleasePool"
  [& body]
  ; NSAutoreleasePool requires special handling because we want the 'release'
  ; to occur at the end of the block, not at some later point when the GC runs
  `(let [pool# (send-msg (alloc NSAutoreleasePool) (selector "init") [])
         result# (do ~@body)]
    (send-msg pool# (selector "release") [])
    result#
    ))



