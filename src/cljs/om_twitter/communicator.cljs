(ns om_twitter.communicator
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [taoensso.sente  :as sente  :refer (cb-success?)]
            [taoensso.sente.packers.transit :as sente-transit]
            [cljs.core.async :as async :refer [<! chan put!]]))

(def packer
  "Defines our packing (serialization) format for client<->server comms."
  (sente-transit/get-flexi-packer :json))

(defn make-handler
  "Create handler function for messages from WebSocket connection, wire channels and the
   start-function to call when the socket is established."
  [data-chan]
  (fn [payload]
    (println payload)))

(defn query-loop
  "Take command / query message off of channel, enrich payload with :uid of current
   WebSocket connection and send to server. Channel is injected when loop is started."
  [channel send-fn chsk-state]
  (go-loop []
           (let [[cmd-type payload] (<! channel)]
             (send-fn [cmd-type (assoc payload :uid (:uid @chsk-state))])
             (recur))))

(defn start-communicator
  "Start communicator by wiring channels."
  [data-chan]
  (let [ws (sente/make-channel-socket! "/chsk" {:packer packer :type :auto})
        {:keys [ch-recv send-fn state]} ws
        handler (make-handler data-chan)]
    (sente/start-chsk-router! ch-recv handler)))
