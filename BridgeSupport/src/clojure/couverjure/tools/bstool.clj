(ns couverjure.tools.bsgen
  (:use clojure.xml couverjure.type-encoding)
  (:import (java.io File FileWriter PrintWriter)))

(defmulti gen-java :kind)
(defmulti gen-java-ref :kind)

(defmethod gen-java :structure [s]
  (java.text.MessageFormat/format
    (str
      "public class {0} '{'\n"
      "    public static class ByRef extends {0} implements Structure.ByReference '{'};\n"
      "    public static class ByVal extends {0} implements Structure.ByValue '{'};\n"
      "{1}'}'")
    (to-array [(:name s) (apply str (map gen-java (:fields s)))])))

(defmethod gen-java :field [f]
  (let [type (:type f)]
    ; need special handling for array fields as size suffix needs to go last
    (if (= :array (:kind type))
      (format "    public %s %s[];\n"
        (gen-java-ref (:type type))
        (:name f))
      (format "    public %s %s;\n"
        (gen-java-ref type)
        (:name f)))))

(defmethod gen-java-ref :structure [s]
  (:name s))

(defmethod gen-java-ref :array [a]
  (format "%s[%d]"
    (gen-java-ref (:type a)) (:size a)))

(defmethod gen-java-ref :bitfield [b]
  "long")

(defmethod gen-java-ref :pointer [p]
  ; have to peek inside the type to do this right
  (let [type (:type p)]
  (if (and (= :primitive (:kind type)))
    (condp = (:type type)
      :void "Pointer"
      :unknown "Pointer"
      :char "ByteByReference"
      :uchar "ByteByReference"
      :short "ShortByReference"
      :ushort "ShortByReference"
      :int "IntByReference"
      :uint "IntByReference"
      :long "IntByReference"
      :ulong "IntByReference"
      :longlong "LongByReference"
      :ulonglong "LongByReference"
      :float "FloatByReference"
      :double "DoubleByReference"
      "Pointer")
    (str (gen-java-ref type) ".ByRef"))))

(defmethod gen-java-ref :primitive [p]
  (.getName (primitive-java-types (:type p))))

(defn java-output-file [name pkg-name dir]
  (let [parts (seq (.split pkg-name "\\."))
        dirname (apply str (interpose \/ parts))
        rootdir (File. dir)
        pkgdir (File. rootdir dirname)]
    (.mkdirs pkgdir)
    (let [file (File. pkgdir (str name ".java"))]
      (println "java-output-file: " (.getAbsolutePath file))
      (if (.exists file) (.delete file))
      file)))

(defn gen-java-file [name dir pkg-name objc-type]
  (with-open [raw-out (FileWriter. (java-output-file (:name objc-type) pkg-name dir))
              out (PrintWriter. raw-out)]
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
      (.println "")
      (.println (gen-java objc-type))
      )))

(defn generate-framework-classes
  "Generates JNA-based java source files from the .bridgesupport XML file, using the supplied output directory and namespace"
  [bsfilename  output-dir namespace]
  (let [file (File. bsfilename)
        xml (xml-seq (parse file))]
    (doall (for [elem xml :when (= :struct (:tag elem))]
      (let [name (:name (:attrs elem))
            type (or (:type64 (:attrs elem)) (:type (:attrs elem)))
            objc-type (first (type-encoding type))]
        ;(when-not objc-type (println type))
        ;(println type)
        ;(println (gen-java objc-type))
        (gen-java-file name output-dir namespace objc-type)
        )))
    ))

(if (= 3 (count *command-line-args*))
  (apply generate-framework-classes *command-line-args*)
  (println "Usage: bsgen <bridgesupport file> <output-dir> <namespace>"))
