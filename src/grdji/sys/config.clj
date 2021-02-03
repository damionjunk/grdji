(ns grdji.sys.config
  (:require [cprop.source :as source]
            [cprop.core :refer [load-config]]
            [mount.core :as mount :refer [defstate]]))

;; Configuration comes from:
;;
;; 1. The bundled `config.edn` located in resources/
;;      make sure this file is there if you want to use this as a default.
;; 2. Command line arguments.
;; 3. An EDN configuration file specified with the -c option.
;; 4. ENV vars
;;
;; They are over-ridden in that order.
(defn source-config []
  (source/from-file
    (get-in (mount/args) [:options :config] "")))


(defstate config
  :start (load-config
           :resource "config.edn"
           ;; see the :options key in `env`
           ;; Attempt to merge the optional --config / -c EDN file
           ;; nil or empty map is acceptable here
           :merge [(mount/args)
                   (try (source-config) (catch Exception e))
                   (source/from-env)]))
