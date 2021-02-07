(ns grdji.core
  (:require [grdji.sys.core :as c]                          ;; system bootstrapping located in sys.core
            [clojure.tools.logging :as log]
            [grdji.http :as http]
            [grdji.file :as gfile]
            [grdji.sys.config :refer [config]])
  (:import (java.io StringWriter)))

(defn handler
  "Primary program flow/control logic.

  Starts a REPL, does file IO, or starts a REST webserver."
  []
  (when (-> config :repl?)
    (log/info "Starting REPL using default configuration."))

  (when (-> config :output?)
    (do (log/info "File input mode, reading from:" (-> config :options :file))
        (let [sort-option (-> config :options :output (#(str "option-" %)) keyword)]
          (log/info "Sort mode: " sort-option)
          (with-open [st (StringWriter.)]
            (println "")
            (gfile/->csv! (gfile/file->sorted-recs (-> config :options :file) sort-option) st)
            (println (str st))))
        ;; Shut everything down if we're not running a webserver.
        (when-not (-> config :http?)
          (c/stop-app))))

  (when (-> config :http?)
    (do (log/info "Starting HTTP server")
        (when (-> config :file?)
          (http/pre-load-records (-> config :options :file)))
        (http/start))))

(defn -main
  [& args]
  (let [{:keys [exit-message ok?]} (c/validate-args args)]
    (when exit-message
      (println exit-message)
      (System/exit (if ok? 0 1)))
    (log/info "DJI Console Starting")
    (c/start-app args handler)))