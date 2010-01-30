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

(ns couverjure.test
  (:use couverjure.core couverjure.parser couverjure.type-encoding clojure.test)
  (:import (com.sun.jna.ptr LongByReference)))

(def NSObject (objc-class :NSObject))
(def NSString (objc-class :NSString))

(deftest test-simple-encoding
  (let [result (method-signature-encoding "@24@0:8@16")]
    (is (= 4 (count (first result))))
    (is (empty? (second result)))))

(deftest test-pointer-encoding
  (let [result (method-signature-encoding "@32@0:8@16^Q24")]
    (is (= 5 (count (first result))))
    (is (empty? (second result)))))

(deftest test-qualifier-encoding
  (let [result (method-signature-encoding "@32@0:8r*16Q24")]
    (is (= 5 (count (first result))))
    (is (empty? (second result)))))

(deftest test-structure-encoding
  (let [result (method-signature-encoding "v56@0:8{CGRect={CGPoint=dd}{CGSize=dd}}16@48")]
    (println result)
    (is (= 5 (count (first result))))
    (is (empty? (second result)))))

(deftest test-structure-encoding2
  (let [result (method-signature-encoding "v56@0:8{CGPoint=dd}16@48")]
    (is (= 5 (count (first result))))
    (is (empty? (second result)))))

(deftest test-array-encoding
  (let [result (method-signature-encoding "v56@0:8[12f]48")]
    (is (= 4 (count (first result))))
    (is (empty? (second result)))))

(deftest test-type-name
  (let [result (type-name "CGRect")]
    (is (empty? (second result)))))

(deftest test-structure-arg
  (let [parser (pattern [ \{ (choice [ alpha digit ]) \= (series primitive) \} ])
        result (parser "{1=dd}")]
    (is (empty? (second result)))))

(deftest test-option-number
  (let [parser (option number)]
    (number "H")))


(defn test-all-method-sigs [class]
  (let [out-count (LongByReference.)
        method-list-ptr (.class_copyMethodList objc-runtime class out-count)
        method-count (.getValue out-count)
        method-list (seq (.getPointerArray method-list-ptr 0 method-count))]
    (println "out-count " method-count " " (count method-list))
    (doall
      (for [[encoding, failure]
            (filter (fn [[encoding, result]] (not (empty? (second result))))
              (for [method method-list]

                (let [encoding (.method_getTypeEncoding objc-runtime method)]
                  (try
                    [encoding, (method-signature-encoding encoding)]
                    (catch Throwable e [encoding, [nil, (.toString e)]]))
                  )))]
        (println encoding "\n" (second failure))))))

(deftest test-method-sigs
  (test-all-method-sigs NSString)
  (test-all-method-sigs NSObject))


(defmethod report :begin-test-var [m] (println "beginning test " (:var m)))

(run-tests)
