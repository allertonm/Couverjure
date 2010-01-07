(ns couverjure.test
  (:use couverjure.core clojure.test))

(deftest test-create-nsstring-subclass
  (let [mystring (new-objc-class :MyClass (objc-class :NSString))
        hello-str "Hello"]
    (add-method mystring :length "I@:" (fn [self sel] (.length hello-str)))
    (add-method mystring :characterAtIndex- "S@:I" (fn [self sel index] (.charAt hello-str index)))
    (register-objc-class mystring)
    (let
      [hello (alloc mystring)]
      (tell hello :init)
      (let [cai (tell hello :characterAtIndex- 0)] (.println System/out cai))
      (.NSLog foundation (unwrap-id hello)))))

(deftest test-create-nsstring-subclass-with-doto
  (let [hello-str "Hello World"
        mystring
        (doto (new-objc-class :MyClass2 (objc-class :NSString))
          (add-method :length "I@:" (fn [self sel] (.length hello-str)))
          (add-method :characterAtIndex- "S@:I" (fn [self sel index] (.charAt hello-str index)))
          (register-objc-class))
        hello (alloc mystring)]
    (tell hello :init)
    (let [cai (tell hello :characterAtIndex- 0)] (.println System/out cai))
    (.NSLog foundation (unwrap-id hello))))

(deftest test-create-nsstring-subclass-with-tell-macro
  (let [mystring (new-objc-class :MyClass3 (objc-class :NSString))
        hello-str "Hello"]
    ;(add-method mystring :length "I@:" (fn [self sel] (.length hello-str)))
    ;(add-method mystring :characterAtIndex- "S@:I" (fn [self sel index] (.charAt hello-str index)))
    (defm mystring :uint [:length] [self sel] (count hello-str))
    (defm mystring :unichar [:characterAtIndex :uint] [self sel index] (.charAt hello-str index))
    (register-objc-class mystring)
    (let
      [hello (alloc mystring)]
      (... hello :init)
      (let [cai (... hello :characterAtIndex 0)] (.println System/out cai))
      (.NSLog foundation (unwrap-id hello)))))

(run-tests)
