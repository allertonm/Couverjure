(ns clojure.contrib.test-contrib.with-ns-test
  (:use clojure.test
	clojure.contrib.with-ns
        [clojure.contrib.seq-utils :only (includes?)]))

(deftest test-namespace-gets-removed
  (let [all-ns-names (fn [] (map #(.name %) (all-ns)))]
    (testing "unexceptional return"
      (let [ns-name (with-temp-ns (ns-name *ns*))]
        (is (not (includes? (all-ns-names) ns-name)))))
    (testing "when an exception is thrown"
      (let [ns-name-str
            (try
             (with-temp-ns
               (throw (RuntimeException. (str (ns-name *ns*)))))
             (catch clojure.lang.Compiler$CompilerException e
               (-> e .getCause .getMessage)))]
        (is (re-find #"^sym.*$" ns-name-str))
        (is (not (includes? (all-ns-names) (symbol ns-name-str))))))))
