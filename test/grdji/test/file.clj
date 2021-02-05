(ns grdji.test.file
  (:require [clojure.test :refer :all]
            [grdji.file :refer :all]
            [clojure.spec.alpha :as sp])
  (:import (java.time LocalDate)))

(def sortable-data
  [["a" "1"] ["e" "2"] ["e" "3"]
   ["e" "4"] ["c" "5"] ["b" "6"]])

(deftest line
  (testing "line to record creation"
    (is (= ::sp/invalid (line->rec "1,2,3,4,5")))
    (is (= ::sp/invalid (line->rec "1,2,be@you.com,4,1999/01/01")))
    (is (= ::sp/invalid (line->rec "1,2,be@you,4,1999-01-01")))
    (is (=  {:lastname "1", :firstname "2", :email "be@you.com", :favoritecolor "4", :dateofbirth #time/date "1999-01-01"}
            (line->rec "1,2,be@you.com,4,1999-01-01")))
    (is (=  {:lastname "1", :firstname "2", :email "be@you.com", :favoritecolor "4", :dateofbirth #time/date "1999-01-01"}
            (line->rec "1 2 be@you.com 4 1999-01-01")))
    (is (=  {:lastname "1", :firstname "2", :email "be@you.com", :favoritecolor "4", :dateofbirth #time/date "1999-01-01"}
            (line->rec "1 | 2 | be@you.com | 4 | 1999-01-01")))))

(deftest sorting
  (testing "option-1 A desc B asc"
    (-> (sort (-> sorts :option-1 :comparator) sortable-data)
        (= '(["e" "2"] ["e" "3"] ["e" "4"]
             ["c" "5"] ["b" "6"] ["a" "1"]))
        is))
  (testing "option-2 A asc"
    (-> (sort (-> sorts :option-2 :comparator) sortable-data)
        (= '(["a" "1"] ["b" "6"] ["c" "5"] ["e" "2"] ["e" "3"] ["e" "4"]))
        is))
  (testing "option-3 A desc"
    (-> (sort (-> sorts :option-3 :comparator) sortable-data)
        (= '(["e" "4"] ["e" "3"] ["e" "2"] ["c" "5"] ["b" "6"] ["a" "1"]))
        is)))

(deftest keyfn
  (testing "correct key functions are returned from sortables"
    (is (= :dateofbirth (-> sorts :option-2 :keyfn)))
    (is (= :lastname (-> sorts :option-3 :keyfn)))
    ;; Test the juxt keyfn by using it and testing the return.
    (is (= [:hello :world]
           ((-> sorts :option-1 :keyfn)
            {:email :hello
             :lastname :world})))))

(deftest transformat
  (testing "formatted sequence creation from a record."
    (-> (trans-format transforms {:lastname    "smith" :firstname "jane"
                                  :email       "foo@bar.com" :favoritecolor "blue"
                                  :dateofbirth (LocalDate/parse "1999-01-01")})
        (= '("smith" "jane" "foo@bar.com" "blue" "1/1/1999"))
        is)))
