(ns minesweeper.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [minesweeper.events :as events]
   [minesweeper.views :as views]
   [minesweeper.config :as config]))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/app]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
