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
  (:use clojure.xml couverjure.types couverjure.parser couverjure.type-encoding)
  (:import (java.io File FileWriter PrintWriter)))

;
; Working with bridgesupport XML elements
;

(defn struct-sig [struct]
  (or (:type64 (:attrs struct)) (:type (:attrs struct))))


;
; Java source code generation
;

; generate java code (i.e class XX { ... })
(defmulti gen-java :kind)
; generate a reference to a type (i.e XX)
(defmulti gen-java-ref :kind)

(def jtab "    ")
(def ctab "  ")

(defn java-struct-ctor [s]
  (str
    jtab "public " (:name s) "("
    (apply str
      (interpose
        ", "
        (for [f (:fields s)]
          (str (gen-java-ref (:type f)) " " (:name f)))))
    ") {\n"
    (apply str
      (for [f (:fields s)]
        (str jtab jtab "this." (:name f) " = " (:name f) ";\n")))
    jtab "}\n"))

(defn java-struct-copy-ctor [s]
  (str
    jtab "public " (:name s) "(" (:name s) " from) {\n"
    (apply str
      (for [f (:fields s)]
        (str jtab jtab "this." (:name f) " = from." (:name f) ";\n")))
    jtab "}\n"))

(defn java-derived-struct-ctor [s name]
  (str
    jtab jtab "public " name "("
    (apply str
      (interpose
        ", "
        (for [f (:fields s)]
          (format "%s %s" (gen-java-ref (:type f)) (:name f)))))
    ") {\n"
    jtab jtab jtab "super("
    (apply str
      (interpose
        ", "
        (for [f (:fields s)] (:name f))))
    ");\n"
    jtab jtab "}\n"))

(defn java-derived-struct-copy-ctor [s name]
  (str
    jtab jtab "public " name "(" (:name s) " from) {\n"
    jtab jtab jtab "super(from);\n"
    jtab jtab "}\n"))

(defn java-member-var [f]
  (let [type (:type f)]
    ; need special handling for array fields as size suffix needs to go last
    ;(if (= :array (:kind type))
    ;  (format "    public %s %s[];\n"
    ;    (gen-java-ref (:type type))
    ;    (:name f))
    (str
      jtab "public "
      (gen-java-ref type)
      " "
      (:name f)
      ";\n")))

(defn java-arg [f]
  (format "%s %s"
    (gen-java-ref (:type f))
    (:name f)))


(defmethod gen-java :structure [s]
  (let [name (:name s)]
    (str
      "public class " name " extends Structure {\n"

      (apply str (map java-member-var (:fields s))) "\n"

      jtab "public " name "() { super(); }\n\n"
      (java-struct-ctor s) "\n"
      (java-struct-copy-ctor s) "\n"

      jtab "public static class ByRef extends " name " implements Structure.ByReference {\n"
      jtab jtab "public ByRef() { super(); }\n"
      (java-derived-struct-ctor s "ByRef")
      (java-derived-struct-copy-ctor s "ByRef")
      jtab "};\n\n"

      jtab "public static class ByVal extends " name " implements Structure.ByValue {\n"
      jtab jtab "public ByVal() { super(); }\n"
      (java-derived-struct-ctor s "ByVal")
      (java-derived-struct-copy-ctor s "ByVal")
      jtab "};\n"

      "}")))

(defmethod gen-java :pointer [s]
  (let [type (:type s)
        name (:name type)]
    (str
      "public class " name "Pointer extends PointerType {\n"

      jtab "public " name "Pointer() { super(); }\n"
      jtab "public " name "Pointer(Pointer p) { super(p); }\n"

      "}")))

(defmethod gen-java-ref :structure [s]
  (if (= :no-name (:name s)) "Object" (:name s)))

(defmethod gen-java-ref :array [a]
  (format "%s[]"
    (gen-java-ref (:type a)) (:size a)))

(defmethod gen-java-ref :bitfield [b]
  "long")

(defmethod gen-java-ref :pointer [p]
  ; have to peek inside the type to do this right
  (let [type (:type p)]
    (cond
      ; primitive case - get correct pointer type
      (= :primitive (:kind type))
      (.getSimpleName (or (:java-type (to-pointer-octype (:type type))) com.sun.jna.Pointer))
      ; pointer to pointer, use PointerByReference
      (= :pointer (:kind type))
      "PointerByReference"
      ; opaque structure - generate ref to opaque PointerType
      (and (= :structure (:kind type)) (not (option? (:fields type))))
      (str (:name type) "Pointer")
      ; default case, use the structure's ByRef type
      true
      (str (gen-java-ref type) ".ByRef"))))

(defmethod gen-java-ref :primitive [p]
  (.getSimpleName (:java-type (:type p))))

;
; output file creation
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
; Generating java class files
;

(defn with-java-file [name dir pkg-name block]
  (with-open [raw-out (FileWriter. (java-output-file name pkg-name dir))
              out (PrintWriter. raw-out)]
    (block out)))

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

(defn gen-java-struct-file [dir pkg-name struct]
  (let [objc-type (:objc-type struct)]
    (with-java-file (:name objc-type) dir pkg-name
      #(doto %
        (gen-java-file-preamble pkg-name)
        (.println (gen-java objc-type))
        ))))

(defn gen-java-opaque-file [dir pkg-name struct]
  (let [objc-type (:objc-type struct)] ; for an opaque, this is assumed to be a pointer
    (with-java-file (str (:name (:type objc-type)) "Pointer") dir pkg-name ; use name of the referenced struct
      #(doto %
        (gen-java-file-preamble pkg-name)
        (.println (gen-java objc-type))
        ))))


