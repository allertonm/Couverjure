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
  (:use couverjure.core clojure.test))

(def NSString (objc-class :NSString))
(def NSObject (objc-class :NSObject))

(println (.CFStringCreateWithCString foundation nil "Hello World" 0))

(def test-nsstring (.releaseOnFinalize (.CFStringCreateWithCString foundation nil "Hello World" 0)))
(def test-nsstring2 (.releaseOnFinalize (.CFStringCreateWithCString foundation nil "Hello World2" 0)))

; this test exercises the low level functions without macros
(deftest test-create-nsstring-subclass
  (let [mystring (new-objc-class (str (gensym)) NSString)
        hello-str "Hello World"]
    (add-method mystring "length" [:uint :id :sel] (fn [self sel] (count hello-str)))
    (add-method mystring "characterAtIndex:" [:unichar :id :sel :uint] (fn [self sel index] (nth hello-str index)))
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
          (method [:uint :length] [] (count hello-str))
          (method [:unichar :characterAtIndex :uint] [index] (nth hello-str index)))
        hello (alloc mystring)]
    (>> hello :init)
    (let [cai (>> hello :characterAtIndex 0)] (is (= cai 72)))
    (is (>> hello :isEqual test-nsstring))
    (.NSLog foundation hello)))

(deftest test-dynamic-send-msg
  (let [hello-str "Hello World"
        mystring
        (implementation (str (gensym)) NSString
          (method [:uint :length] [] (count hello-str))
          (method [:unichar :characterAtIndex :uint] [index] (nth hello-str index)))
        hello (alloc mystring)]
    (dynamic-send-msg hello "init")
    (let [cai (dynamic-send-msg hello "characterAtIndex:" 0)] (is (= cai 72)))
    (is (>> hello :isEqual test-nsstring))
    (.NSLog foundation hello)))

(deftest test-super-init
  (let [hello-str "Hello World"
        mystring
        (implementation (str (gensym)) NSString
          (method [:id :init] []
            (println "in init: " self)
            (let [_self (>>super self :init)]
              (println "self: " _self)
              _self))
          (method [:uint :length] [] (count hello-str))
          (method [:unichar :characterAtIndex :uint] [index] (nth hello-str index)))
        hello (alloc mystring)]
    (>> hello :init)
    (let [cai (>> hello :characterAtIndex 0)] (is (= cai 72)))
    (is (>> hello :isEqual test-nsstring))
    (.NSLog foundation hello)))

(deftest test-state
  (let [mystring
        (implementation (str (gensym)) NSString
          (method [:id :init] []
            (let [_self (>>super self :init)]
              (init _self {:value "Hello World"})
              (println "self: " _self)
              _self))
          (method [:uint :length] [] (count (:value (properties self))))
          (method [:unichar :characterAtIndex :uint] [index] (nth (:value (properties self)) index)))
        hello (alloc mystring)]
    (>> hello :init)
    (let [cai (>> hello :characterAtIndex 0)] (is (= cai 72)))
    (is (>> hello :isEqual test-nsstring))
    (.NSLog foundation hello)))

(deftest test-property-accessors
  (let [test-class
        (implementation (str (gensym)) NSObject
          (property :testString :atom)
          (method [:id :init] []
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
  ; need to sort out an autorelease pool to prevent a leak warning here
  (with-autorelease-pool
    (let [utf8String (>> test-nsstring :UTF8String)]
      (is (= utf8String "Hello World")))))

(comment

  )

(comment
  ; this helped me have a look at how the macros were expanding
  (deftest test-macroexpand-implementation
    (let [hello-str "hello"
          m1 (macroexpand-1 '(implementation (str (gensym)) NSString
        (method [:id :init] []
          (let [_self (>>super self :init)]
            (println "self: " _self)
            _self))
        (method [:uint :length] [] (count hello-str))
        (method [:unichar :characterAtIndex :uint] [index] (nth hello-str index))))
          m2 (macroexpand m1)
          m3 (macroexpand m2)]
      (println m1)
      (println m2)
      (println m3)))
  )


(deftest test-thread-adapter
  (let [ThreadAdapter
        (implementation (str (gensym)) NSObject
          (method [:void :onMainThread] []
            (println "Hello World")))
        thread-adapter (alloc ThreadAdapter)]
    (>> thread-adapter :init)
    (>> thread-adapter :performSelectorOnMainThread (selector :onMainThread) :withObject nil :waitUntilDone true)))

(defmethod report :begin-test-var [m] (println "beginning test " (:var m)))
(defmethod report :end-test-var [m] (System/gc) (println "ending test " (:var m)))

(run-tests)
(System/gc)
