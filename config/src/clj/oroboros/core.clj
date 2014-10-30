(ns oroboros.core
  (:require [matross.mapstache :as mapstache]
            [me.raynes.fs :as fs]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [clj-yaml.core :as yaml]
            [stencil.core :as stencil]))

(defn type-aware-get-in
  ([m ks] (type-aware-get-in m ks nil))
  ([m ks not-found]
   (if-let [k (first ks)]
     (if (sequential? m)
       (if (and (>= k 0) (< k (count m)))
         (recur (nth m k not-found) (rest ks) not-found)
         not-found)
       (if (contains? m k)
         (recur (get m k not-found) (rest ks) not-found)
         not-found))
     m)))

(declare ^:dynamic *oroboros-opts*)
(def default-name "config")

(defn mustache
  "Use stencil for our mustache impl"
  [str vars]
  (stencil/render-string str vars :missing-var-fn :ignore))

(def template-map*
  (partial mapstache/mapstache
    (reify matross.mapstache.IRender
      (can-render? [self v] (and (string? v) (.contains v "{")))
      (render [self template data] (mustache template data)))))

(defn mapstache? [m]
  (instance? matross.mapstache.Mapstache m))

(defn template-map
  [m]
  (if (mapstache? m) m
    (clojure.walk/postwalk
      (fn [v] (if (and (map? v) (not (mapstache? v)))
                (template-map* v) v)) m)))

(defn config []
  "Special map that uses itself for the templating context of its string values"
  (template-map {}))

(defn default? [f]
  (re-matches (re-pattern (str default-name ".*")) f))

(defn from-json [str]
  (let [keyfn (if (:keywords *oroboros-opts*) keyword identity)]
    (template-map (json/read-str str :key-fn keyfn))))

(defn to-json [conf] (json/write-str conf))

(defn write-config [conf path]
  (let [path (fs/expand-home path)
        path (if (fs/directory? path) (fs/file path "config.json") (fs/file path))]
    (fs/mkdirs (fs/parent path))
    (spit path (to-json conf))))

(defn fetch-config
  ([url]
    (fetch-config url default-name))
  ([url config]
    (let [url (str "http://" url "/q")
          opts {:query-params {:config config}}
          res (client/get url opts)]
      (from-json (:body res)))))

(defn sort-configs
  [confs xs]
  (let [sortfn (fn [f]
                 (let [name (first (fs/split-ext f))]
                   (if (default? f) -1 (.indexOf confs name))))]
    (sort-by sortfn xs)))

(defn find-configs
  "return the paths to configs named by the directory"
  [dir & confs]
  (let [exts ["yml" "yaml" "json"]
        confs (cons default-name confs)
        names (into #{} (for [conf confs ext exts] (str conf "." ext)))]
    (for [[root _ files] (-> dir fs/expand-home fs/iterate-dir)
          config (sort-configs confs (clojure.set/intersection files names))]
      (fs/file root config))))

(defn find-names
  "find the names of configs in the directory"
  [dir]
  (let [exts #{".yml" ".yaml" ".json"}]
    (into
     (sorted-set)
     (for [[_ _ files] (-> dir fs/expand-home fs/iterate-dir)
           config files
           :when (let [[name ext] (fs/split-ext config)]
                   (and (exts ext) (not= name default-name)))]
       (first (fs/split-ext config))))))

(defn config-to-cursor
  "~/dir, ~/dir/foo/bar/baz.txt => ['foo' 'bar']"
  [dir conf]
  (let [dir (-> dir fs/expand-home fs/normalized .getAbsolutePath)
        conf (-> conf fs/expand-home fs/normalized fs/parent .getAbsolutePath)
        file-part (subs conf (count dir))
        splits (clojure.string/split
                (apply str (rest file-part))
                (re-pattern java.io.File/separator))]
    (if (= file-part "") []
      (if (:keywords *oroboros-opts*)
        (map keyword splits)
        splits))))

(defn load-config*
  "Load a config file from disk"
  [conf & cursor]
  (let [config (-> conf slurp (yaml/parse-string :keywords (:keywords *oroboros-opts*)))]
    (if (empty? cursor) config
        (assoc-in {} cursor config))))

(defn deep-merge
  "Recursively merges maps. If keys are not maps, the last value wins."
  [& vals]
  (if (every? map? vals)
    (apply merge-with deep-merge vals)
    (last vals)))

(defn overlay
  "Render template values in another context"
  [self other]
  (let [ks (keys self)]
    (select-keys (merge self other) ks)))

(defn load-config
  "Load self referential configs from a directory, named by confs..."
  [dir & confs]
  (let [files (apply find-configs dir confs)
       configs (for [f files] (apply load-config* f (config-to-cursor dir f)))]
    (template-map (apply deep-merge configs))))

(def ^:dynamic *oroboros-opts* {:keywords true})
(defn set-java-opts! []
  (alter-var-root #'oroboros.core/*oroboros-opts*
    (constantly {:keywords false})))
