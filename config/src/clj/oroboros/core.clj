(ns oroboros.core
  (:require [clojure.java.io :as io]
            [cpath-clj.core :as cp]
            [matross.mapstache :as mapstache]
            [me.raynes.fs :as fs]
            [clojure.data.json :as json]
            [clj-yaml.core :as yaml]
            [stencil.core :as stencil]))

(declare ^:dynamic *oroboros-opts*)
(def default-name "config")

(defn extract-ns [k]
  (if (keyword? k)
    (namespace k)
    (last (re-find #"(.+)\/[^\s+]") k)))

(defn mustache
  "Use stencil for our mustache impl"
  [str vars]
  (stencil/render-string str vars :missing-var-fn :ignore))

(defn type-aware-get-in
  ([x ks] (type-aware-get-in x ks nil))
  ([x ks not-found]
     (if-let [k (first ks)]
       (cond
        (sequential? x)
        (if (<= 0 (inc k) (count x))
          (recur (nth x k not-found) (rest ks) not-found)
          not-found)
        (associative? x)
        (if (contains? x k)
          (recur (get x k not-found) (rest ks) not-found)
          not-found))
       x)))

(defn cursor-guess
  "take a best guess at what to do for the given type"
  [x]
  (try (Integer/parseInt x)
       (catch Exception e
         (if (:keywords *oroboros-opts*)
           (keyword x) x))))

(defn extract-token [s]
  "attempt to extract a token from a str"
  (second (re-find #"^\{\{\s*([^\s]+)\s*\}\}$" s)))

(defn extract-cursor [s]
  "attempt to extract a cursor from a str"
  (when s (map cursor-guess (clojure.string/split s #"\."))))

(defn env-replace-template [s]
  (clojure.string/replace
   s #"\{\{\s*env\/([^\s]+)\s*\}\}" "{{ __ENV__.$1 }}"))

(defn replace-dots [s]
  (clojure.string/replace s #"\." "__"))

(defn props-replace-template [s]
  (clojure.string/replace
   s #"\{\{\s*props\/([^\s]+)\s*\}\}"
   (fn [[matched s]]
     (str "{{ __PROPS__." (replace-dots s) " }}"))))


(defn getenv* []
  (let [env (into {} (System/getenv))]
    (if (:keywords *oroboros-opts*)
      (clojure.walk/keywordize-keys env) env)))

(defn getprops* []
  (let [props (into {} (map (fn [[k v]] [(replace-dots k) v]) (System/getProperties)))]
    (if (:keywords *oroboros-opts*)
      (clojure.walk/keywordize-keys props) props)))

(def getenv (memoize getenv*))
(def getprops (memoize getprops*))

(defn prepare-data [data]
  (assoc data
    :__ENV__ (getenv)
    :__PROPS__ (getprops)))

(defn prepare-template [str]
  (props-replace-template (env-replace-template str)))

(def template-map*
  (partial mapstache/mapstache
    (reify matross.mapstache.IRender
      (can-render? [self v] (and (string? v) (.contains v "{")))
      (render [self template data]
        (let [data (prepare-data data)
              template (prepare-template template)]
          (if-let [cursor (-> template extract-token extract-cursor)]
            (type-aware-get-in data cursor template)
            (mustache template data)))))))

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
  (re-matches (re-pattern (str default-name ".*")) (str f)))

(defn from-json [str]
  (let [keyfn (if (:keywords *oroboros-opts*) keyword identity)]
    (template-map (json/read-str str :key-fn keyfn))))

(defn to-json [conf] (json/write-str (into (sorted-map) conf)))

(defn write-config [conf path]
  (let [path (fs/expand-home path)
        path (if (fs/directory? path) (fs/file path "config.json") (fs/file path))]
    (fs/mkdirs (fs/parent path))
    (spit path (to-json conf))))

(defn extract-filename [x]
  (last (re-find #"([^\/]+)\..+$" (str x))))

(defn sort-configs
  [confs xs]
  (let [sortfn (fn [f]
                 (let [name (extract-filename f)]
                   (if (default? f) -1 (.indexOf confs name))))]
    (sort-by sortfn xs)))

(defn find-configs-from-classpath [dir & confs]
  (let [exts ["yml" "yaml" "json"]
        confs (cons default-name confs)
        names (into #{} (for [conf confs ext exts] (str conf "." ext)))]
    (if-let [resource (io/resource dir)]
      (let [xs (mapcat second (cp/resources resource))
            f (fn [path]
                (names (last (clojure.string/split (str path) (re-pattern java.io.File/separator)))))
            res (filter f xs)]
        (sort-configs confs res)))))

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

(defn split* [str]
  (let [splits (clojure.string/split str (re-pattern java.io.File/separator))]
    (if (:keywords *oroboros-opts*)
      (map keyword splits)
      splits)))

(defn config-to-cursor
  "~/dir, ~/dir/foo/bar/baz.txt => ['foo' 'bar']"
  [dir path]
  (let [dir (-> dir fs/expand-home fs/normalized .getAbsolutePath)
        conf (-> path fs/expand-home fs/normalized fs/parent .getAbsolutePath)
        file-part (subs conf (count dir))]
    (if (= file-part "") []
      (split* (apply str (rest file-part))))))

(defn resource-to-cursor
  [dir uri]
  (let [dir (io/resource dir)
        dir (if (= (type dir) java.net.URL) (.toURI dir) dir)
        uri (if (= (type uri) java.net.URL) (.toURI uri) uri)
        rel (str (.relativize dir uri))]
    (butlast (split* rel))))

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

(defn get-config-fn
  [load-fn cursor-fn]
  (fn [dir & confs]
    (let [files (apply load-fn dir confs)
          configs (for [f files] (apply load-config* f (cursor-fn dir f)))]
      (template-map (apply deep-merge configs)))))

(def load-config (get-config-fn find-configs config-to-cursor))
(def resource-config (get-config-fn find-configs-from-classpath resource-to-cursor))

(def ^:dynamic *oroboros-opts* {:keywords true})
(defn set-java-opts! []
  (alter-var-root #'oroboros.core/*oroboros-opts*
    (constantly {:keywords false})))
