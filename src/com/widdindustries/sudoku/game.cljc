(ns com.widdindustries.sudoku.game
  (:require [com.widdindustries.sudoku.board :as b]
            [medley.core :as m]
            [clojure.set :as set]
            [sc.api]))

(def opts (-> (range 1 10) set))

(defn solution [cell] (:solution cell))

(def orders
  {:row b/row
   :col b/col
   :box b/box})

(defn open-options [board cell]
  (when-not (solution cell)
    (let [solutions (->> orders
                         (mapcat (fn [[order-name f]]
                                   ;(sc.api/defsc 1)
                                   (let [order (f board cell)
                                         solutions (keep solution order)
                                         groups (keep (fn [cell]
                                                        (or
                                                          (and (= order-name (:group-order cell))
                                                            (:group cell)) [])) order)]
                                     (apply concat solutions groups))))
                         set)]
      (set/difference opts solutions))))

#_(defn final-solution [board cell]
    (let [open-options (open-options board cell)]
      (when (= 1 (count open-options))
        (first open-options))))

(defn sweep-solutions [board]
  (reduce
    (fn [r n]
      (let [cell (get r n)]
        (if (solution cell)
          r
          (let [opts (open-options r cell)]
            (if (= 1 (count opts))
              (reduced (assoc r n (assoc cell :solution (first opts))))
              (assoc r n (assoc cell :opts opts)))))))
    board
    (keys board))) ;

(defn solved? [board]
  (every? solution (vals board)))

(defn eliminate [order cell]
  (let [order-opts (->> order
                        (mapcat (fn [c] (if (solution c)
                                          [(solution c)]
                                          (:opts c))))
                        set)
        d (set/difference (:opts cell) order-opts)]
    (when (= 1 (count d))
      (first d))))

(defn order-eliminate [board]
  (reduce
    (fn [r cell-idx]
      (let [b2 (reduce
                 (fn [board f]
                   (let [{:keys [opts] :as cell} (get board cell-idx)
                         order (f board cell)]
                     (if-let [solution (eliminate order cell)]
                       (reduced (assoc board cell-idx (merge cell-idx
                                                        {:solution solution})))
                       board)))
                 r
                 [b/row b/col b/box])]
        (if (not= r b2)
          (reduced b2)
          r)))
    board 
    (keys board)))

(defn group-match [board cell-idx]
  (comment (def f b/row)
    (def cell cell) (def board board) ; (def opts opts)
    )
  (reduce
    (fn [board [order-name f]]
      (let [{:keys [opts] :as cell} (get board cell-idx)]
        (if-let [group (seq (filter (fn [c] (= opts (:opts c))) (f board cell)))]
          (if (= (count opts) (count (conj group cell)))
            (let [new-cells (map (fn [cell]
                                   (assoc cell :group opts :group-order order-name))
                              (conj group cell))]
              (reduce
                (fn [r n]
                  (assoc r (b/idx n) n))
                board
                new-cells))
            board)
          board)))
    board
    orders))

(defn next-board [board]
  (reduce 
    (fn [r n]
      (let [b' (n r)]
        (if (not= b' r)
          (reduced b')
          r)))
    board
    [sweep-solutions order-eliminate]))

(defn sweep [board]
  (loop [states [board]]
    (let [current-board (last states)]
      (if (solved? current-board)
        states
        (let [next-board (next-board current-board)]
          (if (= next-board current-board)
            states
            (recur (conj states next-board))))))))


(comment

  (require 'starting-boards)
  (reset! state starting-boards/moderate)
  (def s (sweep starting-boards/moderate))
  (-> s last solved?)
  )