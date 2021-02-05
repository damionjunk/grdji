(ns grdji.test.parse
  (:require [clojure.test :refer :all]
            [grdji.parse :refer :all]
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
    (is (sp/valid? :grdji.parse/dob   (LocalDate/parse "1999-01-01")))
    (is (not (sp/valid? :grdji.parse/email "foo.com")))
    (is (not (sp/valid? :grdji.parse/email "foo.com@bee")))
    (is (not (sp/valid? :grdji.parse/email "foo.b")))
    (is (not (sp/valid? :grdji.parse/email "foo@.b")))
    (is (not (sp/valid? :grdji.parse/email "foo@bar")))     ; this one is probably not valid IRL
    (is (sp/valid? :grdji.parse/email "foo@bar.com"))
    (is (sp/valid? :grdji.parse/email "foo@bar.baz.com"))
    (is (sp/valid? :grdji.parse/email "foo@bar.baz.foo.com"))))

(deftest delimiters
  (testing "detection"
    (is (not (has-comma? "")))
    (is (not (has-space? "")))
    (is (not (has-pipe? "")))
    (is (not (has-pipe? nil)))
    (is (has-comma? "foo,"))
    (is (has-comma? "foo ,"))
    (is (has-pipe? "foo |"))
    (is (has-pipe? "foo|,bar"))
    (is (has-space? "foo bar"))
    (is (has-space? "foo    bar")))

  (testing "classification"
    (is (= :pipe (classify "foo | bar | baz")))
    (is (= :pipe (classify "foo, | bar, | baz")))           ; Pipe detected first
    (is (= :comma (classify "foo , bar , baz")))
    (is (= :space (classify "foo     bar     baz")))
    (is (nil? (classify "            ")))
    (is (nil? (classify nil)))))

(deftest line-parsing
  (testing "parse-line"
    (is (= '("foo" "bar" "baz") (parse-line "foo,bar,baz")))
    (is (= '("foo" "bar" "baz") (parse-line "foo        ,bar ,    baz  ")))
    (is (= '("foo" "bar" "baz") (parse-line "foo | bar | baz  ")))
    (is (= '("foo" "bar" "baz") (parse-line "foo , bar , baz  ")))
    (is (thrown-with-msg? Exception #"No delimiter detected." (parse-line "")))))

(deftest mapping
  (testing "parsed-line->map"
    (is (= {:lastname    "foo" :firstname "bar"
            :email       "baz" :favoritecolor "a"
            :dateofbirth "b"}
           (parsed-line->map '("foo" "bar" "baz" "a" "b")))))
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