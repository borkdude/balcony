(ns balcony.core
  (:require
   [cljs.nodejs :as node :refer [require]]
   [clojure.string :as str]
   [clojure.tools.cli :refer [parse-opts]]
   [goog.string :as gstring]
   [goog.string.format] ;; don't remove
   [got]
   [moment]
   [nodemailer :as nm])
  (:require-macros [balcony.env :refer [defenvs]]))

(node/enable-util-print!)

(defenvs
  WEATHER_API_KEY
  MAIL_USER
  MAIL_PASS
  MAIL_TO
  MAIL_SUBJECT
  MAIL_BODY)

(def transporter (nm/createTransport
                  (clj->js {:service "gmail"
                            :auth
                            {:user MAIL_USER
                             :pass MAIL_PASS}})))

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
                  WEATHER_API_KEY))

(def MAIL_TO
  (or MAIL_TO "michielborkent@gmail.com"))

(def MAIL_SUBJECT
  (or MAIL_SUBJECT "You need to water the balcony today."))

(def MAIL_BODY
  (or MAIL_BODY "Please water the balcony tonight. The average temperature between 9AM and 7PM was {{avg}} degrees Celcius."))

(defn mail-options [avg]
  (clj->js
   {:from "michielborkent@gmail.com"
    :to MAIL_TO
    :subject MAIL_SUBJECT
    :text
    (str/replace MAIL_BODY
                 #"\{\{avg\}\}"
                 (gstring/format "%.1f" avg))}))

(defn send-mail []
  (.then (got WEATHER_API #js {:json true})
         #(let [body (js->clj (.-body %) :keywordize-keys true)
               temps (->>
                     body
                     :data
                     (map :temp)
                     (drop 9)
                     (take 10))
              total (apply + temps)
              avg (/ total (count temps))]
           (when (> avg 20)
             (.sendMail transporter
                        (mail-options avg))))))

(defonce main
  #(let [{:keys [:options :summary]}
         (parse-opts (or *command-line-args*
                         (drop 2 js/process.argv)) cli-options)]
     (cond
       (:mail options) (send-mail)
       ;; (:develop options) (dev!)
       :else (println summary))))

(set! *main-cli-fn* main)
