(ns grdji.specs
  (:require [clojure.spec.alpha :as sp]
            [tick.alpha.api :as tick]
            [tick.format :as tickf]
            [spec-tools.data-spec :as ds]
            [spec-tools.core :as st])
  (:import (java.time LocalDate)))

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

(defn validate-record
  "Uses the defined spec to validate and coerce the supplied `rec` record,
   which should be a map that conforms to the `the-record-spec` defined above.

   Return is a valid and coerced map containing the same keys."
  [rec]
  (st/decode the-record-spec rec st/string-transformer))