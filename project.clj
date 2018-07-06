(defproject forridan "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [org.clojure/core.async "0.4.474"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [org.clojure/tools.deps.alpha "0.3.260"]
                 [funcool/lentes "1.2.0"]]
  :profiles {:dev {:dependencies [[lein-doo "0.1.8"]]
                   :plugins [[lein-doo "0.1.8"]]}}
  :cljsbuild
  {:builds [{:id "test"
             :source-paths ["src" "test"]
             :compiler {:output-to "resources/public/js/doo-tests.js"
                        :main forridan.doo-runner
                        :optimizations :none}}]})

