(ns com.widdindustries.sudoku.view
  (:require [com.widdindustries.sudoku.board :as b]
            [com.widdindustries.sudoku.game :as game]
            [reagent.core :as r]))


(defn board [state]
  (r/with-let [edit (r/atom nil)]
    (def state state)
    (println @edit)
    [:table {:style {:border          "solid"
                     :border-collapse "collapse"
                     :border-spacing  "0"
                     :font-size       "40px"}}
     [:tbody
      (doall
        (for [y (range 9)]
          ^{:key y}
          [:tr
           (doall
             (for [x (range 9)]
               ^{:key x}
               (let [cell (get @state {:x x :y y})]
                 [:td
                  (merge
                    {:title (:opts cell)
                     :style (merge {:height "50px" :width "50px" :border "solid"}
                              (when (#{2 5 8} y)
                                {:border-bottom "4px solid black"})
                              (when
                                (#{2 5 8} x)
                                {:border-right "4px solid black"}))}
                    ;(when-not (and @edit (= [x y] (:coord edit))))
                    {:on-click (fn []
                                 (println "clicked" [x y] (get @state {:x x :y y}))
                                 (reset! edit {:coord [x y]}))})
                  (if (and @edit (= [x y] (:coord @edit)))
                    [:input {:type      "number"
                             :autoFocus true
                             :style     {:height "100%" :width "100%"}
                             :min       1 :max 9
                             :value     (-> (get @state {:x x :y y}) :solution)
                             ;:on-blur (reset! edit nil)
                             :on-change (fn [e]
                                          (let [v (.. e -target -value)]
                                            (def v v)
                                            (when-let [n (and v (js/parseInt v))]
                                              (swap! state update {:x x :y y}
                                                (fn [c]
                                                  (assoc c :solution n)))
                                              (reset! edit nil))))}]
                    [:span 
                     (or (-> cell :solution)
                       (-> cell :group str))])])))]
          ))
      ]]))

(defn app-view []
  (r/with-let [state (r/atom b/board)]
    (js/console.log @state)
    [:div.parent.blue {:style {:display     "grid"
                               :place-items "center"}}
     [:div.box.coral
      [:h1 "Sudoku Solver"]
      [:br]
      [board state]
      [:div
       ;[:button {} "prev"]
       [:button {:on-click (fn []
                             (let [b (game/sweep @state)]
                               (if (not= b @state)
                                 (reset! state b)
                                 (js/info "cannot make progress, sorry")))
                             )} "step"]] ;;
      [:br]
      [:div
       [:button {:on-click (fn [] (reset! state b/board))} "clear"]]
      ]]))

(comment
  (reset! state starting-boards/moderate)
  (reset! state starting-boards/easy)
  (game/sweep @state)
  (def x (game/open-options @state {:x 7 :y 8})
    )
  (get @state {:x 8 :y 4})
  (get @state {:x 5 :y 8})
  (game/eliminate
    (b/box @state {:x 5 :y 8})
    (get @state {:x 5 :y 8}))
  (game/group-match x)
  

  )