(defproject grdji "0.1.0-SNAPSHOT"
  :description "Damion Junk I Scratchpad"
  :url "https://"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]

                 [nrepl "0.8.3"]
                 [cheshire "5.10.0"]
                 [org.clojure/tools.cli "1.0.194"]
                 [org.clojure/tools.logging "1.1.0"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [cprop "0.1.17"]
                 [mount "0.1.16"]

                 ;; Date/time
                 [tick "0.4.29-alpha"]
                 ]

  :exclusions
  [;; Exclude transitive dependencies on all other logging
   ;; implementations, including other SLF4J bridges.
   log4j
   org.slf4j/simple
   org.slf4j/slf4j-jcl
   org.slf4j/slf4j-nop
   org.slf4j/slf4j-log4j12
   org.slf4j/slf4j-log4j13
   ]

  :profiles {:dev {:plugins [[com.jakemccrary/lein-test-refresh "0.24.1"]
                             [jonase/eastwood "0.3.7"]
                             ]}}

  :min-lein-version "2.5.0"
  :resource-paths ["resources"]
  :source-paths ["src" ]
  :test-paths ["test"]
  :target-path "target/%s/"
  :main ^:skip-aot grdji.core

  )
