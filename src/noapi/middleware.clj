(ns noapi.middleware
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.core.match :refer [match]]
   [clojure.data.json :as json]
   [clojure.edn :as edn]
   [cognitect.transit :as transit]
   [ring.util.mime-type :as mime]
   [ring.middleware.params :as params]
   [clojure.tools.namespace.find :as ns-find]))

(defn find-matching-namespaces [ns-prefix]
  (->> (all-ns)
       (map ns-name)
       (filter #(.startsWith (-> % name) ns-prefix))))

(defn namespace-route-prefix [namespace ns-prefix route-prefix]
  (str route-prefix (-> (ns-name namespace)
                        (str/replace ns-prefix "")
                        (str/replace "." "/"))))

(defn function-route-mapping [namespace ns-route-prefix]
  (map (fn [[name function]]
         (let [supposed-route (str ns-route-prefix "/" (str name))]
           (if (.endsWith supposed-route "!")
             [{:method :post
               :uri (str/replace supposed-route #"\!$" "")}
              function]
             [{:method :get
               :uri supposed-route}
              function])))
       (ns-publics namespace)))

(defn ns-route-mapping [ns-prefix route-prefix]
  (->> (find-matching-namespaces ns-prefix)
       (map #(vector % (namespace-route-prefix % ns-prefix route-prefix)))
       (mapcat #(apply function-route-mapping %))
       (into {})))

(def content-type-handlers
  {"application/json" {:deserialize #(json/read-str % :key-fn keyword)
                       :serialize json/write-str}
   "application/edn" {:deserialize edn/read-string
                      :serialize pr-str}
   "application/transit+json" {}
   "application/transit+msgpack" {}})

(defn extract-params [request]
  (let [params (:params (params/params-request request))
        serialized-params-key "_"]
    {:extracted (dissoc params serialized-params-key)
     :serialized (params serialized-params-key)}))

(defn compose-params [extracted deserialized]
  (if deserialized
    deserialized
    [extracted]))

(defn call-api-function [f request]
  (let [type (or (mime/ext-mime-type (:uri request))
                 "application/json")
        {:keys [deserialize serialize]} (content-type-handlers type)
        {:keys [extracted serialized]} (extract-params request)
        params (compose-params extracted
                               (if serialized (deserialize serialized) nil))
        raw-result (apply f params)
        result (serialize raw-result)]
    {:status 200
     :headers {"Content-Type" type}
     :body result}))

(defn wrap-api [handler ns-prefix route-prefix]
  (let [routes (ns-route-mapping ns-prefix route-prefix)]
    (fn [request]
      (let [uri (:uri request)
            uri-without-ext (str/replace uri #"\.[^.]+$" "")
            f (routes {:method (:request-method request)
                       :uri uri-without-ext})]
        (if f
          (call-api-function f request)
          (handler request))))))