(defn tag= [t e] (= t (tag e)))

(defn gen-java-functions-file [name dir pkg-name functions]
  (with-java-file "Functions" dir pkg-name
    (fn [out]
      (gen-java-file-preamble out pkg-name)
      (.print out (str
        "public interface Functions extends Library {\n"))
      (doall (for [f functions]
        (let [content
              (content f)
              return-type
              (first
                (for [e content :when (tag= :retval e)]
                  (first (type-encoding (struct-sig e)))))
              args
              (for [e content :when (tag= :arg e)] e)
              arg-types
              (for [a args]
                (type-encoding (struct-sig a)))]
          ;(println "f " f " content " content " return-type")
          (.print out (str
            jtab "public " (if return-type (gen-java-ref return-type) "void") " " (:name (attrs f)) "("
            (apply str
              (interpose ", "
                (for [i (range 0 (count arg-types))]
                  (let [arg (nth args i)
                        type (first (type-encoding (struct-sig arg)))
                        name (or (:name (attrs arg)) (str "p" i))]
                    (str (gen-java-ref type) " " name)))))
            ");\n"))
          )))
      (.print out "}\n")
      )))

; generating clojure source files

(defn with-clojure-file [name dir pkg-name block]
  (with-open [raw-out (FileWriter. (clojure-output-file name pkg-name dir))
              out (PrintWriter. raw-out)]
    (block out)))

(defn print-separator [out name]
  (.print out (str
    "; ______________________________________________________ " name "\n"
    "\n"
    )))

(defn gen-clojure-framework [name dir clj-namespace java-namespace structs enums]
  (with-clojure-file name dir clj-namespace
    (fn [out]
      (.print out (str
        ";\n"
        "; Generated by bstool\n"
        ";\n"))
      (.print out
        (str
          "(ns " clj-namespace "." name "\n"
          ctab "(:use couverjure.types)\n" ; requires the defoctype macro
          ctab "(:import\n"
          ctab ctab "(" java-namespace "\n"
          ctab ctab ctab
          (apply str
            (interpose
              (str "\n" ctab ctab ctab)
              (apply concat (for [s structs]
                (let [sname (:name (:objc-type s))]
                  [sname (str sname "$ByRef") (str sname "$ByVal")])))
              ))
          ")))\n\n"))
      (doall (for [struct structs]
        (let [cname (:name (:attrs struct))
              jname (:name (:objc-type struct))
              members (apply str (interpose " " (for [f (:fields (:objc-type struct))] (:name f))))
              escape (fn [s] (.replace s "\"" "\\\""))]
          ; generate a def for the class
          (print-separator out cname)
          ; define symbols for use in method type signatures
          (.print out (str
            "(defoctype " cname "\n"
            ctab "\"" (escape (encode (:objc-type struct))) "\"\n"
            ctab jname "$ByVal)\n\n"))
          (.print out (str
            "(defoctype " cname "*\n" 
            ctab "\"^" (escape (encode (:objc-type struct))) "\"\n"
            ctab jname "$ByRef)\n\n"))
            ;"(def " cname "* (assoc :java-type " jname "$ByVal (type-encoding \"^" (escape (struct-sig struct)) "\")))\n\n"))
          ; define constructors for value type
          (.print out (str
            "(defn " (.toLowerCase cname) "\n"
            ctab "([" members "]\n"
            ctab ctab "(" jname "$ByVal. " members "))"
            (if (< 1 (count members))
              (str "\n" ctab "([from]\n"
                ctab ctab "(" jname "$ByVal. from))")
              "")
            ")\n\n"))
          ; define constructors for reference type
          (.print out (str
            "(defn " (.toLowerCase cname) "*\n"
            ctab "([]\n"
            ctab ctab "(" jname "$ByRef.))\n"
            ctab "([" members "]\n"
            ctab ctab "(" jname "$ByRef. " members "))"
            (if (< 1 (count members))
              (str "\n" ctab "([from]\n"
                ctab ctab "(" jname "$ByRef. from))")
              "")
            ")\n\n"))
          (.print out (str
            "(defn " (.toLowerCase cname) "? [x]\n" ctab "(instance? " jname " x))\n\n"))
          )))
      (print-separator out "enums")
      (doall (for [enum enums]
        (.print out (str
          "(def " (:name (:attrs enum)) " " (:value (:attrs enum)) ")\n"))))
      )))

(defn generate-framework-classes
  "Generates JNA-based java source files from the .bridgesupport XML file, using the supplied output directory and namespace"
  [name bsfilename output-dir java-namespace clj-namespace]
  (let [file (File. bsfilename)
        xml (xml-seq (parse file))
        structs
        (for [elem xml :when (tag= :struct elem)]
          (assoc elem
            :objc-type (first (type-encoding (struct-sig elem)))))
        opaques
        (for [elem xml :when (tag= :opaque elem)]
          (assoc elem
            :objc-type (first (type-encoding (struct-sig elem)))))
        enums
        (for [elem xml :when (tag= :enum elem)] elem)
        functions
        (for [elem xml :when (tag= :function elem)] elem)]
    (doall (for [struct structs]
      (gen-java-struct-file output-dir java-namespace struct)
      ))
    (doall (for [op opaques]
      (gen-java-opaque-file output-dir java-namespace op)
      ))
    (gen-java-functions-file name output-dir java-namespace functions)
    (gen-clojure-framework name output-dir clj-namespace java-namespace structs enums)))

(if (= 5 (count *command-line-args*))
  (apply generate-framework-classes *command-line-args*)
  (println "Usage: bsgen <name> <bridgesupport file> <output-dir> <java-namespace> <clj-namespace>"))
