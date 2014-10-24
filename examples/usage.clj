(use 'oroboros.core)

;; by using simple template strings we can enjoy extremely flexible configuration

;; == examples/simple/config.yaml ==
;;
;; cat: tom
;; mouse: jerry
;; name: '{{ cat }} & {{ mouse }}'
;; best: '{{ favorite }}'

(load-config "examples/simple")
;; => {:cat "tom", :mouse "jerry", :name "tom & jerry", :best "{{favorite}}"}
;; name is templated, but since best is undefined it is ignored

;; we can change the context and also include files named 'tom' or 'jerry'
;; these files are allowed to provide overrides to be merged into the config

(load-config "examples/simple" "tom")

  ;; => {:favorite "tom", :cat "tom", :mouse "jerry", :name "tom & jerry", :best "tom"}

(load-config "examples/simple" "jerry")

  ;; => {:favorite "jerry", :cat "tom", :mouse "jerry", :name "tom & jerry", :best "jerry"}

;; load-config supports local template references within a single config file
;; load-config also supports global references across namespaces

;; $ tree examples/advanced
;; examples/advanced/
;; ├── config.yaml
;; ├── db
;; │   ├── config.yaml
;; │   └── production.yaml
;; └── web
;;     ├── config.yaml
;;     └── production.yaml

(load-config "examples/advanced")

  ;; => {:web {:port 1337, :protocol "http", :host "web.example.com:1337",
  ;;           :api "http://web.example.com:1337/v/1.2.3",
  ;;           :command "./bin/start --db db.example.com" },
  ;;     :db {:host "db.example.com"},
  ;;     :version "1.2.3"}

;; this becomes considerable when using templates across environment & app contexts

(load-config "examples/advanced" "production")

  ;; => {:web {:port 1337, :protocol "https", :host "expensive-server.example.com",
  ;;           :api "https://expensive-server.example.com/v/1.2.3",
  ;;           :command "./bin/start --db prod-db.example.com" },
  ;;     :db {:host "prod-db.example.com"},
  ;;     :version "1.2.3"}

;; in code, configuration is live and behaves like a regular map

(def config (load-config "examples/advanced" "production"))

(-> config
    (assoc-in [:version] "1.2.4")
    (get-in [:web :api]))

  ;; => "https://expensive-server.example.com/v/1.2.3"
