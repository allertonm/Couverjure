;   Copyright (c) Chris Houser, Dec 2008. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php)
;   which can be found in the file CPL.TXT at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

; Utilities meant to be used interactively at the REPL

(ns 
  #^{:author "Chris Houser, Christophe Grand, Stephen Gilardi",
     :doc "Utilities meant to be used interactively at the REPL"}
  clojure.contrib.repl-utils
  (:import (java.io File LineNumberReader InputStreamReader PushbackReader)
           (java.lang.reflect Modifier Method Constructor)
           (clojure.lang RT Compiler Compiler$C))
  (:use [clojure.contrib.seq-utils :only (indexed)]
        [clojure.contrib.javadoc.browse :only (browse-url)]
        [clojure.contrib.str-utils :only (str-join re-sub re-partition)]))

;; ----------------------------------------------------------------------
;; Examine Java classes

(defn- sortable [t]
  (apply str (map (fn [[a b]] (str a (format "%04d" (Integer. b))))
                  (partition 2 (concat (re-partition #"\d+" t) [0])))))

(defn- param-str [m]
  (str " (" (str-join
              "," (map (fn [[c i]]
                         (if (> i 3)
                           (str (.getSimpleName c) "*" i)
                           (str-join "," (replicate i (.getSimpleName c)))))
                       (reduce (fn [pairs y] (let [[x i] (peek pairs)]
                                               (if (= x y)
                                                 (conj (pop pairs) [y (inc i)])
                                                 (conj pairs [y 1]))))
                               [] (.getParameterTypes m))))
  ")"))

(defn- member-details [m]
  (let [static? (Modifier/isStatic (.getModifiers m))
        method? (instance? Method m)
        ctor?   (instance? Constructor m)
        text (if ctor?
               (str "<init>" (param-str m))
               (str
                 (when static? "static ")
                 (.getName m) " : "
                 (if method?
                   (str (.getSimpleName (.getReturnType m)) (param-str m))
                   (str (.getSimpleName (.getType m))))))]
    (assoc (bean m)
           :sort-val [(not static?) method? (sortable text)]
           :text text
           :member m)))

(defn show
  "With one arg prints all static and instance members of x or (class x).
  Each member is listed with a number which can be given as 'selector'
  to return the member object -- the REPL will print more details for
  that member.

  The selector also may be a string or regex, in which case only
  members whose names match 'selector' as a case-insensitive regex
  will be printed.

  Finally, the selector also may be a predicate, in which case only
  members for which the predicate returns true will be printed.  The
  predicate will be passed a single argument, a map that includes the
  :text that will be printed and the :member object itself, as well as
  all the properies of the member object as translated by 'bean'.

  Examples: (show Integer)  (show [])  (show String 23)  (show String \"case\")"
  ([x] (show x (constantly true)))
  ([x selector]
      (let [c (if (class? x) x (class x))
            members (sort-by :sort-val
                             (map member-details
                                  (concat (.getFields c)
                                          (.getMethods c)
                                          (.getConstructors c))))]
        (if (number? selector)
          (:member (nth members selector))
          (let [pred (if (ifn? selector)
                       selector
                       #(re-find (re-pattern (str "(?i)" selector)) (:name %)))]
            (println "=== " (Modifier/toString (.getModifiers c)) c " ===")
            (doseq [[i m] (indexed members)]
              (when (pred m)
                (printf "[%2d] %s\n" i (:text m)))))))))

;; ----------------------------------------------------------------------
;; Examine Clojure functions (Vars, really)

(defn get-source
  "Returns a string of the source code for the given symbol, if it can
  find it.  This requires that the symbol resolve to a Var defined in
  a namespace for which the .clj is in the classpath.  Returns nil if
  it can't find the source.  For most REPL usage, 'source' is more
  convenient.
  
  Example: (get-source 'filter)"
  [x]
  (when-let [v (resolve x)]
    (when-let [filepath (:file (meta v))]
      (when-let [strm (.getResourceAsStream (RT/baseLoader) filepath)]
        (with-open [rdr (LineNumberReader. (InputStreamReader. strm))]
          (dotimes [_ (dec (:line (meta v)))] (.readLine rdr))
          (let [text (StringBuilder.)
                pbr (proxy [PushbackReader] [rdr]
                      (read [] (let [i (proxy-super read)]
                                 (.append text (char i))
                                 i)))]
            (read (PushbackReader. pbr))
            (str text)))))))

(defmacro source
  "Prints the source code for the given symbol, if it can find it.
  This requires that the symbol resolve to a Var defined in a
  namespace for which the .clj is in the classpath.
  
  Example: (source filter)"
  [n]
  `(println (or (get-source '~n) (str "Source not found"))))

;; ----------------------------------------------------------------------
;; Handle Ctrl-C keystrokes

(def #^{:doc "Threads to stop when Ctrl-C is pressed.  See 'add-break-thread!'"}
  break-threads (atom {}))

(let [first-time (atom true)]
  (defn start-handling-break
    "Register INT signal handler.  After calling this, Ctrl-C will cause
    all break-threads to be stopped.  See 'add-break-thread!'"
    []
    (when (= :need-init
             (swap! first-time
                    {:need-init false, false false, true :need-init}))
      (sun.misc.Signal/handle
        (sun.misc.Signal. "INT")
        (proxy [sun.misc.SignalHandler] []
          (handle [sig]
            (let [exc (Exception. (str sig))]
              (doseq [tref (vals @break-threads) :when (.get tref)]
                (.stop (.get tref) exc)))))))))

(defn add-break-thread!
  "Add the given thread to break-threads so that it will be stopped
  any time the user presses Ctrl-C.  Calls start-handling-break for
  you.  Adds the current thread if none is given."
  ([] (add-break-thread! (Thread/currentThread)))
  ([t]
    (start-handling-break)
    (let [tref (java.lang.ref.WeakReference. t)]
      (swap! break-threads assoc (.getId t) tref))))

;; ----------------------------------------------------------------------
;; Compiler hooks

(defn expression-info
  "Uses the Clojure compiler to analyze the given s-expr.  Returns
  a map with keys :class and :primitive? indicating what the compiler
  concluded about the return value of the expression.  Returns nil if
  not type info can be determined at compile-time.
  
  Example: (expression-info '(+ (int 5) (float 10)))
  Returns: {:class float, :primitive? true}"
  [expr]
  (let [fn-ast (Compiler/analyze Compiler$C/EXPRESSION `(fn [] ~expr))
        expr-ast (.body (first (.methods fn-ast)))]
    (when (.hasJavaClass expr-ast)
      {:class (.getJavaClass expr-ast)
       :primitive? (.isPrimitive (.getJavaClass expr-ast))})))

;; ----------------------------------------------------------------------
;; scgilardi at gmail

(defn run*
  "Loads the specified namespace and invokes its \"main\" function with
  optional args."
  [ns-sym & args]
  (require ns-sym :reload-all)
  (apply (ns-resolve ns-sym 'main) args))

(defmacro run
  "Loads the specified namespace and invokes its \"main\" function with
  optional args. ns-name is not evaluated."
  [ns-name & args]
  `(run* '~ns-name ~@args))


(load "repl_utils/javadoc")
