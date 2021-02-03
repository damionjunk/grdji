(ns grdji.core
  (:require [grdji.sys.core :as c]                          ;; system bootstrapping located in sys.core
            [clojure.tools.logging :as log]))

(defn -main
  [& args]
  (log/info "DJI Console Starting")
  (c/start-app args))