(ns oroboros.core
  (:require [matross.mapstache :refer [string-renderer mapstache]]
            [stencil.core :refer [render-string]]))

(defn- mustache
  "Use stencil for our mustache impl"
  [str vars]
  (render-string str vars :replace-missing-vars false))

(def template-map
  "Special map that uses itself for templating context of its string values"
  (partial mapstache (string-renderer mustache)))
