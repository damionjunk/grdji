(ns grdji.file
  (:require [clojure.tools.logging :as log]
            [grdji.sys.config :refer [config]]
            [grdji.specs :as sp]
            [grdji.parse :as parse]
            [clojure.java.io :as io]
            [tick.alpha.api :as tick]
            [tick.format :as tickf]
            [clojure.data.csv :as csv])
  (:import (java.time LocalDate)))

;; Composed function for building a validated record.
(def line->rec (comp sp/validate-record parse/parsed-line->map parse/parse-line))

;; Predefined requirements for sorting
(def sorts {;; sorted by email (descending). Then by last name ascending.
            :option-1
            {:keyfn      (juxt :email :lastname)
             :comparator (fn [[e1 l1] [e2 l2]]
                           (compare [e2 l1] [e1 l2]))}

            ;; sorted by birth date, ascending.
            :option-2
            {:keyfn      :dateofbirth
             :comparator compare}

            ;; sorted by last name, descending
            :option-3
            {:keyfn      :lastname
             :comparator (fn [l1 l2] (compare l2 l1))}
            })

(defn file->sorted-recs
  "Opens a file and creates a validated sequence of records sorted using
   the comparators defined in the `sorts` above.

   Returns: A sorted sequence of valid records."
  [file sort-option-key]
  (with-open [file-reader (io/reader file)]
    (sort-by
      (-> sorts sort-option-key :keyfn)
      (-> sorts sort-option-key :comparator)
      (doall (map line->rec (line-seq file-reader))))))

;; Defines both the output position and transformation done on the record.
(def transforms [[:lastname identity]
                 [:firstname identity]
                 [:email identity]
                 [:favoritecolor identity]
                 [:dateofbirth (fn [^LocalDate dob]
                                 (tick/format (tickf/formatter "M/d/yyyy") dob))]])

(defn trans-format
  "Transforms a record into a flattened array suitable for CSV output.
   A formatting function is ran on each element.

   `xforms` is a sequence of tuples with the first element being the source key,
            and the second element the formatting function.
   "
  [xforms rec]
  (map (fn [[k t-fn]] (-> k rec t-fn)) xforms))

(defn transform-sequence
  "Perform the required transformation on a sequence of records."
  [rec-seq]
  (map (partial trans-format transforms)
       rec-seq))

(defn ->csv!
  "Create a CSV from the sequence of records.
  `io-writer` A writer of your choice.
  `sep` Optional separator character, default is \\,"
  [rec-seq io-writer & [sep]]
  (csv/write-csv io-writer (transform-sequence rec-seq)
                 :separator (or sep \,)))

(comment

  ;; The Round trip:

  (with-open [st (java.io.StringWriter.)]
    (->csv! (file->sorted-recs "data/sample1.csv" :option-3) st)
    (println (str st))
    )

  )