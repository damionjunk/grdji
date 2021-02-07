(ns grdji.test.http
  (:require [clojure.test :refer :all]
            [grdji.http :refer :all]
            [clojure.spec.alpha :as sp]
            [reitit.ring :as ring]
            [clojure.java.io :as io])
  (:import (java.time LocalDate)))

(deftest ok-resp-test
  (testing "OK"
    (is (= {:status 200 :headers {} :body nil} (ok nil)))
    (is (= {:status 200 :headers {} :body [1 2 3 4]} (ok [1 2 3 4])))))

(deftest pre-load-test
  (testing "file preload and API results"
    (do
      (pre-load-records (io/resource "testdata.csv"))
      (is (= (count @records) 2)))

    (-> (app {:uri "/records/email" :request-method :get})
        :body
        slurp
        (= "[{\"lastname\":\"bbb\",\"firstname\":\"bbbbc\",\"email\":\"aaaaaa@gfmail.com\",\"favoritecolor\":\"blue\",\"dateofbirth\":\"1918-04-15\"},{\"lastname\":\"ccc\",\"firstname\":\"ccccd\",\"email\":\"aaaaaa@gfmail.com\",\"favoritecolor\":\"blue\",\"dateofbirth\":\"1988-04-15\"}]")
        is)

    (-> (app {:uri "/records/birthdate" :request-method :get})
        :body
        slurp
        (= "[{\"lastname\":\"bbb\",\"firstname\":\"bbbbc\",\"email\":\"aaaaaa@gfmail.com\",\"favoritecolor\":\"blue\",\"dateofbirth\":\"1918-04-15\"},{\"lastname\":\"ccc\",\"firstname\":\"ccccd\",\"email\":\"aaaaaa@gfmail.com\",\"favoritecolor\":\"blue\",\"dateofbirth\":\"1988-04-15\"}]")
        is)

    (-> (app {:uri "/records/name" :request-method :get})
        :body
        slurp
        (= "[{\"lastname\":\"ccc\",\"firstname\":\"ccccd\",\"email\":\"aaaaaa@gfmail.com\",\"favoritecolor\":\"blue\",\"dateofbirth\":\"1988-04-15\"},{\"lastname\":\"bbb\",\"firstname\":\"bbbbc\",\"email\":\"aaaaaa@gfmail.com\",\"favoritecolor\":\"blue\",\"dateofbirth\":\"1918-04-15\"}]")
        is)))

(deftest post-record-test
  )

