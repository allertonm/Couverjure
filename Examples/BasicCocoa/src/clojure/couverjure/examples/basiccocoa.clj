(ns couverjure.examples.basiccocoa
  (:use couverjure.core couverjure.appkit))

(def SimpleAppDelegate
  (doto (new-objc-class "SimpleAppDelegate" (objc-class :NSObject))
    (defm :void [:applicationDidFinishLaunching :id] [self sel notification]
      (println "App Did Finish Launching"))
    (register-objc-class)))

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