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

(ns couverjure.tools.bsgen
  (:use
    clojure.xml
    clojure.contrib.seq-utils
    couverjure.types
    couverjure.parser
    couverjure.type-encoding
    couverjure.tools.java-model
    couverjure.tools.clojure-model)
  (:import (java.io File FileWriter PrintWriter)))

;
; Some helpers for working with bridgesupport XML elements
;

(defn tag= [t e] (= t (tag e)))

(defn type64-or-type [struct]
  (or (:type64 (:attrs struct)) (:type (:attrs struct))))

;
; ______________________________________________________ output file creation
;

(defn output-file [name pkg-name dir extn]
  (let [parts (seq (.split pkg-name "\\."))
        dirname (apply str (interpose \/ parts))
        rootdir (File. dir)
        pkgdir (File. rootdir dirname)]
    (.mkdirs pkgdir)
    (let [file (File. pkgdir (str name "." extn))]
      (println "output-file: " (.getAbsolutePath file))
      (if (.exists file) (.delete file))
      file)))

(defn java-output-file [name pkg-name dir]
  (output-file name pkg-name (str dir "/java") "java"))

(defn clojure-output-file [name pkg-name dir]
  (output-file name pkg-name (str dir "/clojure") "clj"))

;
; ______________________________________________________ Generating java class files
;

; define a java tab (4 spaces)
(def jtab "    ")

; ______________________________________________________ java type references

; the to-type-spec multimethod generates a type-spec, dispatching on the :kind member
(defmulti to-type-spec :kind)

(defmethod to-type-spec :structure [s]
  (type-spec (if (= :no-name (:name s)) "Object" (str (:name s) ".ByVal"))))

(defmethod to-type-spec :array [a]
  (array-type-spec (:name (to-type-spec (:type a)))))

(defmethod to-type-spec :bitfield [b]
  (type-spec "long"))

(defmethod to-type-spec :pointer [p]
  ; have to peek inside the type to do this right
  (let [type (:type p)]
    (cond
      ; primitive case - get correct pointer type
      (= :primitive (:kind type))
      (type-spec (.getSimpleName (or (:java-type (to-pointer-octype (:type type))) com.sun.jna.Pointer)))
      ; pointer to pointer, use PointerByReference
      (= :pointer (:kind type))
      (type-spec "PointerByReference")
      ; opaque structure - generate ref to opaque PointerType
      (and (= :structure (:kind type)) (not (option? (:fields type))))
      (type-spec (str (:name type) "Pointer"))
      ; default case, use the structure's ByRef type
      true
      (type-spec (str (:name (to-type-spec type)) ".ByRef")))))

(defmethod to-type-spec :primitive [p]
  (type-spec (.getSimpleName (:java-type (:type p)))))

; ______________________________________________________ java class generation

(defn constructor [modifiers name params body] (method-decl modifiers nil name params body))
(defn call-super-ctor [params] (call-method nil "super" params))

(defn structure-modifier-class [name type var-decls]
  (let [interface ({:value "Structure.ByValue" :reference "Structure.ByReference"} type)
        inner-name ({:value "ByVal" :reference "ByRef"} type)]
    (class-decl [public static] inner-name interface name [
      (constructor [public] inner-name nil [
        (statement (call-super-ctor nil))
        ])
      (constructor [public] inner-name var-decls [
        (statement
          (call-super-ctor
            (for [v var-decls] (var-ref nil (:name v)))))
        ])
      (constructor [public] inner-name [(var-decl (type-spec name) "from")] [
        (statement
          (call-super-ctor [(var-ref nil "from")])
          )
        ])
      ])))

(defn structure-class-decl [s]
  (let [name (:name s)
        var-decls (for [f (:fields s)] (var-decl (to-type-spec (:type f)) (:name f)))]
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
      (line-comment "By-value override class")
      (structure-modifier-class name :value var-decls)
      (break)
      (line-comment "By-reference override class")
      (structure-modifier-class name :reference var-decls)
      ])))

