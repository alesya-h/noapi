#!/usr/bin/env boot
(set-env! :dependencies '[[org.clojure/clojure "1.7.0"]
                          [adzerk/bootlaces "0.1.11"]
                          [org.clojure/tools.namespace "0.2.11"]
                          [org.clojure/core.match "0.3.0-alpha4"]
                          [org.clojure/data.json "0.2.6"]
                          [com.cognitect/transit-clj "0.8.281"]
                          [ring/ring-core "1.4.0"]]
          :source-paths #{"src"})

(require '[adzerk.bootlaces :refer :all])
(def +version+ "1.0.0")
(bootlaces! +version+)

(task-options! pom {:project     'noapi
                    :version     +version+
                    :description "Library to expose clojure namespaces as HTTP API"
                    :url         "https://github.com/alesguzik/noapi"
                    :scm         {:url "https://github.com/alesguzik/noapi"}
                    :license     {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}})
