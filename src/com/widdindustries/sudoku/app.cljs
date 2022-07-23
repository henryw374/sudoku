(ns com.widdindustries.sudoku.app
  (:require [reagent.dom :as rdom]
            [com.widdindustries.sudoku.view :as view]))

(defn app-container [id]
  (js/document.getElementById id))

(defn mount-components []
  (when-let [container (app-container "app")]
    (rdom/render [view/app-view] container)))

(defn init! []
  (js/console.log "Initializing")
  (mount-components))

(defn ^:dev/after-load on-figwheel-reload []
  (.clear js/console)
  ;(re-frame.core/clear-subscription-cache!)
  (rdom/force-update-all))

(.addEventListener
  js/window
  "load"
  (fn []
    (init!)))


