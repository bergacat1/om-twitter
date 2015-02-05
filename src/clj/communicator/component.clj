(ns communicator.component
  (:gen-class)
  (:require
   [clojure.pprint :as pp]
   [clojure.tools.logging :as log]
   [communicator.websockets :as ws]
   [taoensso.sente :as sente]
   [taoensso.sente.packers.transit :as sente-transit]
   [com.stuartsierra.component :as component]
   [clojure.core.async :as async :refer [chan]]))

(def packer (sente-transit/get-flexi-packer :json)) ;; serialization format for client<->server comm

(defrecord Communicator [comm-chans tc-chans chsk-router]
  component/Lifecycle
  (start [component] (log/info "Starting Communicator Component")
         (let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn connected-uids]}
               (sente/make-channel-socket! {:packer packer :user-id-fn ws/user-id-fn})]
           (ws/send-loop (:tweets tc-chans) send-fn connected-uids)
           (assoc component :ch-recv ch-recv
                            :send-fn send-fn
                            :ajax-post-fn ajax-post-fn
                            :ajax-get-or-ws-handshake-fn ajax-get-or-ws-handshake-fn
                            :connected-uids connected-uids)))
  (stop [component] (log/info "Stopping Communicator Component")
        ;(chsk-router) ;; stops router loop
        (assoc component :ch-recv nil
                         :send-fn nil
                         :ajax-post-fn nil
                         :ajax-get-or-ws-handshake-fn nil
                         :connected-uids nil)))

(defn new-communicator [] (map->Communicator {}))

(defrecord Communicator-Channels []
  component/Lifecycle
  (start [component] (log/info "Starting Communicator Channels Component")
         (assoc component
           :query (chan)
           :query-results (chan)
           :tweet-missing (chan)
           :missing-tweet-found (chan)
           :tweet-count (chan)))
  (stop [component] (log/info "Stop Communicator Channels Component")
        (assoc component :query nil :query-results nil :tweet-missing nil
                         :missing-tweet-found nil :tweet-count nil)))

(defn new-communicator-channels [] (map->Communicator-Channels {}))
