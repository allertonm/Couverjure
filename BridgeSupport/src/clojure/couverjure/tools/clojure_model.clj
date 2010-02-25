;    Copyright 2010 Mark Allerton. All rights reserved.
;
;    Redistribution and use in source and binary forms, with or without modification, are
;    permitted provided that the following conditions are met:
;
;       1. Redistributions of source code must retain the above copyright notice, this list of
;          conditions and the following disclaimer.
;
;       2. Redistributions in binary form must reproduce the above copyright notice, this list
;          of conditions and the following disclaimer in the documentation and/or other materials
;          provided with the distribution.
;
;    THIS SOFTWARE IS PROVIDED BY MARK ALLERTON ``AS IS'' AND ANY EXPRESS OR IMPLIED
;    WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
;    FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
;    CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
;    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
;    SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
;    ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
;    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
;    ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
;
;    The views and conclusions contained in the software and documentation are those of the
;    authors and should not be interpreted as representing official policies, either expressed
;    or implied, of Mark Allerton.

(ns couverjure.tools.clojure-model
  (:use
    couverjure.tools.formatter))

(defn no-brackets [seq]
  (conj (apply list seq) :no-brackets))

; this is a model of economy when compared to the java version :)
;
(defn emit-clojure [m]
  (cond
    ; lists - normally emitted as a bracketed list but there are some directives
    ; that can be in first position.
    (list? m)
    (cond
      ; make the first element of a list :no-brackets to suppress brackets
      ; good for the file level of source
      (= :no-brackets (first m))
      (map emit-clojure (rest m))
      ; :comment as first element of a list makes the rest of the list into a comment
      ; (each list element is a new comment line)
      (= :comment (first m))
      (for [mi (rest m)] [ ";" (str mi) :break])
      ; :source as first element writes the rest of the elements directly to the output
      (= :source (first m))
      (apply str (rest m))
      ; everything else
      true
      ["(" (map emit-clojure m) ")"])
    ; vectors
    (vector? m)
    ["[" (map emit-clojure m) "]"]
    ; maps
    (map? m)
    ["{" (for [mi m] [(emit-clojure (first mi)) (emit-clojure (second mi))]) "}"]
    ; strings
    (string? m)
    [ (str "\"" m "\"") ]
    ; formatting instructions
    (#{:break :indent :unindent} m)
    m
    ; default
    true
    (str m)
    ))

(defn format-clojure-source
  [writer emitted-tree]
  (format-source writer emitted-tree 2 #{ "(" "[" "{" } #{ ")" "]" "}" }))



