(ns couverjure.tools.java-model
  (:use clojure.test))

(defmacro deftagged [name & keys]
  (let [keys-as-symbols (for [k keys] (symbol (subs (str k) 1)))
        struct-name (gensym)]
  `(do
    (defstruct ~struct-name :tag ~@keys)
    (defn ~name [~@keys-as-symbols] (struct ~struct-name ~(keyword (str name)) ~@keys-as-symbols)))))

(deftagged type-specifier :name :array?)
(deftagged var-declaration :type-specifier :name)
(deftagged field-declaration :modifiers :var-declaration)
(deftagged method-declaration :modifiers :type-specifier :name :parameters :body)
(deftagged class-declaration :modifiers :name :implements :extends :body)
(deftagged interface-declaration :modifiers :name :extends :body)
(deftagged var-reference :object :name)
(deftagged modifier :name)

(deftagged statement :body)
(deftagged method-invocation :object :method :parameters)
(deftagged assignment :left :right)
(deftagged break)
(deftagged line-comment :text)

(defmulti java-source (fn [m level] (or (:tag m) :seq)))

(def jtab "    ")

(defn indent [x] (repeat x jtab))

(defn java-source-modifiers [mods]
  (if (and mods (> (count mods) 0))
    [(interpose " " (java-source mods 0)) " "]
    nil))

(defmethod java-source :seq [s level]
  (doall (for [m s] (java-source m level))))

(defmethod java-source :type-specifier [ts level]
  [ (:name ts) (if (:array? ts) "[]") ])

(defmethod java-source :var-declaration [vd level]
  [ (java-source (:type-specifier vd) level) " " (:name vd) ])

(defmethod java-source :field-declaration [fd level]
  [ (indent level)
    (java-source-modifiers (:modifiers fd))
    (java-source (:var-declaration fd) level)
    ";\n"])

(defmethod java-source :method-declaration [md level]
  [(indent level)
   (java-source-modifiers (:modifiers md))
   (java-source (:type-specifier md) level)
   (if (:type-specifier md) " ")
   (:name md)
   "("
   (interpose ", " (java-source (:parameters md) level))
   ")"
   (if (:body md)
     [" {\n"
      (java-source (:body md) (inc level))
      (indent level)
      "}\n" ]
     ";\n")])

(defmethod java-source :class-declaration [cd level]
  [ (indent level)
    (java-source-modifiers (:modifiers cd))
    "class " (:name cd)
    (if (:implements cd) " implements ")
    (:implements cd)
    (if (:extends cd) " extends ")
    (:extends cd)
    " {\n"
    (java-source (:body cd) (inc level))
    (indent level)
    "}\n"])

(defmethod java-source :interface-declaration [cd level]
  [ (indent level)
    (java-source-modifiers (:modifiers cd))
    "interface " (:name cd)
    (if (:extends cd) " extends ")
    (:extends cd)
    " {\n"
    (java-source (:body cd) (inc level))
    (indent level)
    "}\n"])

(defmethod java-source :var-reference [vr level]
  [ (:object vr) (if (:object vr) ".") (:name vr) ])

(defmethod java-source :modifier [mod level]
  [ (:name mod) ])

(defmethod java-source :statement [st level]
  [ (indent level) (java-source (:body st) level) ";\n" ])

(defmethod java-source :method-invocation [mi level]
  [ (:object mi)
    (if (:object mi) ".")
    (:method mi)
    "("
    (interpose ", " (java-source (:parameters mi) level))
    ")" ])

(defmethod java-source :assignment [as level]
  [ (java-source (:left as) level) " = " (java-source (:right as) level) ])

(defmethod java-source :break [b level]
  [ "\n" ])

(defmethod java-source :line-comment [c level]
  [ (indent level) "// " (:text c) "\n" ])


(defn output-java-source [stream java-source-tree]
  (doall
    (for [s java-source-tree]
      (cond
        (instance? String s) (.print stream s)
        (nil? s) nil
        true (output-java-source stream s))
    )))

(def public (modifier "public"))
(def static (modifier "static"))
(defn ctor-declaration [modifiers name params body] (method-declaration modifiers nil name params body))
(defn super-ctor-invocation [params] (method-invocation nil "super" params))

(defn jna-structure [name var-declarations]
  (class-declaration [public] name nil "Structure" [
    (line-comment "Structure fields")
    (for [v var-declarations] (field-declaration [public] v))
    (break)
    (line-comment "Constructors")
    (ctor-declaration [public] name nil [
      (statement (super-ctor-invocation nil))
      ])
    (ctor-declaration [public] name var-declarations [
      (for [v var-declarations]
        (statement (assignment (var-reference "this" (:name v)) (var-reference nil (:name v)))))
      ])
    (ctor-declaration [public] name [(var-declaration (type-specifier name false) "from")] [
      (for [v var-declarations]
        (statement (assignment (var-reference "this" (:name v)) (var-reference "from" (:name v)))))
      ])
    (break)
    (line-comment "Value and Reference override classes")
    (class-declaration [public static] "ByVal" "Structure.ByVal" name [
      (ctor-declaration [public] "ByVal" nil [
        (statement (super-ctor-invocation nil))
        ])
      ])
    ]))

;(deftest test-deftagged
;  (println (macroexpand '(deftagged method-invocation :object :method :parameters))))

(deftest test-build-model
  (let [vars [(var-declaration (type-specifier "String" false) "field")
              (var-declaration (type-specifier "int" false) "x")]]
    (println (jna-structure "Test" vars))))

(deftest test-model-to-source
  (let [vars [(var-declaration (type-specifier "String" false) "field")
              (var-declaration (type-specifier "int" false) "x")]
        model (jna-structure "Test" vars)
        source-tree (java-source model 0)]
    ;(println source-tree)
    (output-java-source System/out source-tree)))

(run-all-tests)

