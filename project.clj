(defproject temperatures "0.1.0-SNAPSHOT"
  :description "..."
  :url "..."
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.8.40"]
                 [devcards "0.2.1-6"]
                 [reagent "0.6.0-alpha"]
                 [sablono "0.6.2"]
                 [cljsjs/nvd3 "1.8.1-0"]]
  :plugins [[lein-cljsbuild "1.1.3"]
            [lein-figwheel "0.5.2"]]
  :main ^:skip-aot temperatures.core
  :target-path "target/%s"
  :profiles {:dev     {:dependencies [[com.cemerick/piggieback "0.2.1"]
                                      [figwheel-sidecar "0.5.2"]]
                       :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]
                                      :init             (do (use 'figwheel-sidecar.repl-api)
                                                            (start-figwheel!))}
                       :source-paths ["src-cljs"]}
             :uberjar {:aot :all}}
  :clean-targets ^{:protect false} ["resources/public/js/" "target"]
  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["src-cljs"]
                        :figwheel     {:devcards       true
                                       :websocket-host :js-client-host}
                        :compiler     {:main                 "temperatures.core"
                                       :asset-path           "js/out"
                                       :output-to            "resources/public/js/temperatures.js"
                                       :output-dir           "resources/public/js/out"
                                       :source-map-timestamp true}}]})
