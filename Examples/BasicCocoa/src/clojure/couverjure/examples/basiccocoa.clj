(ns couverjure.examples.basiccocoa
  (:use couverjure.core couverjure.appkit couverjure.webkit))

(def main-window (atom nil))
(def main-web-view (atom nil))

(def SimpleAppDelegate
  (doto (new-objc-class "SimpleAppDelegate" (objc-class :NSObject))
    (defm :void [:applicationDidFinishLaunching :id] [self sel notification]
      (println "App Did Finish Launching"))
    (defm :void [:setWindow :id] [self sel window]
      (reset! main-window (retain window)))
    (defm :void [:setWebView :id] [self sel web-view]
      (reset! main-web-view (retain web-view)))
    (defm :void [:backForward :id] [self sel segmented-control]
      ; TODO: we can't write this method properly because we can't get the result from [segmented-control :selectedSegment]
      (println "backForward:"))
    (defm :void [:address :id] [self sel text-field]
      (... (deref main-web-view) :takeStringURLFrom text-field))
    (register-objc-class)))

(def NSThread (objc-class :NSThread))

(let [ThreadAdapter
      (doto (new-objc-class (str (gensym)) (objc-class :NSObject))
        (defm :int [:onMainThread] [self sel] 
          (ns-application-main))
        (register-objc-class))
      thread-adapter (alloc ThreadAdapter)]
    (... thread-adapter :init)
    (... thread-adapter :performSelectorOnMainThread (selector :onMainThread) :withObject nil :waitUntilDone true))
;(ns-application-main)