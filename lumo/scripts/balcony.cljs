#!/usr/bin/env bash

":";tools_cli='org.clojure/tools.cli {:mvn/version "0.3.7"}'
":";momentjs='org.webjars.npm/moment {:mvn/version "2.22.1"}'
":";nodemailer='org.webjars.npm/nodemailer {:mvn/version "4.6.5"}'
":";src=$(clojure -Srepro -Spath -Sdeps "{:deps {$tools_cli $moment $nodemailer}}")

"exec" "lumo" "-K" "-c" "$src" "$0" "$@"

(ns balcony.core
  (:require
   [cljs.nodejs :as node :refer [require]]
   [clojure.string :as str]
   [clojure.tools.cli :refer [parse-opts]]
   [goog.object :as gobject]
   [goog.string :as gstring]
   [goog.string.format] ;; don't remove
   [moment]
   [nodemailer :as nm]))

(node/enable-util-print!)

(defonce http (node/require "http"))
(defonce https (node/require "https"))

(defn get-url [url cb]
  (.get (if (str/starts-with? url "https") https http) url cb))

(defn GET [url]
  (js/Promise.
   (fn [resolve reject]
     (get-url url
              (fn [res]
                (let [body (atom "")]
                  (-> res
                      (.on "data" #(swap! body str (.toString %)))
                      (.on "end" #(-> @body
                                      js/JSON.parse
                                      (js->clj :keywordize-keys true)
                                      resolve)))))))))

(defn env
  [name]
  (gobject/get js/process.env name))

(def transporter (nm/createTransport
                  (clj->js {:service "gmail"
                            :auth
                            {:user (env "MAIL_USER")
                             :pass (env "MAIL_PASS")}})))

(def cli-options
  [["-m" "--mail" "Mail if average temperature of today is above threshold"]])

(def now (moment))
(def date-format "YYYY-MM-DD")
(def today (.format now date-format))
(def tomorrow (.format (.add now 1 "days") date-format))

(def WEATHER_API
  (gstring/format "https://api.weatherbit.io/v2.0/history/hourly?city=Amersfoort,NL&start_date=%s&end_date=%s&key=%s"
                  today
                  tomorrow
                  (env "WEATHER_API_KEY")))

(def MAIL_TO
  (or (env "MAIL_TO") "michielborkent@gmail.com"))

(def MAIL_SUBJECT
  (or (env "MAIL_SUBJECT") "You need to water the balcony today."))

(def MAIL_BODY
  (or (env "MAIL_BODY") "Please water the balcony tonight. The average temperature between 9AM and 7PM was {{avg}} degrees Celcius."))

(defn send-mail []
  (.then (GET WEATHER_API)
         (fn [response]
           (let [
                 temps (->>
                        response
                        :data
                        (map :temp)
                        (drop 9)
                        (take 10))
                 total (apply + temps)
                 avg (/ total (count temps))]
             (when (> avg 20)
               (.sendMail transporter
                          (clj->js
                           {:from "michielborkent@gmail.com"
                            :to MAIL_TO
                            :subject MAIL_SUBJECT
                            :text (str/replace MAIL_BODY #"\{\{avg\}\}"
                                               (gstring/format "%.1f" avg))})))))))

(let [{:keys [:options :summary]}
      (parse-opts (or *command-line-args*
                      (drop 2 js/process.argv)) cli-options)]
  (cond
    (:mail options) (send-mail)
    ;; (:develop options) (dev!)
    :else (println summary)))
