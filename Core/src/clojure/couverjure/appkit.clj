(ns couverjure.appkit
  (:use couverjure.core)
  (:import
    (com.sun.jna Native)
    (org.couverjure.core AppKit)))

(println "Loading Couverjure AppKit")

(def appkit (Native/loadLibrary "AppKit" AppKit))

; Cocoa application startup
(defn ns-application-main []
  ;(.NSApplicationMain appkit (count *command-line-args*) (into-array *command-line-args*)))
  (.NSApplicationMain appkit 0 nil))

