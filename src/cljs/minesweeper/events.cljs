(ns minesweeper.events
  (:require
   [re-frame.core :as re-frame]
   [minesweeper.db :as db]
   [minesweeper.utils :refer [clog]]
   [minesweeper.minefield :refer [init-mines sweep-tile
                                  reveal-tiles mark-tile
                                  trust-tile find-mines
                                  marked-inc-dec
                                  update-game-state
                                  reveal-mines
                                  win-condition?]]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::init-game
 (fn [db _]
   (let [difficulty (:difficulty db)
         minefield (init-mines difficulty)
         mines (find-mines minefield)]
     (assoc db
            :minefield minefield
            :init-difficulty difficulty 
            :mines mines
            :score {:mines (count mines)
                    :marked 0}
            :game-state "playing"))))

(re-frame/reg-event-db
 ::sweep
 (fn [db [_ id]]
   (let [old (get db :minefield)
         old-game-state (get db :game-state)
         new (sweep-tile old id)
         new-game-state (update-game-state old-game-state
                                           new
                                           id
                                           true)
         new (if (= "lose" new-game-state)
               (reveal-mines new (get db :mines))
               new)]
     (assoc db
            :minefield new
            :game-state new-game-state))))

(re-frame/reg-event-db
 ::reveal
 (fn [db _]
   (let [game-state (get db :game-state)]
     (if-not (#{"win" "lose"} game-state)
       (let [field (get db :minefield)
             mines (get db :mines)
             revealed (reveal-tiles field)
             win? (win-condition? revealed mines)]
         (assoc db
                :minefield revealed
                :game-state (if win? "win" "lose")))
       db))))

(re-frame/reg-event-db
 ::mark-tile
 (fn [db [_ id]]
   (let [old (get db :minefield)
         new (mark-tile old id)]
     (-> (assoc db :minefield new)
         (update-in [:score :marked]
                    marked-inc-dec new id)))))

(re-frame/reg-event-db
 ::trust-mark
 (fn [db [_ id]]
   (let [old (get db :minefield)
         old-game-state (get db :game-state)
         new (trust-tile old id)
         new-game-state (update-game-state old-game-state
                                           new
                                           id
                                           false)
         new (if (= "lose" new-game-state)
               (reveal-mines new (get db :mines))
               new)]
     (assoc db
            :minefield new
            :game-state new-game-state))))

(re-frame/reg-event-db
 ::set-difficulty
 (fn [db [_ difficulty]]
   (assoc db :difficulty difficulty)))
