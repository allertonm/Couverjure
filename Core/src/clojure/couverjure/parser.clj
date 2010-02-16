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

; This is basically a simple Parser Combinator library
; What we mean by this is that parsers are a family of functions that take a sequence
; (normally but not limited to a sequence of chars and return a result
;   [ token(s) rest-of-seq ] if the parser matched the start of the sequence, or
;   nil if they did not
; These can be combined together to create parser functions that match more complex structures
; - the library provides functions which combine parsers into series (repeats), patterns (sequences)
; and options - and the results of these can themselves be combined.

(defn char-set
  "Returns a set including all of the characters in the range"
  [first last]
  (set (map char (range (int first) (inc (int last))))))

(defn in-set
  "Returns a parser that matches a character from the given set"
  ([char-set transform]
    (fn [in]
      (if (char-set (first in)) [(transform (first in)) (rest in)] nil)))
  ([char-set]
    (in-set char-set identity)))

(defn single-char
  "Returns a parser that matches the given character"
  ([char transform]
    (fn [in]
      (if (= char (first in)) [(transform char) (rest in)] nil)))
  ([char]
    (single-char char identity)))

(declare pattern)

(defn to-parser
  "Takes a term, which may be one of a) a character, b) a string or c) a parser, and returns a parser.
  In cases a & b, the returned parser will match the given character or string. In case c we return
  the supplied parser unchanged. This function is used to massage the arguments to choice, repeat and
  series so that code using them can be more readable."
  [term]
  (condp instance? term
    Character (single-char term)
    String (pattern (map single-char term))
    term))

(defn choice
  "Returns a parser which will attempt to match one of the supplied terms"
  ([terms transform]
    (let [_ps (map to-parser terms)]
      (fn [in]
        (loop [ps _ps]
          (let [p (first ps)]
            (if p
              (let [[token rem] (p in)]
                (if token
                  [ (transform token) rem ]
                  (recur (rest ps))))
              ))))))
  ([terms]
    (choice terms identity)))

(defn series
  "Returns a parser which will attempt to match one or more of the supplied term"
  ([term transform]
    (let [p (to-parser term)]
      (fn [_in]
        (loop [in _in, tokens nil]
          (let [[token rem] (p in)]
            (if token
              (recur rem (conj (or tokens []) token))
              (if tokens
                [ (transform tokens) in ])
              ))))))
  ([term]
    (series term identity)))

(defn pattern
  "Returns a parser which will match each of the supplied terms in sequence."
  ([terms transform]
    (let [_ps (map to-parser terms)]
      (fn [_in]
        (loop [in _in, tokens nil, ps _ps]
          (let [p (first ps)]
            (if p
              (let [[token rem] (p in)]
                (if token
                  (recur rem (conj (or tokens []) token) (rest ps))))
              (if tokens [(transform tokens), in]))))
        )))
  ([terms]
    (pattern terms identity)))

(defn option
  "Returns a parser that will optionally match the supplied term (i.e it will
  always 'succeed' at parsing, but will return the token :nothing if the term is not matched"
  ([term transform]
    (let [p (to-parser term)]
      (fn [in]
        (or (p in) [:nothing in]))))
  ([term]
    (option term identity)))

; helper for dealing with option results

(defn option?
  "Returns true if the value is not equal to the :nothing keyword produced by an unmatched 'option'
  - so can be used in transform and post-transform code to check whether an option matched"
  [x]
  (not (= :nothing x)))
