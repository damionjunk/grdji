(ns grdji.core
  (:require [grdji.sys.core :as c]                          ;; system bootstrapping located in sys.core
            [clojure.tools.logging :as log]
            [grdji.file :as gfile]
            [grdji.sys.config :refer [config]])
  (:import (java.io StringWriter)))

(defn handler
  "Primary program flow/control logic.

  Starts a REPL, does file IO, or starts a REST webserver."
  []
  (cond
    (-> config :options :repl)
    (do (log/info "Starting in REPL only mode."))

    (-> config :options :file)
    (do (log/info "File input mode, reading from:" (-> config :options :file))
        (let [sort-option (-> config :options :option (#(str "option-" %)) keyword)]
          (log/info "Sort mode: " sort-option)
          (with-open [st (StringWriter.)]
            (println "")
            (gfile/->csv! (gfile/file->sorted-recs (-> config :options :file) sort-option) st)
            (println (str st))))
        (c/stop-app))))

(defn -main
  [& args]
  (let [{:keys [exit-message ok?]} (c/validate-args args)]
    (when exit-message
      (println exit-message)
      (System/exit (if ok? 0 1)))
    (log/info "DJI Console Starting")
    (c/start-app args handler)))