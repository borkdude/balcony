(ns balcony.env
  (:require [goog.object :as gobject]))

(defmacro defenvs
  "Defines vars with same name as environment variables"
  [& variables]
  (let [defs (for [variable variables]
               `(def ~variable
                  (gobject/get js/process.env
                              (str (quote ~variable)))))]
    `(do ~@defs)))