(defn opaque-class-decl [s]
  (let [type (:type s)
        name (str (:name type) "Pointer")]
    (class-decl [public] name nil "PointerType" [
      (constructor [public] name nil [
        (statement (call-super-ctor nil))
        ])
      (constructor [public] name [ (var-decl (type-spec "Pointer") "p") ] [
        (statement (call-super-ctor [ (var-ref nil "p") ]))
        ])
      ])))

(defn library-interface-decl [name libfns]
  (interface-decl [public] name "Library" [
    (for [f libfns]
      (let [content
            (content f)
            return-type
            (first
              (for [e content :when (tag= :retval e)]
                (first (type-encoding (type64-or-type e)))))
            ;_ (println "return-decoded: " (:name (attrs f)) " " return-decoded)
            return-type-spec
            (if return-type (to-type-spec return-type) (type-spec "void"))
            args
            (for [e content :when (tag= :arg e)] e)
            arg-decls
            (for [a args]
              (var-decl (to-type-spec (first (type-encoding (type64-or-type a)))) (:name (:attrs a))))
            all-arg-decls
            (if (:variadic (attrs f))
              (concat arg-decls [(var-decl (variadic-type-spec "Object") "rest")])
              arg-decls)]
        (method-decl [public] return-type-spec (:name (:attrs f)) all-arg-decls nil)))
    ]))


; ______________________________________________________ java file output

; execute the block with an output stream on a clojure output file
(defn with-java-file [name dir pkg-name block]
  (with-open [raw-out (FileWriter. (java-output-file name pkg-name dir))
              out (PrintWriter. raw-out)]
    (block out)))

; standard import preamble for all java files
(defn java-file-preamble [pkg-name]
  [ (multiline-comment [ "Generated by bstool" ])
    (package-decl pkg-name)
    (break)
    (import-decl "com.sun.jna" "*")
    (import-decl "com.sun.jna.ptr" "*")
    (import-decl "org.couverjure.core" "*")
    (break) ])

(defn gen-java-file-preamble [out pkg-name]
  (doto out
    (.println (str
      "/*\n"
      " * Generated by bstool\n"
      " */\n"))
    (.println (str "package " pkg-name ";"))
    (.println "")
    (.println "import com.sun.jna.*;")
    (.println "import com.sun.jna.ptr.*;")
    (.println "import org.couverjure.core.*;")
    (.println "")))

; generate a class for a structure
(defn gen-java-struct-file [dir pkg-name struct]
  (let [objc-type (:objc-type struct)]
    (with-java-file (:name objc-type) dir pkg-name
      #(doto %
        ;(gen-java-file-preamble pkg-name)
        ;(format-java-source (emit-java (to-class-decl objc-type)))
        (format-java-source
          (emit-java [
              (java-file-preamble pkg-name)
              (structure-class-decl objc-type)
              ])))
        )))

; generate a class for an opaque pointer type
(defn gen-java-opaque-file [dir pkg-name struct]
  (let [objc-type (:objc-type struct)] ; for an opaque, this is assumed to be a pointer
    (with-java-file (str (:name (:type objc-type)) "Pointer") dir pkg-name ; use name of the referenced struct
      #(doto %
        (format-java-source
          (emit-java [
              (java-file-preamble pkg-name)
              (opaque-class-decl objc-type)
              ])))
        )))

(defn java-library-name [name function-type]
  (str name ({ :inline "Inline" :variadic "Variadic" } function-type)))

