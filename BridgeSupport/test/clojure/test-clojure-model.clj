(ns couverjure.tools.test
  (:use clojure.test couverjure.tools.clojure-model))

(def expected-output
"; This is a comment
; split over two lines
(defn nsfastenumerationstate
  ([state itemsPtr mutationsPtr extra]
    (new _NSFastEnumerationState$ByVal state itemsPtr mutationsPtr extra))
  ([from]
    (new _NSFastEnumerationState$ByVal from)))")

(def test-model
  (list :no-brackets
    (list :comment "This is a comment" "split over two lines")
    (list (symbol "defn") (symbol "nsfastenumerationstate") :break :indent
      (list [(symbol "state") (symbol "itemsPtr") (symbol "mutationsPtr") (symbol "extra")] :break :indent
        (list (symbol "new") (symbol "_NSFastEnumerationState$ByVal") (symbol "state") (symbol "itemsPtr") (symbol "mutationsPtr") (symbol "extra"))) :unindent :break
      (list [(symbol "from")] :break :indent
        (list (symbol "new") (symbol "_NSFastEnumerationState$ByVal") (symbol "from"))))))

(deftest test-formatter
  (let [writer (format-clojure-source (java.io.StringWriter.) (emit-clojure test-model))]
    (print (.toString writer))
    (is (= expected-output (.toString writer)))))

(run-tests)


