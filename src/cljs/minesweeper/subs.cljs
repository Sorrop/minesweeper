(ns minesweeper.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::mines
 (fn [db]
   (:mines db)))
