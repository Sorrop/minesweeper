(ns minesweeper.events
  (:require
   [re-frame.core :as re-frame]
   [minesweeper.db :as db]
   [minesweeper.utils :refer [clog]]
   [minesweeper.minefield :refer [init-mines sweep-tile
                                  reveal-tiles mark-tile
                                  trust-tile]]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::init-game
 (fn [db _]
   (assoc db :minefield (init-mines))))

(re-frame/reg-event-db
 ::sweep
 (fn [db [_ id]]
   (update db :minefield sweep-tile id)))

(re-frame/reg-event-db
 ::reveal
 (fn [db _]
   (update db :minefield reveal-tiles)))

(re-frame/reg-event-db
 ::mark-tile
 (fn [db [_ id]]
   (update db :minefield mark-tile id)))

(re-frame/reg-event-db
 ::trust-mark
 (fn [db [_ id]]
   (update db :minefield trust-tile id)))
