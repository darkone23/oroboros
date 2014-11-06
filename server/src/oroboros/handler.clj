(ns oroboros.handler
  (:require [oroboros.core :as config]
            [ring.util.response :as resp]
            [compojure.core :refer :all]
            [clojure.data.json :as json]
            [compojure.handler :as handler]
            [compojure.route :as route]))

(def directory ".")

(defn json-ok [body]
  (let [body (if (associative? body) (into (sorted-map) body) body)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/write-str body)}))

(defroutes app-routes
  (GET "/" [] (resp/resource-response "index.html" {:root "public"}))
  (GET "/configs" [] (json-ok (config/find-names directory)))
  (GET "/q" {{:keys [config var] :or {config "config"}} :params}
    (let [cursor (if var (config/extract-cursor var) [])
          config (config/load-config directory config)]
      (if-let [conf (config/type-aware-get-in config cursor)]
        (json-ok conf))))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
