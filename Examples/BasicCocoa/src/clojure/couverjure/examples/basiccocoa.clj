(ns couverjure.examples.basiccocoa
  (:use couverjure.core couverjure.appkit couverjure.webkit))

(def main-window (atom nil))
(def main-web-view (atom nil))

(defimplementation SimpleAppDelegate (objc-class :NSObject)
  (property :window :atom)
  (property :webView :atom)
  (method [:id :init] []
    (let [_self (>>super self :init)]
      (init _self {:window (atom nil) :webView (atom nil)})
      _self))
  (method [:void :applicationDidFinishLaunching :id] [notification]
    (println "App Did Finish Launching"))
  (method [:void :backForward :id] [segmented-control]
    (println "backForward:")
    (let [web-view (deref ((properties self) :webView))]
      (condp = (>> segmented-control :selectedSegment)
        0 (>> web-view :goBack)
        1 (>> web-view :goForward))))
  (method [:void :address :id] [text-field]
    (>> (deref ((properties self) :webView)) :takeStringURLFrom text-field)))

(def NSThread (objc-class :NSThread))

(let [ThreadAdapter
      (implementation (str (gensym)) (objc-class :NSObject)
        (method [:void :onMainThread] []
          (ns-application-main)))
      thread-adapter (alloc ThreadAdapter)]
  (>> thread-adapter :init)
  (>> thread-adapter :performSelectorOnMainThread (selector :onMainThread) :withObject nil :waitUntilDone true))
;(ns-application-main)