(ns couverjure.type-encoding
  (:use couverjure.parser))

;
; This is the parser grammar for Objective-C method type encodings
;

(def alpha (in-set (set (map char (concat (range (int \a) (inc (int \z))) (range (int \A) (inc (int \Z))))))))
(def digit (in-set (set (map char (range (int \0) (inc (int \9)))))))

(def primitive (in-set #{ \c \i \s \l \q \C \I \S \L \Q \f \d \B \v \* \@ \# \: \? }))
(def qualifier (in-set #{ \r \n \N \o \O \R \V }))

(def number (series digit))
(def type-name (choice (series (choice alpha digit \_)) \?))

(def array-count number)
(def offset number)

(defn type-encoding [in]
  ((choice
    primitive
    (pattern qualifier type-encoding)
    (pattern \[ array-count type-encoding \])
    (pattern \{ type-name \= (option (series type-encoding)) \})
    (pattern \( type-name \= (option (series type-encoding)) \))
    (pattern \b number)
    (pattern \^ type-encoding)) in))

(def method-argument-encoding (pattern type-encoding (option offset)))

(def method-signature-encoding (series method-argument-encoding))
