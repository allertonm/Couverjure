(ns couverjure.tools.formatter
  (:use clojure.contrib.seq-utils))    

(defn format-source
  "Generate formatted source code from the results of emit-java or emit-clojure.
 This uses the formatting-instruction keywords in the tree to control
 line-breaks and tabs, but otherwise just writes all of the strings
 in the tree to the output (assumed to be a java.io.Writer), separated by spaces
 unless overridden by the no-space-after? or no-space-before? fns, or the :no-space
 keyword in the tree.
 nils in the tree are ignored (same as for 'str')"
  [writer emitted-tree tab-size no-space-after? no-space-before?]
  (loop [emitted (filter (complement nil?) (flatten emitted-tree))
         at-break true
         no-space false
         level 0]
    (let [s (first emitted)
          rest (rest emitted)]
       (cond
         ; nil means we're done
         (nil? s)
         nil
         ; line-break
         (= s :break)
         (do
           (.write writer "\n")
           (recur rest true false level))
         ; indent
         (= s :indent)
         (recur rest at-break no-space (inc level))
         ; unindent
         (= s :unindent)
         (recur rest at-break no-space (dec level))
         (= s :no-space)
         (recur rest at-break true level)
         ; empty strings are ignored
         (empty? s)
         (recur rest at-break no-space level)
         ; everything else should be strings
         (string? s)
         (do
           (if at-break
              (.write writer (apply str (repeat (* level tab-size) \space))))
           (if (not (or at-break no-space (no-space-before? s)))
             (.write writer " "))
           (.write writer s)
           (if (no-space-after? s)
              (recur rest false true level)
              (recur rest false false level)))
    )))
  writer)


