(ns couverjure.test
  (:use couverjure.core clojure.test))

(def NSString (objc-class :NSString))
(def NSObject (objc-class :NSObject))

(def test-nsstring (wrap-id (.CFStringCreateWithCString foundation 0 "Hello World" 0)))
(def test-nsstring2 (wrap-id (.CFStringCreateWithCString foundation 0 "Hello World2" 0)))

; this test exercises the low level functions without macros
(deftest test-create-nsstring-subclass
  (let [mystring (new-objc-class (str (gensym)) NSString)
        hello-str "Hello World"]
    (add-method mystring :length [:uint :id :sel] (fn [self sel] (count hello-str)))
    (add-method mystring :characterAtIndex- [:unichar :id :sel :uint] (fn [self sel index] (nth hello-str index)))
    (register-objc-class mystring)
    (let
      [hello (alloc mystring)]
      (send-msg hello :init)
      (let [cai (send-msg hello :characterAtIndex- 0)] (is (= cai 72)))
      (is (>> hello :isEqual test-nsstring))
      (.NSLog foundation (unwrap-id hello)))))

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
    (.NSLog foundation (unwrap-id hello))))

(deftest test-dynamic-send-msg
  (let [hello-str "Hello World"
        mystring
        (implementation (str (gensym)) NSString
          (method [:uint :length] [] (count hello-str))
          (method [:unichar :characterAtIndex :uint] [index] (nth hello-str index)))
        hello (alloc mystring)]
    (dynamic-send-msg hello :init)
    (let [cai (dynamic-send-msg hello :characterAtIndex- 0)] (is (= cai 72)))
    (is (>> hello :isEqual test-nsstring))
    (.NSLog foundation (unwrap-id hello))))

(deftest test-super-init
  (let [hello-str "Hello World"
        mystring
        (implementation (str (gensym)) NSString
          (method [:id :init] []
            (let [_self (>>super self :init)]
              (println "self: " _self)
              _self))
          (method [:uint :length] [] (count hello-str))
          (method [:unichar :characterAtIndex :uint] [index] (nth hello-str index)))
        hello (alloc mystring)]
    (>> hello :init)
    (let [cai (>> hello :characterAtIndex 0)] (is (= cai 72)))
    (is (>> hello :isEqual test-nsstring))
    (.NSLog foundation (unwrap-id hello))))

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
    (.NSLog foundation (unwrap-id hello))))

(deftest test-property-accessors
  (let [test-class
        (implementation (str (gensym)) NSObject
          (property :testString :atom)
          (method [:id :init] []
            (let [_self (>>super self :init)]
              (init _self {:testString (atom test-nsstring) })
              _self)))
        test (alloc test-class)]
    (>> test :init)
    (let [read-property (>> test :testString)]
      (is (>> read-property :isEqual test-nsstring )))
    (is (do (>> test :setTestString test-nsstring2) true) "Set accessor ran without exceptions")
    (let [read-property (>> test :testString)]
      (is (>> read-property :isEqual test-nsstring2 )))
    ))

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


(deftest test-thread-adapter
  (let [ThreadAdapter
        (implementation (str (gensym)) NSObject
          (method [:void :onMainThread] []
            (println "Hello World")))
        thread-adapter (alloc ThreadAdapter)]
    (>> thread-adapter :init)
    (>> thread-adapter :performSelectorOnMainThread (selector :onMainThread) :withObject nil :waitUntilDone true)))

(defmethod report :begin-test-var [m] (println "beginning test " (:var m)))
; (defmethod report :end-test-var [m] (println "ending test " (:var m)))

(run-tests )
