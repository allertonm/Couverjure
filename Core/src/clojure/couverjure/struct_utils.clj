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

(ns couverjure.struct-utils)

(defmacro deftagged
  "A helper macro for creating tagged structs and their constructor functions
Creates an anonymous struct with a key :tag in addition to the supplied keys,
and a constructor function with the supplied name which constructs the struct
using the supplied arguments, setting :tag to :<name>"
  [name & keys]
  (let [keys-as-symbols (for [k keys] (symbol (subs (str k) 1)))
        struct-name (gensym)]
    `(do
      (defstruct ~struct-name :tag ~@keys)
      (defn ~name [~@keys-as-symbols] (struct ~struct-name ~(keyword (str name)) ~@keys-as-symbols)))))


