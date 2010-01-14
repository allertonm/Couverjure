(ns couverjure.webkit
  (:import
    (com.sun.jna Native Library)))

(println "Loading Couverjure WebKit")

(def webkit (Native/loadLibrary "WebKit" Library))
