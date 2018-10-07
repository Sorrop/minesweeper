(ns minesweeper.views
  (:require
   [re-frame.core :as re-frame]
   [minesweeper.subs :as subs]
   [minesweeper.events :as events]
   [minesweeper.utils :refer [clog]]))


(defn render-tile [tile]
  (let [state (get tile :state)
        content (:content tile)]
    (if (= "mine" (:content tile))
      [:img {:class state :src (if (not= state "marked")
                                 "images/bomb1.svg"
                                 "images/golf_flag.png")}]
      (if (= state "marked")
        [:img {:src "images/golf_flag.png"}]
        [:text {:class state}
         (when-not (= 0 content)
           content)]))))

(defn main-panel []
  (let [minefield (re-frame/subscribe [::subs/minefield])]
    (when (not-empty @minefield)
      [:div.minefield
       (doall
        (for [x (range (count @minefield))
              y (range (count (get @minefield x)))
              :let [tile (get-in @minefield [x y])
                    t-state (:state tile)]]
          [:div.tile {:key [x y]
                      :class (when (= t-state "stepped")
                               t-state)
                      :on-click (fn [e]
                                  (when-not (= t-state "stepped")
                                    (re-frame/dispatch [::events/sweep [x y]])))
                      :on-context-menu (fn [e]
                                         (.preventDefault e)
                                         (when-not (= t-state "stepped")
                                           (re-frame/dispatch [::events/mark-tile [x y]])))
                      :on-mouse-down (fn [e]
                                       (let [event (.-nativeEvent e)
                                             button (.-button event)]
                                         (when (= button 1)
                                           (re-frame/dispatch [::events/trust-mark [x y]]))))}
           (render-tile tile)]))
       ])))

(defn score-view []
  (let [score (re-frame/subscribe [::subs/score])
        game-state (re-frame/subscribe [::subs/game-state])
        {:keys [marked mines]} @score]
    (when (not= @game-state "")
      [:ul
       [:li "Mines: " mines]
       [:li "Marked: " marked]])))

(defn app []
  [:div.container
   [:div.buttons
    [:button {:on-click (fn [_]
                          (re-frame/dispatch [::events/init-game]))} "New Game"]
    [:button {:on-click (fn [_]
                          (re-frame/dispatch [::events/reveal]))} "Reveal"]]
   (main-panel)
   (score-view)])
