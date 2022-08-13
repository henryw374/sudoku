(ns cljs
  (:require [com.widdindustries.tiado-cljs2 :as util]
            [clojure.java.shell :as sh]))

(defn test-watch []
  (util/browser-test-build :watch {}))

(defn app-config []
  (->
    (util/browser-app-config)
    (merge
      {:modules {:main {:entries ['com.widdindustries.sudoku.app]}}})))

(defn app-release []
  ;(sh/sh "cp" "-r" "resources/public/" "docs/") need to change js path
  (util/prod-build
    (-> (app-config)
        (merge {:asset-path "/sudoku/cljs-out"
                :output-dir "docs/cljs-out" })
        (dissoc :devtools))))

(defn app-watch []
  (util/watch (app-config)))


(comment
  (app-release)

  ; start compiling and watching the app
  (app-watch)
  ; visit http://localhost:9000 
  (util/repl)

  ; start up live-compilation of tests
  (test-watch)
  ; run cljs tests, having opened browser at test page (see print output of above "for tests, open...")
  (util/run-tests)
  ; start a cljs repl session in the test build. :cljs/quit to exit
  (util/repl :browser-test-build)

  ; do the release build
  (app-release)

  (util/build-report (app-config) "build-report.html")

  ; you can stop/start etc as required
  (util/stop-server)

  )