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
  (:use couverjure.types couverjure.core couverjure.cocoa.foundation clojure.test)
  (:import
    (com.sun.jna Memory)))

(def NSString (objc-class :NSString))
(def NSObject (objc-class :NSObject))


(println (.CFStringCreateWithCString foundation nil "Hello World" 0))

(def test-nsstring (.releaseOnFinalize (.CFStringCreateWithCString foundation nil "Hello World" 0)))
(def test-nsstring2 (.releaseOnFinalize (.CFStringCreateWithCString foundation nil "Hello World2" 0)))

; this test exercises the low level functions without macros
(deftest test-create-nsstring-subclass
  (let [mystring (new-objc-class (str (gensym)) NSString)
        hello-str "Hello World"]
    (add-method mystring "length" [OCUInt OCID OCSel] (fn [self sel] (count hello-str)))
    (add-method mystring "characterAtIndex:" [OCUnichar OCID OCSel OCUInt] (fn [self sel index] (nth hello-str index)))
    (register-objc-class mystring)
    (let
      [hello (alloc mystring)]
      (dynamic-send-msg hello "init")
      (let [cai (dynamic-send-msg hello "characterAtIndex:" 0)] (is (= cai 72)))
      (is (>> hello :isEqual test-nsstring))
      (.NSLog foundation hello))))

(deftest test-new-implementation-macro
  (let [hello-str "Hello World"
        mystring
        (implementation (str (gensym)) NSString
          (method [OCUInt :length] [] (count hello-str))
          (method [OCUnichar :characterAtIndex OCUInt] [index] (nth hello-str index)))
        hello (alloc mystring)]
    (>> hello :init)
    (let [cai (>> hello :characterAtIndex 0)] (is (= cai 72)))
    (is (>> hello :isEqual test-nsstring))
    (.NSLog foundation hello)))

(deftest test-dynamic-send-msg
  (let [hello-str "Hello World"
        mystring
        (implementation (str (gensym)) NSString
          (method [OCUInt :length] [] (count hello-str))
          (method [OCUnichar :characterAtIndex OCUInt] [index] (nth hello-str index)))
        hello (alloc mystring)]
    (dynamic-send-msg hello "init")
    (let [cai (dynamic-send-msg hello "characterAtIndex:" 0)] (is (= cai 72)))
    (is (>> hello :isEqual test-nsstring))
    (.NSLog foundation hello)))

(deftest test-super-init
  (let [hello-str "Hello World"
        mystring
        (implementation (str (gensym)) NSString
          (method [OCID :init] []
            (println "in init: " self)
            (let [_self (>>super self :init)]
              (println "self: " _self)
              _self))
          (method [OCUInt :length] [] (count hello-str))
          (method [OCUnichar :characterAtIndex OCUInt] [index] (nth hello-str index)))
        hello (alloc mystring)]
    (>> hello :init)
    (let [cai (>> hello :characterAtIndex 0)] (is (= cai 72)))
    (is (>> hello :isEqual test-nsstring))
    (.NSLog foundation hello)))

(deftest test-state
  (let [mystring
        (implementation (str (gensym)) NSString
          (method [OCID :init] []
            (let [_self (>>super self :init)]
              (init _self {:value "Hello World"})
              (println "self: " _self)
              _self))
          (method [OCUInt :length] [] (count (:value (properties self))))
          (method [OCUnichar :characterAtIndex OCUInt] [index] (nth (:value (properties self)) index)))
        hello (alloc mystring)]
    (>> hello :init)
    (let [cai (>> hello :characterAtIndex 0)] (is (= cai 72)))
    (is (>> hello :isEqual test-nsstring))
    (.NSLog foundation hello)))

(deftest test-property-accessors
  (let [test-class
        (implementation (str (gensym)) NSObject
          (property :testString :atom)
          (method [OCID :init] []
            (let [_self (>>super self :init)]
              (init _self {:testString (atom test-nsstring)})
              _self)))
        test (alloc test-class)]
    (>> test :init)
    (let [read-property (>> test :testString)]
      (is (>> read-property :isEqual test-nsstring)))
    (is (do (>> test :setTestString test-nsstring2) true) "Set accessor ran without exceptions")
    (let [read-property (>> test :testString)]
      (is (>> read-property :isEqual test-nsstring2)))
    ))

(deftest test-string-coercion
  (with-autorelease-pool
    (let [utf8String (>> test-nsstring :UTF8String)]
      (is (= utf8String "Hello World")))))

; the NSRange tests probably need to move out of test-core, since they also require
; couverjure.cocoa.foundation to be generated from BridgeSupport

; this test will test two things
; 1) use of a structure argument (the NSRange arg to getCharacters:inRange:) both
; as a sender and as a receiver
; 2) (more minor) use of a memory buffer to receive the result
(deftest test-using-nsrange
  (let [hello-str "Hello World"
        mystring
        (implementation (str (gensym)) NSString
          (method [OCUInt :length] [] (count hello-str))
          (method [OCUnichar :characterAtIndex OCUInt] [index] (nth hello-str index))
          (method [OCVoid :getCharacters OCUnknown :range NSRange] [ptr range]
            (println "getCharacters: " ptr " range: " range)
            (>>super self :getCharacters ptr :range range)))
        hello (alloc mystring)]
    (>> hello :init)
    (let [buffer (Memory. (* 2 (count hello-str)))
          range (nsrange 0 (count hello-str))]
      (println "range: " range)
      (>> hello :getCharacters buffer :range range)
      (is (= 72 (.getShort buffer 0))))
    ))

(deftest nsstring-getcharacters-inrange
  (let [size (count "Hello World")
        ; This example shows us that we need to do better than just use JNA's Memory class! Ugh.
        buffer (Memory. (* 2 size))]
    (>> test-nsstring :getCharacters buffer :range (nsrange 0 size))
    (is (= 72 (.getShort buffer 0)))
    (is (= 100 (.getShort buffer (* 2 (dec size)))))))

(deftest test-inlined-nsmakerange
  (let [range (nsmakerange 0 1)]
    (is (nsrange? range))
    (is (= 0 (.location range)))
    (is (= 1 (.length range)))))

(deftest test-constants
  (is (not (nil? NSDecimalDigits)))
  (println NSDecimalDigits))

(deftest test-thread-adapter
  (let [ThreadAdapter
        (implementation (str (gensym)) NSObject
          (method [OCVoid :onMainThread] []
            (println "Hello World")))
        thread-adapter (alloc ThreadAdapter)]
    (>> thread-adapter :init)
    (>> thread-adapter :performSelectorOnMainThread (selector :onMainThread) :withObject nil :waitUntilDone true)))

(defmethod report :begin-test-var [m] (println "beginning test " (:var m)))
(defmethod report :end-test-var [m] (System/gc) (println "ending test " (:var m)))

(run-tests)
(System/gc)
