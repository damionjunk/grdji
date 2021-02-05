(ns grdji.sys.core
  (:require [clojure.tools.logging :as log]
            [clojure.tools.cli :refer [parse-opts]]
            [mount.core :as mount :refer [defstate]]
            [clojure.java.io :as io]
            [grdji.sys.nrepl]
            [grdji.sys.config :refer [config]]
            [clojure.string :as s]))

;; log uncaught exceptions in threads
(Thread/setDefaultUncaughtExceptionHandler
  (reify Thread$UncaughtExceptionHandler
    (uncaughtException [_ thread ex]
      (log/error {:what      :uncaught-exception
                  :exception ex
                  :where     (str "Uncaught exception on" (.getName thread))}))))

(def cli-options
  [["-c" "--config EDNFILE"  "EDN Config Overrides"]
   ["-r" "--repl"            "Start a repl and do nothing."]
   ["-f" "--file INPUT"      "Input Records File"
    :parse-fn io/file
    :validate [#(.exists %) "Input file not found."]]
   ["-o" "--option OPT" "Sort option 1: email(desc)last(asc) 2: dob(asc) 3: last(desc)"
    :parse-fn #(Long. %)
    :validate [#(and (> % 0) (<= % 3)) "Sort option must be 1, 2, or 3."]
    :default 1]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["grdji record parser"
        ""
        "Usage: grdji [options]"
        ""
        "Options:"
        options-summary]
       (s/join "\n")))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (s/join \n errors)))

(defn validate-args
  [args]
  (let [{:keys [options arguments errors summary] :as opts} (parse-opts args cli-options)]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (usage summary) :ok? true}

      ;; Ignore commands for migration and just start the repl
      (:repl options) (merge opts {:repl? true})

      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}

      ;; The only other valid runmode besides REPL for now.
      (:file options) opts

      :else ; failed custom validation => exit with usage summary
      {:exit-message (usage summary)})))

(defn stop-app []
  (doseq [component (:stopped (mount/stop))]
    ;(log/info component "stopped")
    )
  (shutdown-agents))

(defn get-components [args]
  (-> args
      validate-args
      mount/start-with-args
      :started))

(defn start-app [args handler]
  (doseq [component (get-components args)]
    (log/info component "started"))

  (handler)
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))