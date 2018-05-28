#!/usr/bin/env bash

":";tools_cli='org.clojure/tools.cli {:mvn/version "0.3.7"}'
":";momentjs='org.webjars.npm/moment {:mvn/version "2.22.1"}'
":";src=$(clojure -Srepro -Spath -Sdeps "{:deps {$tools_cli $moment}}")

"exec" "lumo" "-K" "-c" "$src" "$0" "$@"

(ns balcony.core
  (:require [moment]))

(def now (moment))
(def date-format "YYYY-MM-DD")
(def today (.format now date-format))
(def tomorrow (.format (.add now 1 "days") date-format))

(println today tomorrow)
