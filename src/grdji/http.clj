(ns grdji.http
  (:require [clojure.tools.logging :as log]
            [ring.adapter.jetty :refer [run-jetty]]
            [grdji.sys.config :refer [config]]
            [ring.util.http-response :as response]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.coercion :as coercion]
            [ring.middleware.params :as params]
            [muuntaja.core :as m]
            [reitit.ring :as ring]
            [grdji.file :as f]
            [clojure.java.io :as io]
            [grdji.specs :as sp]))

;; This is our datastore; it would normally be a database instead.
(defonce records (atom []))

(defn pre-load-records
  "Populate the records atom with the contents of the specified `file`."
  [file]
  (log/info "Loading records from file: " file)
  (with-open [file-reader (io/reader file)]
    (reset! records (doall (filter sp/valid-record? (map f/line->rec (line-seq file-reader)))))))

(defn add-line
  "Adds a line to the `records` atom if it's valid. The Valid record is returned.
   nil is returned on an invalid record addition attempt."
  [line]
  (let [rec (f/line->rec line)]
    (if (sp/valid-record? rec)
      (do
        (log/info "Adding record:" rec)
        (swap! records conj rec)
        rec)
      (log/info "Invalid record: " line))))

(defn ok
  "Set the body in the http 200 response map to `data`.
  Coercion happens in the middleware, so this should be standard data structures."
  [data]
  (-> (response/ok)
      (assoc :body data)))

(defn get-records
  "Access the records from our model and return them in sorted order according
  to the `sort-key` definition."
  [sort-key]
  (sort-by
    (-> f/sorts sort-key :keyfn)
    (-> f/sorts sort-key :comparator)
    @records))

(def routes
  [["/records"
    {:post (fn [{params :params}]
             (log/debug "POST record:" (-> params (get "line")))
             (ok (add-line (-> params (get "line")))))}]
   ["/records/email"
    {:get (fn [_]
            (log/debug "GET email")
            (ok (get-records :option-1)))}]
   ["/records/birthdate"
    {:get (fn [_]
            (log/debug "GET birthdate")
            (ok (get-records :option-2)))}]
   ["/records/name"
    {:get (fn [_]
            (log/debug "GET lastname")
            (ok (get-records :option-3)))}]])

(def app
  (ring/ring-handler
    (ring/router
      routes
      {:data {:muuntaja   m/instance
              :middleware [params/wrap-params
                           muuntaja/format-response-middleware
                           muuntaja/format-request-middleware
                           coercion/coerce-response-middleware
                           coercion/coerce-request-middleware]}})

    (ring/routes (ring/create-default-handler))))

(defn start []
  (run-jetty #'app {:port 3000 :join? false}))
