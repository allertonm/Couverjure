;;; json/write.clj: JavaScript Object Notation (JSON) generator

;; by Stuart Sierra, http://stuartsierra.com/
;; May 9, 2009

;; Copyright (c) Stuart Sierra, 2009. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.


(ns 
  #^{:author "Stuart Sierra",
     :doc "JavaScript Object Notation (JSON) generator.

This library will generate JSON from the following types:
 * nil
 * all primitives (Boolean, Byte, Short, Integer, Long, Float, Double)
 * String (actually any CharSequence)
 * java.util.Map (including Clojure maps)
 * java.util.Collection (including Clojure vectors, lists, and sets)
 * Java arrays

You can extend this library to handle new types by adding methods to
print-json.

This library does NOT attempt to preserve round-trip equality between
JSON and Clojure data types. That is, if you write a JSON string with
this library, then read it back with clojure.contrib.json.read, you
won't necessarily get the exact same data structure.  For example,
Clojure sets are written as JSON arrays, which will be read back as
Clojure vectors.

If you want indented output, try the clojure-json library at
http://github.com/danlarkin/clojure-json

This implementation attempts to follow the description of JSON at
<http://json.org/>.  Maps become JSON objects, all other collections
become JSON arrays.  JSON object keys are always converted to strings.
Within strings, all non-ASCII characters are hexadecimal escaped.
",
     :see-also [["http://json.org/", "JSON Home Page"]]}
  clojure.contrib.json.write
  (:require [clojure.contrib.java-utils :as j])
  (:use [clojure.test :only (deftest- is)]))

(defmulti
  #^{:doc "Prints x as JSON.  Nil becomes JSON null.  Keywords become
  strings, without the leading colon.  Maps become JSON objects, all
  other collection types become JSON arrays.  Java arrays become JSON
  arrays.  Unicode characters in strings are escaped as \\uXXXX.
  Numbers print as with pr."
     :arglists '([x])}
  print-json (fn [x] (cond
                       (nil? x) nil  ;; prevent NullPointerException on next line
                       (.isArray (class x)) ::array
                       :else (type x))))


;; Primitive types can be printed with Clojure's pr function.
(derive java.lang.Boolean ::pr)
(derive java.lang.Byte ::pr)
(derive java.lang.Short ::pr)
(derive java.lang.Integer ::pr)
(derive java.lang.Long ::pr)
(derive java.lang.Float ::pr)
(derive java.lang.Double ::pr)

;; Collection types can be printed as JSON objects or arrays.
(derive java.util.Map ::object)
(derive java.util.Collection ::array)

;; Symbols and keywords are converted to strings.
(derive clojure.lang.Symbol ::symbol)
(derive clojure.lang.Keyword ::symbol)


(defmethod print-json ::pr [x] (pr x))

(defmethod print-json nil [x] (print "null"))

(defmethod print-json ::symbol [x] (print-json (name x)))

(defmethod print-json ::array [s]
  (print \[)
  (loop [x s]
    (when (seq x)
     (let [fst (first x)
           nxt (next x)]
       (print-json fst)
       (when (seq nxt)
         (print \,)
         (recur nxt)))))
  (print \]))

(defmethod print-json ::object [m]
  (print \{)
  (loop [x m]
    (when (seq m)
      (let [[k v] (first x)]
        (when (nil? k)
          (throw (Exception. "JSON object keys cannot be nil/null")))
        (print-json (j/as-str k))
        (print \:)
        (print-json v))
      (let [nxt (next x)]
        (when (seq nxt)
          (print \,)
          (recur nxt)))))
  (print \}))

(defmethod print-json java.lang.CharSequence [#^CharSequence s]
  (let [sb (StringBuilder. (count s))]
    (.append sb \")
    (dotimes [i (count s)]
      (let [cp (Character/codePointAt s i)]
        (cond
         ;; Handle printable JSON escapes before ASCII
         (= cp 34) (.append sb "\\\"")
         (= cp 92) (.append sb "\\\\")
         (= cp 47) (.append sb "\\/")
         ;; Print simple ASCII characters
         (< 31 cp 127) (.append sb (.charAt s i))
         ;; Handle non-printable JSON escapes
         (= cp 8) (.append sb "\\b")
         (= cp 12) (.append sb "\\f")
         (= cp 10) (.append sb "\\n")
         (= cp 13) (.append sb "\\r")
         (= cp 9) (.append sb "\\t")
         ;; Any other character is Hexadecimal-escaped
         :else (.append sb (format "\\u%04x" cp)))))
    (.append sb \")
    (print (str sb))))

(defn json-str
  "Converts x to a JSON-formatted string."
  [x]
  (with-out-str (print-json x)))



;;; TESTS

;; Run these tests with
;; (clojure.test/run-tests 'clojure.contrib.print-json)

;; Bind clojure.test/*load-tests* to false to omit these
;; tests from production code.

(deftest- can-print-json-strings
  (is (= "\"Hello, World!\"" (json-str "Hello, World!")))
  (is (= "\"\\\"Embedded\\\" Quotes\"" (json-str "\"Embedded\" Quotes"))))

(deftest- can-print-unicode
  (is (= "\"\\u1234\\u4567\"" (json-str "\u1234\u4567"))))

(deftest- can-print-json-null
  (is (= "null" (json-str nil))))

(deftest- can-print-json-arrays
  (is (= "[1,2,3]" (json-str [1 2 3])))
  (is (= "[1,2,3]" (json-str (list 1 2 3))))
  (is (= "[1,2,3]" (json-str (sorted-set 1 2 3))))
  (is (= "[1,2,3]" (json-str (seq [1 2 3])))))

(deftest- can-print-java-arrays
  (is (= "[1,2,3]" (json-str (into-array [1 2 3])))))

(deftest- can-print-empty-arrays
  (is (= "[]" (json-str [])))
  (is (= "[]" (json-str (list))))
  (is (= "[]" (json-str #{}))))

(deftest- can-print-json-objects
  (is (= "{\"a\":1,\"b\":2}" (json-str (sorted-map :a 1 :b 2)))))

(deftest- object-keys-must-be-strings
  (is (= "{\"1\":1,\"2\":2") (json-str (sorted-map 1 1 2 2))))

(deftest- can-print-empty-objects
  (is (= "{}" (json-str {}))))

(deftest- accept-sequence-of-nils
  (is (= "[null,null,null]" (json-str [nil nil nil]))))

(deftest- error-on-nil-keys
  (is (thrown? Exception (json-str {nil 1}))))

(deftest- characters-in-symbols-are-escaped
  (is (= "\"foo\\u1b1b\"" (json-str (symbol "foo\u1b1b")))))