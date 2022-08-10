(ns com.widdindustries.sudoku.board
  (:require [medley.core :as m]))

(defn x [cell] (:x cell))
(defn y [cell] (:y cell))

(defn idx [s] (select-keys s [:x :y]))

(def board
  (->> (for [x (range 9)
             y (range 9)]
         {:x x :y y})
       (m/index-by idx)))

(defn f-squares [board fs]
  (reduce
    (fn [r n]
      (update r (idx n) (fn [cell] ((:f n) r cell))))
    board
    fs))

(defn line [board axis cell]
  (->>
    board
    (m/filter-keys #(and (= (axis cell) (axis %))
                      (not= (idx %) (idx cell))))
    (vals)))

(defn row [board cell] (line board x cell))
(defn col [board cell] (line board y cell))

(def a->box
  {0 0
   1 0
   2 0
   3 3
   4 3
   5 3
   6 6
   7 6
   8 6})

(defn box [board cell]
  (let [x0 (-> (x cell) a->box)
        y0 (-> (y cell) a->box)
        idxs (for [x' (range x0 (+ x0 3))
                   y' (range y0 (+ y0 3))
                   :when (or (not= (x cell) x') (not= (y cell) y'))]
               {:x x' :y y'})]
    (vals (select-keys board idxs))))

(defn position [board]
  (->>
    (for [y (range 9)
          x (range 9)]
      (let [cell (get board {:x x :y y})]
        (or (:solution cell) "-")))
    flatten
    (apply str)))

(comment
  (count (box board {:x 0 :y 0}))

  (->
    (f-squares board [{:x 0 :y 0 :f (fn [_ s] (assoc s :solution 3))}])
    (get {:x 0 :y 0}))

  (row board {:x 2 :y 3})
  (col board {:x 2 :y 3})
  )
