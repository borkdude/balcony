#!/usr/bin/env bash

":";cheshire='cheshire {:mvn/version "5.8.0"}'
":";cider='cider/cider-nrepl {:mvn/version "0.17.0-SNAPSHOT"}'
":";clj_http='clj-http {:mvn/version "3.9.0"}'
":";nrepl='org.clojure/tools.nrepl {:mvn/version "0.2.12"}'
":";postal='com.draines/postal {:mvn/version "2.0.2"}'
":";tools_cli='org.clojure/tools.cli {:mvn/version "0.3.7"}'

"exec" "clj" "-Sdeps" "{:deps {$cheshire $cider $clj_http $postal $tools_cli}}" "$0" "$@"

(ns balcony.core
  (:require
   [clojure.string :as str]
   [clojure.tools.cli :refer [parse-opts]])
  (:import [java.time LocalDateTime]
           [java.time.format DateTimeFormatter]))

(defmacro jit-require
  [ns alias vars]
  (let [target-ns (symbol (str "lazy." ns))]
    `(let [al# (quote ~alias)
           ns# (quote ~ns)
           tns# (quote ~target-ns)
           vs# (quote ~vars)]
       (ns-unalias *ns* al#)
       (create-ns tns#)
       (alias al# tns#)
       (doseq [v# vs#]
         (intern tns# v#))
       (intern tns# (symbol "require!")
               #(do
                  (require ns#)
                  (doseq [v# vs#]
                    (intern tns# v# (ns-resolve ns# v#))))))))

;; libraries are loaded just in time for faster startup up time

(jit-require cider.nrepl cider [cider-nrepl-handler])
(jit-require clojure.tools.nrepl.server nrepl [start-server])
(jit-require clj-http.client http [get])
(jit-require cheshire.core json [parse-string])
(jit-require postal.core email [send-message])

;; some helper macros

(defmacro defconst
  [const-name const-val]
  `(def
     ~(with-meta const-name
        (assoc (meta const-name) :const true))
     ~const-val))

(defmacro defenvs
  "Defines vars with same name as environment variables"
  [& variables]
  (cons 'do
        (for [variable variables]
          `(defconst ~variable
             (System/getenv (str (quote ~variable)))))))

;; constants

(defenvs
  WEATHER_API_KEY
  MAIL_USER
  MAIL_PASS
  MAIL_TO
  MAIL_SUBJECT
  MAIL_BODY)

;; today and tomorrow's string representation

(def dtf (DateTimeFormatter/ofPattern "yyyy-MM-dd"))
(def ^LocalDateTime now (LocalDateTime/now))

(defconst TODAY
  (.format now dtf))

(defconst TOMORROW
  (-> now
      (.plusDays 1)
      (.format dtf)))

(defconst WEATHER_API
  (format "https://api.weatherbit.io/v2.0/history/hourly?city=Amersfoort,NL&start_date=%s&end_date=%s&key=%s"
          TODAY
          TOMORROW
          WEATHER_API_KEY))

(defconst MAIL_TO
  (str/split
   (or MAIL_TO "michielborkent@gmail.com")
   #",\s*"))

(defconst MAIL_SUBJECT
  (or MAIL_SUBJECT "You need to water the balcony today."))

(defconst MAIL_BODY
  (or MAIL_BODY "Please water the balcony tonight. The average temperature between 9AM and 7PM was {{avg}} degrees Celcius."))

(defn send-mail!
  []
  (http/require!)
  (json/require!)
  (email/require!)
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
                           :body (str/replace MAIL_BODY #"\{\{avg\}\}"
                                              (format "%.1f" avg))}))))

(defn dev! []
  (cider/require!)
  (nrepl/require!)
  (nrepl/start-server :port 7888 :handler cider/cider-nrepl-handler)
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

;;;; Scratch

(comment
  (send-mail!)
  )
