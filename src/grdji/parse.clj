(ns grdji.parse
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [grdji.specs :as sp]))

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
  (zipmap (map first sp/the-record-base) line-seq))

(comment

  (with-open [file-reader (io/reader "data/sample1.csv")]
    (doall (map (comp sp/validate-record parsed-line->map parse-line) (line-seq file-reader)))
    )

  (->
    (parse-line "junk | damion | junkda@gmail.com | blue | 1978-03-02a")
    parsed-line->map
    sp/validate-record
    )

  (->
    (parse-line "junk | damion | junkda@gmail.com | blue | 1978-03-02")
    parsed-line->map
    sp/validate-record
    )

  )