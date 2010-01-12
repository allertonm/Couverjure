(ns couverjure.examples.basiccocoa
  (:use couverjure.core couverjure.appkit couverjure.webkit))

(def main-window (atom nil))
(def main-web-view (atom nil))

(defimplementation SimpleAppDelegate (objc-class :NSObject)
    (method [:void :applicationDidFinishLaunching :id] [notification]
      (println "App Did Finish Launching"))
    (method [:void :setWindow :id] [window]
      (reset! main-window (retain window)))
    (method [:void :setWebView :id] [web-view]
      (reset! main-web-view (retain web-view)))
    (method [:void :backForward :id] [segmented-control]
      ; TODO: we can't write this method properly because we can't get the result from [segmented-control :selectedSegment]
      (println "backForward:"))
    (method [:void :address :id] [text-field]
      (... (deref main-web-view) :takeStringURLFrom text-field)))

(def NSThread (objc-class :NSThread))

(let [ThreadAdapter
      (implementation (str (gensym)) (objc-class :NSObject)
        (method [:void :onMainThread] []
          (ns-application-main)))
      thread-adapter (alloc ThreadAdapter)]
    (... thread-adapter :init)
    (... thread-adapter :performSelectorOnMainThread (selector :onMainThread) :withObject nil :waitUntilDone true))
;(ns-application-main)