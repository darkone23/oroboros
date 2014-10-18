(ns oroboros.handler
  (:require [oroboros.core :refer [circle template-map]]
            [clojure.data.json :as json]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]))

(def directory ".")

(defn get-in-string-cursor
  [coll cursor]
  (get-in coll (map keyword cursor)))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/q" {{:keys [var config]} :params}
    (let [var (if var (clojure.string/split var #"\.") [])
          config (if config (circle directory config) (circle directory))
          config (if (empty? var) config (get-in-string-cursor config var))]
      (when config
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body (json/write-str config)})))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
