(ns grdji.test.parse
  (:require [clojure.test :refer :all]
            [grdji.parse :refer :all]
            [clojure.spec.alpha :as sp]))

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
           (parsed-line->map '("foo" "bar" "baz" "a" "b"))))))