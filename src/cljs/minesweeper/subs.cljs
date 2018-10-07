(ns minesweeper.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::minefield
 (fn [db]
   (:minefield db)))
