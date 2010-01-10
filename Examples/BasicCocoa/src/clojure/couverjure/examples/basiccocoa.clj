(ns couverjure.examples.basiccocoa
  (:use couverjure.core couverjure.appkit))

(println *command-line-args*)

(def NSThread (objc-class :NSThread))
(println "isMainThread:" (... NSThread :isMainThread))
(println "mainThread:" (... NSThread :mainThread))
(println "currentThread:" (... NSThread :currentThread))
(println (. (Thread/currentThread) getName))

(.NSLog foundation (unwrap-id (wrap-id (.CFStringCreateWithCString foundation nil "Hello World" 0))))

(let [ThreadAdapter
      (doto (new-objc-class (str (gensym)) (objc-class :NSObject))
        (defm :int [:onMainThread] [self sel] 
          (println "isMainThread2:" (... NSThread :isMainThread))
          (ns-application-main))
        (register-objc-class))
      thread-adapter (alloc ThreadAdapter)]
    (... thread-adapter :init)
    (... thread-adapter :performSelectorOnMainThread (selector :onMainThread) :withObject nil :waitUntilDone true))
;(ns-application-main)