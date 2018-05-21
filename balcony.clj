#!/usr/bin/env bash
(> "/dev/null");cheshire="cheshire {:mvn/version \"5.8.0\"}"
(> "/dev/null");cider="cider/cider-nrepl {:mvn/version \"0.17.0-SNAPSHOT\"}"
(> "/dev/null");clj_http="clj-http {:mvn/version \"3.9.0\"}"
(> "/dev/null");nrepl="org.clojure/tools.nrepl {:mvn/version \"0.2.12\"}"
(> "/dev/null");postal="com.draines/postal {:mvn/version \"2.0.2\"}"
(> "/dev/null");tools_cli="org.clojure/tools.cli {:mvn/version \"0.3.7\"}"
"exec" "clj" "-Sdeps" "{:deps {$cheshire $cider $clj_http $jdbc $h2 $postal $tools_cli}}" "$0" "$@"

(ns balcony.core
  (:require
   [balcony.dev :as dev]
   [balcony.email :as email]
   [balcony.http :as http]
   [balcony.json :as json]
   [clojure.string :as str]
   [clojure.tools.cli :refer [parse-opts]])
  (:import [java.time LocalDateTime]
           [java.time.format DateTimeFormatter]
           [java.text DecimalFormat]))

;; proxy namespaces, libraries are loaded lazily for faster startup up time
(ns balcony.dev)
(declare start-server cider-nrepl-handler)

(ns balcony.http
  (:refer-clojure :exclude [get update]))
(declare get)

(ns balcony.json)
(declare parse-string)

(ns balcony.email)
(declare send-message)

(in-ns 'balcony.core)

(defmacro defenv!
  "Defines vars with same name as environment variables"
  [& variables]
  (cons 'do
        (for [variable variables]
          `(def ~variable
             (System/getenv (str (quote ~variable)))))))

(defmacro resolve*
  "Requires namespace and interns vars to target-ns."
  [ns target-ns & vars]
  (cons 'do
        (cons (list 'require ns)
              (for [v vars]
                `(intern ~target-ns ~v (ns-resolve ~ns ~v))))))

;; constants

(defenv!
  WEATHER_API_KEY
  MAIL_USER
  MAIL_PASS
  MAIL_TO
  MAIL_SUBJECT
  MAIL_BODY)

(def dtf (DateTimeFormatter/ofPattern "yyyy-MM-dd"))
(def now (LocalDateTime/now))
(def TODAY
  (.format ^LocalDateTime now dtf))
(def TOMORROW
  (-> ^LocalDateTime now
      (.plusDays 1)
      (.format dtf)))

(def WEATHER_API
  (format "https://api.weatherbit.io/v2.0/history/hourly?city=Amersfoort,NL&start_date=%s&end_date=%s&key=%s"
          TODAY
          TOMORROW
          WEATHER_API_KEY))

(def MAIL_TO (str/split
              (or MAIL_TO "michielborkent@gmail.com")
              #",\s*"))
(def MAIL_SUBJECT (or MAIL_SUBJECT "You need to water the balcony today."))
(def MAIL_BODY (or MAIL_BODY "Please water the balcony tonight. The average temperature between 9AM and 7PM was {{avg}} degrees Celcius."))

(defn send-mail!
  []
  (resolve* 'postal.core 'balcony.email 'send-message)
  (resolve* 'clj-http.client 'balcony.http 'get)
  (resolve* 'cheshire.core 'balcony.json 'parse-string)
  (let [response
        (-> (http/get WEATHER_API)
            :body
            (json/parse-string true))
        temps (->>
               response
               :data
               (map :temp)
               (drop 9)
               (take 10))
        total (apply + temps)
        avg (/ total (count temps))]
    (when (> avg 20)
      (email/send-message {:host "smtp.gmail.com"
                           :user MAIL_USER
                           :pass MAIL_PASS
                           :ssl true}
                          {:from "michielborkent@gmail.com"
                           :to MAIL_TO
                           :subject MAIL_SUBJECT
                           :body (str/replace MAIL_BODY #"\{\{avg\}\}" (str avg))}))))

(defn dev! []
  (resolve* 'clojure.tools.nrepl.server 'balcony.dev 'start-server)
  (resolve* 'cider.nrepl 'balcony.dev 'cider-nrepl-handler)
  (dev/start-server :port 7888 :handler dev/cider-nrepl-handler)
  (println "Started nREPL on port 7888"))

(def cli-options
  [["-d" "--develop" "Starts nREPL"]
   ["-m" "--mail" "Mail if average temperature of today is above threshold"]])

(defonce init
  (let [opts (:options
              (parse-opts *command-line-args* cli-options))]
    (cond
      (:mail opts) (send-mail!)
      (:develop opts) (dev!))))
