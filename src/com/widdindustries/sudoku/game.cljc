(ns com.widdindustries.sudoku.game
  (:require [com.widdindustries.sudoku.board :as b]
            [medley.core :as m]
            [clojure.set :as set]))

(def opts (-> (range 1 10) set))

(defn solution [cell] (:solution cell))

(defn open-options [board cell]
  (when-not (solution cell)
    (let [solutions (->> (concat (b/row board cell)
                            (b/col board cell)
                            (b/box board cell))
                          (mapcat (fn [c] (if (solution c)
                                          [(solution c)]
                                          (:group c)))))]
      (disj opts solutions))))

(defn final-solution [board cell]
  (let [open-options (open-options board cell)]
    (when (= 1 (count open-options))
      (first open-options))))

(defn sweep-solutions [board]
  (reduce
    (fn [r n]
      (if (solution n)
        board
        (update r n (fn [cell]
                      (let [opts (open-options r cell)]
                        (cond-> (assoc cell :opts opts)
                          (= 1 (count opts)) (assoc :solution (first opts))))))))
    board
    (keys board)))

(defn solved? [board]
  (every? solution (vals board)))

(def max-sweeps 500)

(defn group-match [board {:keys [opts] :as cell}]
  (reduce
    (fn [board f]
      (if-let [group (seq (filter (fn [c] (= opts (:opts c))) (f board cell)))]
        (let [new-cells (map (fn [cell]
                               (assoc cell :group opts))
                          (conj group cell))]
          (reduce
            (fn [r n]
              (assoc r (b/idx n) n))
            board
            new-cells))
        board))
    board
    [b/row b/col b/box]))

(defn sweep-groups [board]
  (reduce
    (fn [r n]
      (if (solution n)
        board
        (group-match r n)))
    board
    (keys board)))

(defn sweep [board]
  (let [board (sweep-solutions board)]
    (if (solved? board)
      board 
      (sweep-groups board))))

(defn solve [board]
  (loop [remaining max-sweeps
         steps [board]]
    (let [[board] (last steps)]
      (cond
        (solved? board) board
        (zero? remaining) :fail
        :else (recur (conj steps (sweep board)) 
                (dec remaining))))))


