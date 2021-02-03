(ns grdji.sys.nrepl
  (:require
    [nrepl.server :as nrepl]
    [mount.core :as mount :refer [defstate]]
    [grdji.sys.config :refer [config]]
    [clojure.tools.logging :as log]))

(defn start
  "Start a network repl for debugging on specified port followed by
  an optional parameters map. The :bind, :transport-fn, :handler,
  :ack-port and :greeting-fn will be forwarded to
  clojure.tools.nrepl.server/start-server as they are."
  [{:keys [port bind transport-fn handler ack-port greeting-fn]}]
  (try
    (log/info "starting nREPL server on port" port)
    (nrepl/start-server :port port
                        :bind bind
                        :transport-fn transport-fn
                        :handler handler
                        :ack-port ack-port
                        :greeting-fn greeting-fn)
    (catch Throwable t
      (log/error t "failed to start nREPL")
      (throw t))))

(defn stop [server]
  (nrepl/stop-server server)
  (log/info "nREPL server stopped"))


;; Start a nREPL server attached to this process when the config block `:nrepl` is defined.
(defstate ^{:on-reload :noop} repl-server
          :start (when (and (:nrepl config) (:repl? config)) (start (:nrepl config)))
          :stop  (when repl-server (stop repl-server)))