(defn function-type-pred [function-type]
  (condp = function-type
    :inline #(:inline (attrs %))
    :variadic #(:variadic (attrs %))
    #(not (or (:inline (attrs %)) (:variadic (attrs %))))))

; generate a class defining external functions
(defn gen-java-functions-file [name dir pkg-name functions function-type]
  (let [classname
        (java-library-name name function-type)
        filtered
        (filter (function-type-pred function-type) functions)]
    (with-java-file classname dir pkg-name
      (fn [out]
        (format-java-source
          out
          (emit-java [
            (java-file-preamble pkg-name)
            (library-interface-decl classname filtered)
            ])))
      )))

; generate all java files for a framework
(defn gen-java-framework [name output-dir java-namespace components]
  (doall (for [struct (:structs components)]
    (gen-java-struct-file output-dir java-namespace struct)
    ))
  (doall (for [op (:opaques components)]
    (gen-java-opaque-file output-dir java-namespace op)
    ))
  (gen-java-functions-file name output-dir java-namespace (:functions components) nil)
  ; inline functions must be written to a separate class since we have to load the bridgesupport dylib
  ; with the compiled versions
  (gen-java-functions-file name output-dir java-namespace (:functions components) :inline)
  ; write variadic functions to a separate class since they cannot be used with JNA direct mapping
  (gen-java-functions-file name output-dir java-namespace  (:functions components) :variadic))


; ______________________________________________________ generating clojure source files

; execute the block with an output stream on a clojure output file
(defn with-clojure-file [name dir pkg-name block]
  (with-open [raw-out (FileWriter. (clojure-output-file name pkg-name dir))
              out (PrintWriter. raw-out)]
    (block out)))

; section separator for clojure code
(def separator "______________________________________________________ ")

(defn clojure-framework-struct [struct struct-type]
  (let [cname
        (:name (:attrs struct))
        jname
        (:name (:objc-type struct))
        members
        (for [f (:fields (:objc-type struct))] (symbol (:name f)))
        struct-classname
        (str jname ({:value "$ByVal" :reference "$ByRef"} struct-type))
        type-prefix
        (if (= :reference struct-type) "^")
        suffix
        (if (= :reference struct-type) "*")
        escape
        (fn [s] (.replace s "\"" "\\\""))]
    (list :no-brackets
      (list (symbol "defoctype") (symbol (str cname suffix)) :break :indent
        (str type-prefix (escape (encode (:objc-type struct))))
        (symbol struct-classname))
      :break :break :unindent
      (list (symbol "defn") (symbol (str (.toLowerCase cname) suffix "?")) :break :indent
        [(symbol "x")] :break
        (list (symbol "instance?") (symbol struct-classname) (symbol "x")))
      :break :break :unindent
      (list (symbol "defn") (symbol (str (.toLowerCase cname) suffix)) :break :indent
        (list (apply vector members) :break :indent
          (list (symbol (str struct-classname ".")) (no-brackets members)))
        (if (< 1 (count members))
          (list :no-brackets :break :unindent
            (list [(symbol "from")] :break :indent
              (list (symbol (str struct-classname ".")) (symbol "from"))))
          (list :no-brackets)))
      :break :break :unindent :unindent
      )))

(defn clojure-framework-structs [structs]
  (no-brackets
    (for [struct structs]
      (let [cname (:name (:attrs struct))]
        (list :no-brackets
          (list :comment (str separator cname))
          :break
          (clojure-framework-struct struct :value)
          (clojure-framework-struct struct :reference)
          )))))

(defn clojure-framework-enums [enums]
  (no-brackets
    (for [enum enums]
      (list :no-brackets
        (list (symbol "def") (symbol (:name (:attrs enum))) (list :source (:value (:attrs enum))))
        :break))))

; must go after functions (non-inline) since it uses the library def
(defn clojure-framework-constants [framework-name constants]
  (let [filtered (filter #(= "@" (:type (attrs %))) constants)
        constants-lib-name (str (.toLowerCase framework-name) "-constants")]
    (list :no-brackets
      (list (symbol "def") (symbol constants-lib-name) (list (symbol "load-framework-constants") framework-name))
      :break :break
      (no-brackets
        (for [c filtered]
          (let [const-name (:name (attrs c))]
            (list :no-brackets
              (list (symbol "def") (symbol const-name) :break :indent
                (list (symbol "framework-constant") (symbol constants-lib-name) const-name))
              :unindent :break :break
              ))
          )))))

(defn clojure-framework-functions [name functions function-type]
  (let [library-class
        (java-library-name name function-type)
        native-library-name
        (if (= function-type :inline) (str name "Inline") name)
        filtered
        (filter (function-type-pred function-type) functions)
        lib-sym (symbol (str (.toLowerCase library-class) "-lib"))]
    (if (seq filtered)
      (list :no-brackets
        (list (symbol "def") lib-sym :break :indent
          (list (symbol "load-framework-library") (symbol library-class) (str native-library-name)))
        :break :break :unindent
        (no-brackets
          (for [f filtered]
            (let [fn-name
                  (:name (attrs f))
                  arg-syms
                  (for [e (content f) :when (tag= :arg e)]
                    (symbol (:name (:attrs e))))
                  all-arg-syms
                  (if (= :variadic function-type)
                    (concat arg-syms [(symbol "rest")])
                    arg-syms)]
              (list :no-brackets
                (list (symbol "defn") (symbol fn-name) :break :indent
                  (apply vector all-arg-syms) :break
                  (list (symbol (str "." fn-name)) lib-sym (no-brackets all-arg-syms)))
                :break :break :unindent)
              )))
        ))))

(defn clojure-framework-classes [name classes]
  (list :no-brackets
    (no-brackets
      (for [c classes]
        (list :no-brackets
          (list (symbol "def") (symbol (:name (attrs c))) (list (symbol "objc-class") (:name (attrs c))))
          :break)
        ))
    :break :break))

(defn gen-clojure-framework [name dir clj-namespace java-namespace components]
  (with-clojure-file (.toLowerCase name) dir clj-namespace
    (fn [out]
      (format-clojure-source out (emit-clojure
        (list :no-brackets
          (list :comment "" "Generated by bstool" "")
          (list (symbol "ns") (symbol (str clj-namespace "." (.toLowerCase name))) :break :indent
          ;(list (symbol "ns") (symbol clj-namespace) :break :indent
            (list :use (symbol "couverjure.core") (symbol "couverjure.types") (symbol "couverjure.frameworks")) :break
            (list :import
              (list (symbol java-namespace) :break :indent
                (list :no-brackets
                  (symbol name) :break
                  (symbol (str name "Inline")) :break
                  (symbol (str name "Variadic")) :break
                  (no-brackets
                    (for [s (:structs components)]
                      (let [sname (:name (:objc-type s))]
                        (list :no-brackets
                          (symbol (str sname "$ByRef"))
                          :break
                          (symbol (str sname "$ByVal"))
                          :break)))))
                )))
          :break :unindent :unindent
          (list :comment (str separator "structs")) :break
          (clojure-framework-structs (:structs components))
          (list :comment (str separator "functions (non-inline)")) :break
          (clojure-framework-functions name (:functions components) nil)
          (list :comment (str separator "functions (variadic)")) :break
          (clojure-framework-functions name (:functions components) :variadic)
          (list :comment (str separator "functions (inline)")) :break
          (clojure-framework-functions name (:functions components) :inline)
          (list :comment (str separator "enums")) :break
          (clojure-framework-enums (:enums components)) :break
          (list :comment (str separator "constants")) :break
          (clojure-framework-constants name (:constants components)) :break
          (list :comment (str separator "classes")) :break
          (clojure-framework-classes name (:classes components))
          ))))))

; ______________________________________________________ tool main

(defn generate-framework-classes
  "Generates JNA-based java source files from the .bridgesupport XML file, using the supplied output directory and namespace"
  [name bsfilename output-dir java-namespace clj-namespace]
  (let [file (File. bsfilename)
        xml (xml-seq (parse file))
        components {
        :structs
        (for [elem xml :when (tag= :struct elem)]
          (assoc elem
            :objc-type (first (type-encoding (type64-or-type elem)))))
        :opaques
        (for [elem xml :when (tag= :opaque elem)]
          (assoc elem
            :objc-type (first (type-encoding (type64-or-type elem)))))
        :enums
        (for [elem xml :when (tag= :enum elem)] elem)
        :functions
        (for [elem xml :when (tag= :function elem)] elem)
        :constants
        (for [elem xml :when (tag= :constant elem)] elem)
        :classes
        (for [elem xml :when (tag= :class elem)] elem)
        }]
    (gen-java-framework name output-dir java-namespace components)
    ; one clojure module will reference all three classes, clojure code shouldn't need to know these details
    (gen-clojure-framework name output-dir clj-namespace java-namespace components)))

(if (= 5 (count *command-line-args*))
  (apply generate-framework-classes *command-line-args*)
  (println "Usage: bsgen <name> <bridgesupport file> <output-dir> <java-namespace> <clj-namespace>"))
