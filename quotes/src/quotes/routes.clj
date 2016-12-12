(ns quotes.routes
  (:gen-class)
  (:use [quotes.api :only [api-routes]])
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [common-utils.core :as utils]
            [common-utils.middleware :refer [correlation-id-middleware]]
            [clojure.tools.logging :as log]
            [org.httpkit.server :refer [run-server]]))

(defroutes app-routes
  (GET "/ping" [] {:status 200})
  (context "/api" []
           (api-routes)))

(def app
  (-> app-routes
      correlation-id-middleware))

(defn -main []
  (let [port (Integer/parseInt (utils/config "APP_PORT" "8085"))]
    (log/info "Running quotes on port" port)
    (run-server (var app) {:ip "0.0.0.0"
                           :port port})))
