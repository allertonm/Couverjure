(ns couverjure.tools.test
  (:use clojure.test couverjure.tools.java-model))

(defn constructor [modifiers name params body] (method-decl modifiers nil name params body))
(defn call-super-ctor [params] (call-method nil "super" params))

(defn test-structure [name var-decls]
  (class-decl [public] name nil "Structure" [
    (line-comment "Structure fields")
    (for [v var-decls] (field-decl [public] v))
    (break)
    (line-comment "Constructors")
    (constructor [public] name nil [
      (statement (call-super-ctor nil))
      ])
    (constructor [public] name var-decls [
      (for [v var-decls]
        (statement (assignment (var-ref "this" (:name v)) (var-ref nil (:name v)))))
      ])
    (constructor [public] name [(var-decl (type-spec name) "from")] [
      (for [v var-decls]
        (statement (assignment (var-ref "this" (:name v)) (var-ref "from" (:name v)))))
      ])
    (break)
    (line-comment "Value and Reference override classes")
    (class-decl [public static] "ByVal" "Structure.ByVal" name [
      (constructor [public] "ByVal" nil [
        (statement (call-super-ctor nil))
        ])
      (constructor [public] "ByVal" var-decls [
        (statement
          (call-super-ctor
            (for [v var-decls] (var-ref nil (:name v)))))
        ])
      ])
    ]))

(defn test-structure-file [name var-decls]
  [(package-decl "org.couverjure.cocoa")
   (break)
   (import-decl "com.sun.jna" "*")
   (break)
   (test-structure name var-decls)
   (break)
   (interface-decl [public] "MyLibrary" "Library" [
     (method-decl [public]
       (type-spec "int") "NSThing"
       [(var-decl (type-spec "String") "field")
        (var-decl (type-spec "int") "x")]
       nil)
     ])])

(deftest test-build-model
  (let [vars [(var-decl (type-spec "String") "field")
              (var-decl (type-spec "int") "x")]]
    (println (test-structure "Test" vars))))

; we're expecting the following code to be generated
(def expected-output "package org.couverjure.cocoa;

import com.sun.jna.*;

public class Test extends Structure {
    // Structure fields
    public String field;
    public int x;

    // Constructors
    public Test() {
        super();
    }
    public Test(String field, int x) {
        this.field = field;
        this.x = x;
    }
    public Test(Test from) {
        this.field = from.field;
        this.x = from.x;
    }

    // Value and Reference override classes
    public static class ByVal implements Structure.ByVal extends Test {
        public ByVal() {
            super();
        }
        public ByVal(String field, int x) {
            super(field, x);
        }
    }
}

public interface MyLibrary extends Library {
    public int NSThing(String field, int x);
}
")

(deftest test-model-to-source
  (let [vars [(var-decl (type-spec "String") "field")
              (var-decl (type-spec "int") "x")]
        model (test-structure-file "Test" vars)
        output (java.io.StringWriter.)]
    (do
      (format-java-model output model)
      (is (= (.toString output) expected-output)))))

(run-tests)