(ns couverjure.webkit
  (:import
    (com.sun.jna Native)
    (org.couverjure.jna WebKit)))

(println "Loading Couverjure WebKit")

(def webkit (Native/loadLibrary "WebKit" WebKit))
