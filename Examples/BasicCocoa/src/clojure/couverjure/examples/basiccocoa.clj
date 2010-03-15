(ns couverjure.examples.basiccocoa
  (:use
    couverjure.types
    couverjure.core)
  (:require
    [couverjure.cocoa.foundation :as fnd]
    [couverjure.cocoa.appkit :as app]
    [couverjure.cocoa.webkit :as web]))

; The class "SimpleAppDelegate" is named as the "File's Owner" in our default NIB
; - so will be automatically loaded after NSApplicationMain is invoked.
(defimplementation SimpleAppDelegate fnd/NSObject
  ; define accessors for window and webView - both of which have been named
  ; as outlets on SimpleAppDelegate in the NIB
  (property :window :atom)
  (property :webView :atom)
  ; implement init to set up our state (must currently do this if we define properties.)
  (method [OCID :init] []
    (let [_self (>>super self :init)]
      (init _self {:window (atom nil) :webView (atom nil)})
      _self))
  ; SimpleAppDelegate is wired as the application delegate in the NIB, so this will be invoked after load
  (method [OCVoid :applicationDidFinishLaunching OCID] [notification]
    (println "App Did Finish Launching"))
  ; backForward: is an action wired to the segmented control we're using for back and forward buttons
  (method [OCVoid :backForward OCID] [segmented-control]
    ; the explicit deref of properties is pretty ugly here - it's much less code to invoke
    ; the accessor (but currently much slower!)
    (let [web-view (deref ((properties self) :webView))]
      (condp = (>> segmented-control :selectedSegment)
        0 (>> web-view :goBack)
        1 (>> web-view :goForward))))
  ; handle new values in the address box
  (method [OCVoid :address OCID] [text-field]
    (>> (deref ((properties self) :webView)) :takeStringURLFrom text-field)))

; This is a hack to get around the fact that either JavaApplicationStub or clojure (most likely the former)
; does not run this script on Cocoa's "main thread" - and we need our invocation os NSApplicationMain to
; happen on the main thread.
; We acheive this by creating an instance of an anonymous (objc) class and invoking it using performSelectorOnMainThread
(let [ThreadAdapter
      (implementation (str (gensym)) fnd/NSObject
        (method [OCVoid :onMainThread] []
          (app/NSApplicationMain 0 nil)))
      thread-adapter (alloc ThreadAdapter)]
  (>> thread-adapter :init)
  (>> thread-adapter :performSelectorOnMainThread (selector :onMainThread) :withObject nil :waitUntilDone true))
