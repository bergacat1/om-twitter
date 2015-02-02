(ns communicator.websockets
  (:gen-class)
  (:require
   [clojure.core.match :as match :refer (match)]
   [clojure.pprint :as pp]
   [clojure.tools.logging :as log]
   [taoensso.sente :as sente]
   [com.matthiasnehlsen.inspect :as inspect :refer [inspect]]
   [clojure.core.async :as async :refer [<! >! put! timeout go-loop]]))

(defn user-id-fn
  "generates unique ID for request"
  [req]
  (let [uid (str (java.util.UUID/randomUUID))]
    (log/info "Connected:" (:remote-addr req) uid)
    uid))

(defn make-handler
  "create event handler function for the websocket connection"
  [query-chan tweet-missing-chan register-percolation-chan]
  (fn [{event :event}]
    (match event
           [:cmd/percolate params] (put! register-percolation-chan params)
           [:cmd/query params]     (put! query-chan params)
           [:cmd/missing params]   (put! tweet-missing-chan params)
           [:chsk/ws-ping]         () ; currently just do nothing with ping (no logging either)
           :else                   (log/info "Unmatched event:" (pp/pprint event)))))

(defn send-loop
  "run loop, call f with message on channel"
  [channel f]
  (go-loop [] (let [msg (<! channel)] (println (:text msg))) (recur)))

(defn tweet-stats
  "send stats about number of indexed tweets to all connected clients"
  [uids chsk-send!]
  (fn [msg] (doseq [uid (:any @uids)]
              (chsk-send! uid [:stats/total-tweet-count msg]))))

(defn perc-matches
  "deliver percolation matches to interested clients"
  [uids chsk-send!]
  (fn [msg]
    (inspect :comm/perc-matches msg)
    (let [[t matches subscriptions] msg]
      (doseq [uid (:any @uids)]
        (when (contains? matches (get subscriptions uid))
          (chsk-send! uid [:tweet/new t]))))))

(defn relay-msg
  "send query result chunks back to client"
  [msg-type msg-key chsk-send!]
  (fn [msg] (chsk-send! (:uid msg) [msg-type (msg-key msg)])))

(defn run-users-count-loop
  "runs loop for sending stats about number of connected users to all connected clients"
  [chsk-send! connected-uids]
  (go-loop [] (<! (timeout 2000))
           (let [uids (:any @connected-uids)]
             (inspect :comm/connected-uids uids)
             (doseq [uid uids] (chsk-send! uid [:stats/users-count (count uids)])))
           (recur)))
