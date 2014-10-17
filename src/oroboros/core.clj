(ns oroboros.core
  (:require [matross.mapstache :refer [string-renderer mapstache]]
            [me.raynes.fs :refer [file iterate-dir normalized parent expand-home split-ext]]
            [clj-yaml.core :refer [parse-string]]
            [stencil.core :refer [render-string]]))

(defn mustache
  "Use stencil for our mustache impl"
  [str vars]
  (render-string str vars :missing-var-fn :ignore))

(def template-map
  "Special map that uses itself for the templating context of its string values"
  (partial mapstache (string-renderer mustache)))

(def default-name "config")

(defn default? [f]
  (re-matches (re-pattern (str default-name ".*")) f))

(defn sort-configs
  [confs xs]
  (let [sortfn (fn [f]
                 (let [name (first (split-ext f))]
                   (if (default? f) -1 (.indexOf confs name))))]
    (sort-by sortfn xs)))

(defn find-config-files
  "return the paths to configs named by the directory"
  [dir & confs]
  (let [exts ["yml" "yaml" "json"]
        confs (cons default-name confs)
        names (into #{} (for [conf confs ext exts] (str conf "." ext)))]
    (for [[root _ files] (-> dir expand-home iterate-dir)
          config (sort-configs confs (clojure.set/intersection files names))]
      (file root config))))

(defn config-to-cursor
  "~/dir, ~/dir/foo/bar/baz.txt => ['foo' 'bar']"
  [dir conf]
  (let [dir (-> dir expand-home normalized .getAbsolutePath)
        conf (-> conf expand-home normalized parent .getAbsolutePath)
        file-part (subs conf (count dir))
        splits (clojure.string/split
                (apply str (rest file-part))
                (re-pattern java.io.File/separator))]
    (if (= file-part "") []
      (map keyword splits))))

(defn load-config
  "Load a template-map config file from disk"
  [conf & cursor]
  (let [config (-> conf slurp parse-string template-map)]
    (if (empty? cursor) config
        (assoc-in nil cursor config))))

(defn deep-merge
  "Recursively merges maps. If keys are not maps, the last value wins."
  [& vals]
  (if (every? map? vals)
    (apply merge-with deep-merge vals)
    (last vals)))

(defn config-circle
  "Load self referential configs from a directory, named by confs..."
  [dir & confs]
  (let [files (apply find-config-files dir confs)
        configs (for [f files] (apply load-config f (config-to-cursor dir f)))]
    (template-map (apply deep-merge configs))))
