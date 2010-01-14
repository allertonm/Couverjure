(ns couverjure.test
  (:use couverjure.core clojure.test))

; this test exercises the low level functions without macros
(deftest test-create-nsstring-subclass
  (let [mystring (new-objc-class (str (gensym)) (objc-class :NSString))
        hello-str "Hello"]
    (add-method mystring :length [:uint :id :sel] (fn [self sel] (count hello-str)))
    (add-method mystring :characterAtIndex- [:unichar :id :sel :uint] (fn [self sel index] (nth hello-str index)))
    (register-objc-class mystring)
    (let
      [hello (alloc mystring)]
      (tell hello :init)
      (let [cai (tell hello :characterAtIndex- 0)] (.println System/out cai))
      (.NSLog foundation (unwrap-id hello)))))

(deftest test-new-implementation-macro
  (let [hello-str "Hello World"
        mystring
        (implementation (str (gensym)) (objc-class :NSString)
          (method [:uint :length] [] (count hello-str))
          (method [:unichar :characterAtIndex :uint] [index] (nth hello-str index)))
        hello (alloc mystring)]
    (... hello :init)
    (let [cai (... hello :characterAtIndex 0)] (.println System/out cai))
    (.NSLog foundation (unwrap-id hello))))

(run-tests)
