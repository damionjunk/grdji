(ns grdji.test.specs
  (:require [clojure.test :refer :all]
            [grdji.specs :refer :all]
            [grdji.parse :refer [parsed-line->map]]
            [clojure.spec.alpha :as sp])
  (:import (java.time LocalDate)))

(deftest test-data-spec
  (testing "data.spec encoders"
    (is (= "1999-01-01" (datetime-encoder nil (LocalDate/parse "1999-01-01")))))

  (testing "data.spec decoders"
    (is (= (LocalDate/parse "1999-01-01") (datetime-decoder nil "1999-01-01")))
    (is (nil? (datetime-decoder nil "1999-01-1")))
    (is (nil? (datetime-decoder nil "abcd")))))

(deftest test-specs
  (testing "dob-spec"
    (is (sp/valid? :grdji.specs/dob   (LocalDate/parse "1999-01-01")))
    (is (not (sp/valid? :grdji.specs/email "foo.com")))
    (is (not (sp/valid? :grdji.specs/email "foo.com@bee")))
    (is (not (sp/valid? :grdji.specs/email "foo.b")))
    (is (not (sp/valid? :grdji.specs/email "foo@.b")))
    (is (not (sp/valid? :grdji.specs/email "foo@bar")))     ; this one is probably not valid IRL
    (is (sp/valid? :grdji.specs/email "foo@bar.com"))
    (is (sp/valid? :grdji.specs/email "foo@bar.baz.com"))
    (is (sp/valid? :grdji.specs/email "foo@bar.baz.foo.com"))))

(deftest mapping
  (testing "validated records"
    (is (= ::sp/invalid
           (-> '("last" "first" "email" "color" "1999-01-01")
               parsed-line->map
               validate-record)))
    (is (= ::sp/invalid
           (-> '("last" "first" "email" "color" "199a-01-01")
               parsed-line->map
               validate-record)))
    (is (= {:lastname      "last" :firstname "first" :email "email@foo.com",
            :favoritecolor "color" :dateofbirth #time/date"1999-01-01"}
           (-> '("last" "first" "email@foo.com" "color" "1999-01-01")
               parsed-line->map
               validate-record)))))