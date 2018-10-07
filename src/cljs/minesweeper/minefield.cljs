(ns minesweeper.minefield
  (:require [minesweeper.utils :refer [clog]]))


(defn directions [[x y]]
  {:n [(dec x) y]
   :w [x (dec y)]
   :s [(inc x) y]
   :e [x (inc y)]
   :sw [(inc x) (dec y)]
   :se [(inc x) (inc y)]
   :ne [(dec x) (inc y)]
   :nw [(dec x) (dec y)]})


(defn produce-n-items [f n]
  (->> (repeatedly f)
       (take n)
       (into [])))

(defn retrieve-neighbour-ids [[x y] mines & {:keys [no-mines?] :or [false]}]
  (let [n-ids (directions [x y])]
    (for [n-id (vals n-ids)
          :let [tile (get-in mines n-id)]
          :when (if no-mines?
                  (and tile (not= (:content tile) "mine"))
                  tile)]
      n-id)))

(defn find-mines [field]
  (into #{}
        (for [x (range (count field))
              y (range (count (get field x)))
              :when (= (:content (get-in field [x y]))
                       "mine")]
          [x y])))

(defn init-mines []
  (let [row-init (fn [] {:content (rand-nth [0 0 0 0 0 0 0 0 0 0 0 "mine"])
                         :state "hidden"})
        initial (produce-n-items #(produce-n-items row-init 6) 6)
        mines-ids (find-mines initial)
        to-update (mapcat #(retrieve-neighbour-ids % initial :no-mines? true)
                          mines-ids)]
    (reduce (fn [acc tile-id]
              (update-in acc (conj tile-id :content) inc))
            initial
            to-update)))

(defn tiles-to-sweep [field id]
  (loop [to-check [id]
         checked #{}
         out []]
    (if (not-empty to-check)
      (let [tile-id (->> to-check
                         first)
            content (get-in field (conj tile-id :content))
            neighbour-ids (when (= content 0)
                            (retrieve-neighbour-ids tile-id field))]
        (if (not= content 0)
          (recur (rest to-check)
                 (conj checked tile-id)
                 (conj out tile-id))
          (recur (concat (rest to-check)
                         (filter #(not (checked %)) neighbour-ids))
                 (conj checked tile-id)
                 (concat out (conj neighbour-ids tile-id)))))
      out)))

(defn sweep-tile [field id]
  (let [{:keys [content]} (get-in field id)
        to-update (tiles-to-sweep field id)]
    (reduce (fn [acc id]
              (let [current (get-in acc (conj id :state))]
                (if (not= current "marked")
                  (assoc-in acc (conj id :state) "stepped")
                  acc)))
            field
            to-update)))

(defn reveal-tiles [field]
  (->> (for [x (range (count field))
             y (range (count (get field x)))]
         [x y])
       (reduce (fn [acc id]
                 (assoc-in acc (conj id :state) "stepped"))
               field)))

(defn mark-tile [field id]
  (let [current (get-in field (conj id :state))]
    (assoc-in field (conj id :state) (if (= current "marked")
                                       "hidden"
                                       "marked"))))

(defn do-trust-tile [field neighbours]
  (let [to-update (mapcat #(tiles-to-sweep field %)
                          neighbours)]
    (reduce (fn [acc id]
              (let [current (get-in acc (conj id :state))]
                (if (not= current "marked")
                  (assoc-in acc (conj id :state) "stepped")
                  acc)))
            field
            to-update)))

(defn trust-tile [field id]
  (let [{:keys [state content]} (get-in field id)
        neighbours (when (not= state "hidden")
                     (retrieve-neighbour-ids id field))
        marked-neighbours (when (= state "stepped")
                            (count (filter #(= (:state %) "marked")
                                           (map #(get-in field %)
                                                neighbours))))]
    (condp = state
      "hidden" field
      "stepped" (if (= marked-neighbours content)
                  (do-trust-tile field neighbours)
                  field)
      (do-trust-tile field neighbours)
      )))
