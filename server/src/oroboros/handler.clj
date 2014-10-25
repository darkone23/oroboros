(ns oroboros.handler
  (:require [oroboros.core :as config]
            [ring.util.response :as resp]
            [compojure.core :refer :all]
            [clojure.data.json :as json]
            [compojure.handler :as handler]
            [compojure.route :as route]))

(def directory ".")

(defn get-in-string-cursor
  [coll cursor]
  (get-in coll (map keyword cursor)))

(defroutes app-routes
  (GET "/" [] (resp/resource-response "index.html" {:root "public"}))
  (GET "/configs" [] {:status 200
                      :headers {"Content-Type" "application/json"}
                      :body (json/write-str (config/find-names directory))})
  (GET "/q" {{:keys [var config]} :params}
    (let [var (if var (clojure.string/split var #"\.") [])
          config (if config (config/load-config directory config) (config/load-config directory))
          config (if (empty? var) config (get-in-string-cursor config var))]
      (when config
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body (json/write-str config)})))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
