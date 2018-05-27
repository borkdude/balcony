(require 'lumo.build.api)

(lumo.build.api/build "src"
                      {:target :nodejs
                       :main 'balcony.core
                       :optimizations :simple
                       :output-to "out/main.js"
                       :elide-asserts true
                       :static-fns true
                       :fn-invoke-direct true})
