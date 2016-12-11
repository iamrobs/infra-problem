(ns front-end.data
  (:require [common-utils.core :as utils]
            [clojure.data.json :as json]
            [org.httpkit.client :as http]))

(def quote-service-url
  (utils/config "QUOTE_SERVICE_URL" "http://10.128.0.3:8080"))

(def newsfeed-service-url
  (utils/config "NEWSFEED_SERVICE_URL" "http://10.128.0.2:8080"))

(def newsfeed-token
  (utils/config "NEWSFEED_SERVICE_TOKEN" "T1&eWbYXNWG1w1^YGKDPxAWJ@^et^&kX"))

(def options {:as         :text
              :timeout    1000
              :user-agent "front-end"})

(defn- handle-response
  [response]
  (let [error (:error @response)
        status (:status @response)]
    (cond (not (nil? error))                   (throw (Exception. "Feed returned error" error))
          (nil? status)                        (throw (Exception. (str "Could not get status of response" response)))
          (or (< status 200) (>= status 300))  (throw (Exception. (str "Error getting response: status " status " returned")))
          :else                                @response)))

(defn get-quote []
  (http/get (str quote-service-url "/api/quote") options))

(defn handle-quote-response
  [resp]
  (json/read-str (:body (handle-response resp))))

(defn get-news []
  (http/get (str newsfeed-service-url "/api/feeds") (assoc-in options
                                                              [:headers "X-Auth-Token"]
                                                              newsfeed-token)))

(defn- handle-news-values
  [key value]
  (if (and (not (nil? value))
           (.contains key "date"))
    (.parse (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSSZ") value)
    value))

(defn- process-entry
  [entry]
  (let [date (or (get entry "updated-date")
                 (get entry "published-date"))]
    (assoc-in entry ["date"] date)))

(defn handle-news-response
  [resp]
  (let [body    (:body (handle-response resp))
        r       (json/read-str body :value-fn handle-news-values)
        entries (get r "entries")]
    (map process-entry entries)))
