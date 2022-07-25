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
                                   ;(sc.api/defsc [3 -1])
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
      (update r n (fn [cell]
                    (if (solution cell)
                      cell
                      (let [opts (open-options r cell)]
                        (if (= 1 (count opts))
                          (assoc cell :solution (first opts))
                          (assoc cell :opts opts)))))))
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

(defn order-eliminate [board cell-idx]
  (reduce
    (fn [board f]
      (let [{:keys [opts] :as cell} (get board cell-idx)
            order (f board cell)]
        (if-let [solution (eliminate order cell)]
          (assoc board cell-idx (merge cell-idx
                                  {:solution solution}))
          board)))
    board
    [b/row b/col b/box]))

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

(defn sweep [board]
  (reduce
    (fn [board n]
      (cond 
        (solved? board) (reduced board)
        (solution (get board n)) board
        :else (-> board
                  (sweep-solutions)
                  (order-eliminate n)
                  ;(group-match n)
                  )))
    board
    (keys board)))


