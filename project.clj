(defproject om-twitter "0.1.0-SNAPSHOT"
  :description "A dashbord of the twitter stream."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj" "src/cljs"]

  :dependencies [[org.clojure/clojure "1.7.0-alpha4"]
                 [org.clojure/clojurescript "0.0-2760" :scope "provided"]
                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.1"]
                 [compojure "1.3.1"]
                 [enlive "1.1.5"]
                 [om "0.7.3"]
                 [figwheel "0.1.4-SNAPSHOT"]
                 [environ "1.0.0"]
                 [com.cemerick/piggieback "0.1.5"]
                 [weasel "0.4.2"]
                 [leiningen "2.5.0"]

                 [twitter-api "0.7.7"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [clj-time "0.9.0"]
                 [clj-pid "0.1.1"]
                 [com.stuartsierra/component "0.2.2"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.2"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [com.matthiasnehlsen/inspect "0.1.3"]
                 [com.cognitect/transit-clj  "0.8.259"]
                 [com.cognitect/transit-cljs "0.8.192"]
                 [com.taoensso/sente "1.3.0"]
                 [tailrecursion/cljs-priority-map "1.1.0"]
                 [http-kit "2.1.19"]]

  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-environ "1.0.0"]]

  :min-lein-version "2.5.0"

  :uberjar-name "om-twitter.jar"

  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :compiler {:output-to     "resources/public/js/app.js"
                                        :output-dir    "resources/public/js/out"
                                        :source-map    "resources/public/js/out.js.map"
                                        :preamble      ["react/react.min.js"]
                                        :externs       ["react/externs/react.js"]
                                        :optimizations :whitespace
                                        :pretty-print  true}}}}

  :profiles {:dev {:repl-options {:init-ns om-twitter.main
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :plugins [[lein-figwheel "0.1.4-SNAPSHOT"]]

                   :figwheel {:http-server-root "public"
                              :port 3449
                              :css-dirs ["resources/public/css"]}

                   :env {:is-dev true}

                   :cljsbuild {:builds {:app {:source-paths ["src/cljs"]}}}}

             :uberjar {:hooks [leiningen.cljsbuild]
                       :env {:production true}
                       :omit-source true
                       :aot :all
                       :cljsbuild {:builds {:app
                                            {:source-paths ["env/prod/cljs"]
                                             :compiler
                                             {:optimizations :advanced
                                              :pretty-print false}}}}}}
  )
