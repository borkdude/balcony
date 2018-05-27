(require 'lumo.build.api)

(lumo.build.api/build "src"
                      {:target :nodejs
                       :main 'balcony.core
                       :optimizations :advanced
                       :output-to "out/main.js"})
