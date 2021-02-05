(ns grdji.parse
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [clojure.spec.alpha :as sp]
            [tick.alpha.api :as tick]
            [tick.format :as tickf]
            [spec-tools.data-spec :as ds]
            [spec-tools.core :as st])
  (:import (java.time LocalDate)))

;; Record spec

(def naive-email-re #"[A-Za-z0-9\._+-]+@[A-Za-z0-9_+-]{2,}(\.[A-Za-z0-9_+-]{2,})+$")

(defn datetime-decoder [_ string] (try (LocalDate/parse string (tick/formatter "yyyy-MM-dd"))
                                       (catch Exception e))) ;; We can swallow the exception, because nil is not a valid LocalDate
(defn datetime-encoder [_ datetime] (tick/format (tickf/formatter "yyyy-MM-dd") datetime))

(sp/def ::dob
  (st/spec {:spec          #(instance? java.time.LocalDate %)
            :decode/string datetime-decoder
            :encode/string datetime-encoder}))

(sp/def ::email
  (st/spec {:spec (sp/and string? not-empty #(re-find naive-email-re %))}))

;; Defining our record as a vector of tuples so that we can build a map and
;; also generate an ordered header.
(def the-record-base [[:lastname string?]
                      [:firstname string?]
                      [:email ::email]
                      [:favoritecolor string?]
                      [:dateofbirth ::dob]])

;; This is the data spec "just data" record.
(def the-record (into {} the-record-base))

(def the-record-spec
  (ds/spec ::the-record the-record))

(defn- has-delim? [line delim-re]
  (when (and line delim-re)
    (->> line (re-find delim-re) empty? not)))

(defn has-comma? [line] (has-delim? line #","))
(defn has-space? [line] (has-delim? line #"\s+"))
(defn has-pipe?  [line] (has-delim? line #"\|"))

(defn classify
  "A classification function that returns a keyword or nil based on what is
   found in the source data line. The search is priority ordered:

   :pipe, :comma, :space"
  [line]
  (when-let [line (and line (string? line) (s/trim line))]
    (cond
      (has-pipe? line) :pipe
      (has-comma? line) :comma
      (has-space? line) :space
      :else nil)))

(defn- read-csv [line sep]
  (->> (csv/read-csv line :separator sep)
       (first)
       (map s/trim)))

(def parse-line nil)
(defmulti parse-line
          "Parses an input line using the detected delimiter."
          classify)
(defmethod parse-line :space [line] (read-csv line \ ))
(defmethod parse-line :pipe [line] (read-csv line \|))
(defmethod parse-line :comma [line] (read-csv line \,))
(defmethod parse-line nil [_] (throw (Exception. "No delimiter detected.")))

(defn parsed-line->map
  "Takes a sequence that represents a parsed line and uses `the-record` keys
   above to return a map that is in the form:
   ```
   {:lastname    \"foo\" :firstname \"bar\"
    :email       \"baz\" :favoritecolor \"a\"
    :dateofbirth \"b\"}
   ```
   "
  [line-seq]
  (zipmap (map first the-record-base) line-seq))


(defn validate-record
  "Uses the defined spec to validate and coerce the supplied `rec` record,
   which should be a map that conforms to the `the-record-spec` defined above.

   Return is a valid and coerced map containing the same keys."
  [rec]
  (st/decode the-record-spec rec st/string-transformer))

(comment

  (with-open [file-reader (io/reader "data/sample1.csv")]
    (doall (map (comp validate-record parsed-line->map parse-line) (line-seq file-reader)))
    )

  (->
    (parse-line "junk | damion | junkda@gmail.com | blue | 1978-03-02a")
    parsed-line->map
    validate-record
    )

  (->
    (parse-line "junk | damion | junkda@gmail.com | blue | 1978-03-02")
    parsed-line->map
    validate-record
    )

  )