(ns com.widdindustries.sudoku.view
  (:require [com.widdindustries.sudoku.board :as b]
            [starting-boards]
            [com.widdindustries.sudoku.game :as game]
            [reagent.core :as r]))

(defn board [board on-click edit]
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
             (let [cell (get board {:x x :y y})]
               ^{:key x}
               [:td
                (merge
                  {:style (merge { ;:text-align    "center" ;
                                  ;:vertical-align "middle" ;
                                  :height        "50px" :width "50px" :border "solid"}
                            (when (#{2 5 8} y)
                              {:border-bottom "4px solid black"})
                            (when
                              (#{2 5 8} x)
                              {:border-right "4px solid black"}))}
                  {:on-click (fn [] (when on-click (on-click x y)))})
                (if-let [edit (edit x y)]
                  [edit]
                  [:div {:style {:display "flex"
                                 :flex-direction "column"
                                 :justify-content "space-between"
                                 }}
                   [:div {:style {:font-size       "10px"}} (:opts cell)]
                   [:span
                    (or (-> cell :solution)
                      (-> cell :group str))]])])))]))]])

(defonce entry-state (r/atom b/board))

(defn entry-board [solution]
  (r/with-let [edit (r/atom nil)]
    (js/console.log @entry-state)
    [:div
     [:h2 "Put the starting numbers into the grid below. Click 'Solve' when ready"]
     [board @entry-state
      (fn [x y]
        (println "clicked" [x y] (get @entry-state {:x x :y y}))
        (reset! edit {:coord [x y]}))
      (fn [x y]
        (when (and @edit (= [x y] (:coord @edit)))
          (fn []
            [:input {:type      "number"
                     :autoFocus true
                     :style     {:height "100%" :width "100%"}
                     :min       1 :max 9
                     :value     (-> (get @entry-state {:x x :y y}) :solution)
                     :on-change (fn [e]
                                  (let [v (.. e -target -value)]
                                    (def v v)
                                    (when-let [n (and v (js/parseInt v))]
                                      (swap! entry-state update {:x x :y y}
                                        (fn [c]
                                          (assoc c :solution n)))
                                      (reset! edit nil))))}])))]
     [:div
      [:button {:on-click (fn []
                            (def solution solution)
                            (let [states (game/sweep @entry-state)]
                              (reset! solution states)))} 
       "Solve"]] 
     [:br]
     [:div
      [:button {:on-click (fn [] (reset! entry-state b/board))} "clear"]]]))

(defn solution-view [solution]
  (r/with-let [idx (r/atom 0)]
    (def b (get @solution @idx))
    [:div  
     (when-not (game/solved? (last @solution))
       [:span "No solution could be found :("])
     [board (get @solution @idx) (fn []) (fn [])]
     [:div [:span
            [:button {:disabled (zero? @idx)
                      :on-click (fn [] (swap! idx dec))} "Previous"]
            [:button {:disabled (= (count @solution) (inc @idx))
                      :on-click (fn [] (swap! idx inc))} "Next"]
            ]]
     [:div [:button {:on-click (fn []
                                 (reset! solution nil))} "Reset"]]
     ]
    ))

(defn app-view []
  (r/with-let [solution (r/atom nil)]
    [:div.parent.blue {:style {:display     "grid"
                               :place-items "center"}}
     [:div.box.coral
      [:h1 "Jefdoku"]
      (if @solution
        [solution-view solution]
        [entry-board solution])]]))

(comment
  (require 'starting-boards)
  (require 'stuck-ats)
  (reset! entry-state starting-boards/jeff-defeater)
  (reset! entry-state starting-boards/moderate)
  (reset! entry-state stuck-ats/a)
  (def s (game/sweep starting-boards/moderate))
  
  (game/sweep @state)
  (def x (game/open-options @state {:x 7 :y 8})
    )
  (get @state {:x 8 :y 4})
  (get @state {:x 5 :y 8})
  (game/eliminate
    (b/box @state {:x 5 :y 8})
    (get @state {:x 5 :y 8}))
  (game/group-match x)
  @entry-state
  (println (b/position b))
  

  )