(ns om-twitter.main
  (:gen-class)
  (:require  [twitterclient.component :as tc]
             [twitterreader.component :as tr]
             [communicator.component :as comm]
             [clojure.edn :as edn]
             [http.component :as http]
             [clojure.tools.logging :as log]
             [clj-pid.core :as pid]
             [com.stuartsierra.component :as component]))

(def conf (edn/read-string (slurp "twitterconf.edn")))

(defn get-system
  "Create system by wiring individual components so that component/start
   will bring up the individual components in the correct order."
  [conf]
  (component/system-map
   :twitterclient-channels (tc/new-twitterclient-channels)
   :twitterclient (component/using (tc/new-twitterclient conf) {:channels :twitterclient-channels})
   ;:twitterreader (component/using (tr/new-twitterreader) {:channels :twitterclient-channels})
   :comm-channels          (comm/new-communicator-channels)
   :comm          (component/using (comm/new-communicator)     {:comm-chans   :comm-channels
                                                                :tc-chans   :twitterclient-channels})
   :http          (component/using (http/new-http-server conf) {:comm       :comm})))

(def system (get-system conf))

(defn -main [& args]
  (pid/save (:pidfile-name conf))
  (pid/delete-on-shutdown! (:pidfile-name conf))
  (log/info "Application started, PID" (pid/current))
  (alter-var-root #'system component/start))
