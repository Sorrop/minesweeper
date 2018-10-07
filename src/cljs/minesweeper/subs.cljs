(ns minesweeper.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::minefield
 (fn [db]
   (:minefield db)))

(re-frame/reg-sub
 ::score
 (fn [db]
   (:score db)))

(re-frame/reg-sub
 ::game-state
 (fn [db]
   (:game-state db)))
