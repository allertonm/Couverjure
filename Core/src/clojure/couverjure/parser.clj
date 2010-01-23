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

(ns couverjure.parser)

;
; This module is intended to provide just enough parser to handle Couverjure's requirements for
; ObjC method type encoding parsing, without introducing extra dependencies
;

(defn in-set [chars]
  "Returns a parser that matches a character from the given set"
  (fn [in]
    (if (chars (first in)) [(first in) (rest in)] [nil in])))

(defn single-char [char]
  "Returns a parser that matches the given character"
  (fn [in]
    (if (= char (first in)) [char (rest in)] [nil in])))

(declare pattern)

(defn to-parser [term]
  "Takes a term, which may be one of a) a character, b) a string or c) a parser, and returns a parser.
  In cases a & b, the returned parser will match the given character or string. In case c we return
  the supplied parser unchanged. This function is used to massage the arguments to choice, repeat and
  series so that code using them can be more readable."
  (condp instance? term
    Character (single-char term)
    String (apply pattern (map single-char term))
    term))

(defn choice [& terms]
  "Returns a parser which will attempt to match one of the supplied terms"
  (let [_ps (map to-parser terms)]
    (fn [in]
      (loop [ps _ps]
        (let [p (first ps)]
          (if p
            (let [[token rem] (p in)]
              (if token
                [token, rem]
                (recur (rest ps))))
            [nil, in]))))))

(defn series [term]
  "Returns a parser which will attempt to match one or more of the supplied term"
  (let [p (to-parser term)]
    (fn [_in]
      (loop [in _in, tokens nil]
        (let [[token rest] (p in)]
          (if token
            (recur rest (conj (if (nil? tokens) [] tokens) token))
            [tokens rest]))))))

(defn pattern [& terms]
  "Returns a parser which will match each of the supplied terms in sequence."
  (let [_ps (map to-parser terms)]
    (fn [_in]
      (loop [in _in, tokens nil, ps _ps]
        (let [p (first ps)]
          (if p
            (let [[token rem] (p in)]
              (if token
                (recur rem (conj (if (nil? tokens) [] tokens) token) (rest ps))
                [nil, _in]))
            [tokens, in]))))))

(defn option [term]
  "Returns a parser that will optionally match the supplied term (i.e it will
  always 'succeed' at parsing, but will not return any tokens if the term is not matched"
  (fn [in]
    (let [result (term in)
          [tokens _] result]
      (if tokens
        result
        [[] in]))))
