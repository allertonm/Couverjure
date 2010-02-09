(ns couverjure.tools.bsgen
  (:use clojure.xml couverjure.type-encoding)
  (:import (java.io File)))

(defmulti gen-java :kind)
(defmulti gen-java-ref :kind)

(defmethod gen-java :structure [s]
  (format "public class %s {\n%s}"
    (:name s)
    (apply str (map gen-java (:fields s)))))

(defmethod gen-java :field [f]
  (format "    public %s %s;\n"
    (gen-java-ref (:type f))
    (:name f)))

(defmethod gen-java-ref :structure [s]
  (:name s))

(defmethod gen-java-ref :array [a]
  (format "%s[%d]"
    (gen-java-ref (:type a)) (:size a)))

(defmethod gen-java-ref :bitfield [b]
  "long")

(defmethod gen-java-ref :pointer [p]
  (str (gen-java-ref (:type p)) "ByRef"))

(defmethod gen-java-ref :primitive [p]
  (.getName (primitive-java-types (:type p))))

(defn generate-framework-classes
  "Generates JNA-based java source files from the .bridgesupport XML file, using the supplied output directory and namespace"
  [bsfilename namespace output-dir]
  (let [file (File. bsfilename)
        xml (xml-seq (parse file))]
    (doall (for [elem xml :when (= :struct (:tag elem))]
      (let [name (:name (:attrs elem))
            type (or (:type64 (:attrs elem)) (:type (:attrs elem)))
            objc-type (first (type-encoding type))]
        ;(when-not objc-type (println type))
        (println (gen-java objc-type))
        )))
    ))

(if (= 3 (count *command-line-args*))
  (apply generate-framework-classes *command-line-args*)
  (println "Usage: bsgen <bridgesupport file> <output-dir> <namespace>"))
