(ns grdji.sys.core
  (:require [clojure.tools.logging :as log]
            [clojure.tools.cli :refer [parse-opts]]
            [mount.core :as mount :refer [defstate]]
            [grdji.sys.nrepl]
            [grdji.sys.config]))

;; log uncaught exceptions in threads
(Thread/setDefaultUncaughtExceptionHandler
  (reify Thread$UncaughtExceptionHandler
    (uncaughtException [_ thread ex]
      (log/error {:what      :uncaught-exception
                  :exception ex
                  :where     (str "Uncaught exception on" (.getName thread))}))))

(def cli-options
  [["-c" "--config EDNFILE"  "EDN Config Overrides"]])

(defn stop-app []
  (doseq [component (:stopped (mount/stop))]
    (log/info component "stopped"))
  (shutdown-agents))

(defn get-components [args]
  (-> args
      (parse-opts cli-options)
      mount/start-with-args
      :started))

(defn start-app [args]
  (doseq [component (get-components args)]
    (log/info component "started"))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))