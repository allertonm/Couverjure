(ns couverjure.tools.java-model
  (:use clojure.test))

(defmacro deftagged [name & keys]
  (let [keys-as-symbols (for [k keys] (symbol (subs (str k) 1)))
        struct-name (gensym)]
  `(do
    (defstruct ~struct-name :tag ~@keys)
    (defn ~name [~@keys-as-symbols] (struct ~struct-name ~(keyword (str name)) ~@keys-as-symbols)))))

(deftagged type-spec :name)
(deftagged array-type-spec :name)
(deftagged variadic-type-spec :name)
(deftagged var-decl :type-spec :name)
(deftagged field-decl :modifiers :var-decl)
(deftagged method-decl :modifiers :type-spec :name :parameters :body)
(deftagged class-decl :modifiers :name :implements :extends :body)
(deftagged interface-decl :modifiers :name :extends :body)
(deftagged var-ref :object :name)
(deftagged modifier :name)
(deftagged package-decl :package-name)
(deftagged import-decl :package-name :class-name)

(deftagged statement :body)
(deftagged call-method :object :method :parameters)
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
  (for [m s] (java-source m level)))

(defmethod java-source :type-spec [ts level]
  [ (:name ts) ])

(defmethod java-source :array-type-spec [ts level]
  [ (:name ts) "[]" ])

(defmethod java-source :variadic-type-spec [ts level]
  [ (:name ts) "..." ])

(defmethod java-source :var-decl [vd level]
  [ (java-source (:type-spec vd) level) " " (:name vd) ])

(defmethod java-source :field-decl [fd level]
  [ (indent level)
    (java-source-modifiers (:modifiers fd))
    (java-source (:var-decl fd) level)
    ";\n"])

(defmethod java-source :method-decl [md level]
  [(indent level)
   (java-source-modifiers (:modifiers md))
   (java-source (:type-spec md) level)
   (if (:type-spec md) " ")
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

(defmethod java-source :class-decl [cd level]
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

(defmethod java-source :interface-decl [cd level]
  [ (indent level)
    (java-source-modifiers (:modifiers cd))
    "interface " (:name cd)
    (if (:extends cd) " extends ")
    (:extends cd)
    " {\n"
    (java-source (:body cd) (inc level))
    (indent level)
    "}\n"])

(defmethod java-source :var-ref [vr level]
  [ (:object vr) (if (:object vr) ".") (:name vr) ])

(defmethod java-source :modifier [mod level]
  [ (:name mod) ])

(defmethod java-source :statement [st level]
  [ (indent level) (java-source (:body st) level) ";\n" ])

(defmethod java-source :call-method [mi level]
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

(defmethod java-source :package-decl [p level]
  [ (indent level) "package " (:package-name p) ";\n" ])

(defmethod java-source :import-decl [i level]
  [ (indent level) "import " (:package-name i) "." (:class-name i) ";\n" ])


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
(defn constructor [modifiers name params body] (method-decl modifiers nil name params body))
(defn call-super-ctor [params] (call-method nil "super" params))

(defn jna-structure [name var-decls]
  (class-decl [public] name nil "Structure" [
    (line-comment "Structure fields")
    (for [v var-decls] (field-decl [public] v))
    (break)
    (line-comment "Constructors")
    (constructor [public] name nil [
      (statement (call-super-ctor nil))
      ])
    (constructor [public] name var-decls [
      (for [v var-decls]
        (statement (assignment (var-ref "this" (:name v)) (var-ref nil (:name v)))))
      ])
    (constructor [public] name [(var-decl (type-spec name) "from")] [
      (for [v var-decls]
        (statement (assignment (var-ref "this" (:name v)) (var-ref "from" (:name v)))))
      ])
    (break)
    (line-comment "Value and Reference override classes")
    (class-decl [public static] "ByVal" "Structure.ByVal" name [
      (constructor [public] "ByVal" nil [
        (statement (call-super-ctor nil))
        ])
      (constructor [public] "ByVal" var-decls [
        (statement
          (call-super-ctor
            (for [v var-decls] (var-ref nil (:name v)))))
        ])
      ])
    ]))

(defn jna-structure-file [name var-decls]
  [(package-decl "org.couverjure.cocoa")
   (break)
   (import-decl "com.sun.jna" "*")
   (break)
   (jna-structure name var-decls)
   (break)
   (interface-decl [public] "MyLibrary" "Library" [
     (method-decl [public]
       (type-spec "int") "NSThing"
       [(var-decl (type-spec "String") "field")
        (var-decl (type-spec "int") "x")]
       nil)
     ])])

;(deftest test-deftagged
;  (println (macroexpand '(deftagged call-method :object :method :parameters))))

(deftest test-build-model
  (let [vars [(var-decl (type-spec "String") "field")
              (var-decl (type-spec "int") "x")]]
    (println (jna-structure "Test" vars))))

(deftest test-model-to-source
  (let [vars [(var-decl (type-spec "String") "field")
              (var-decl (type-spec "int") "x")]
        model (jna-structure-file "Test" vars)
        source-tree (java-source model 0)]
    ;(println source-tree)
    (output-java-source System/out source-tree)))

(run-all-tests)

