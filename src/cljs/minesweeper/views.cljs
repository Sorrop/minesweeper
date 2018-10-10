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
  (let [minefield (re-frame/subscribe [::subs/minefield])
        game-state (re-frame/subscribe [::subs/game-state])
        init-difficulty (re-frame/subscribe [::subs/init-difficulty])]
    (when (not-empty @minefield)
      [:div.minefield {:class @init-difficulty}
       (doall
        (for [x (range (count @minefield))
              y (range (count (get @minefield x)))
              :let [tile (get-in @minefield [x y])
                    t-state (:state tile)
                    t-content (:content tile)]]
          [:div.tile {:key [x y]
                      :class (when (= t-state "stepped")
                               (if (= t-content "mine")
                                 (str t-state " " t-content)
                                 t-state))
                      :on-click (fn [e]
                                  (when (= @game-state "playing")
                                    (when-not (= t-state "stepped")
                                      (re-frame/dispatch [::events/sweep [x y]]))))
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
      [:div.score-view
       [:ul
        [:li "Mines: " mines]
        [:li "Marked: " marked]]
       (condp = @game-state
         "lose" [:p "You lost."]
         "win" [:p "You won!"]
         nil)])))

(defn select-difficulty-view []
  (let [difficulty (re-frame/subscribe [::subs/difficulty])]
    (when @difficulty
      [:fieldset
       [:legend "Select difficulty"]
       [:div
        [:input#easy  {:name "difficulty"
                       :type "radio"
                       :value "easy"
                       :checked (= @difficulty "easy")
                       :on-change (fn [_] (re-frame/dispatch [::events/set-difficulty "easy"]))}
         ]
        [:label {:for "easy"} "Easy"]]
       [:div
        [:input#normal {:name "difficulty"
                        :type "radio"
                        :value "normal"
                        :checked (= @difficulty "normal")
                        :on-change (fn [_] (re-frame/dispatch [::events/set-difficulty "normal"]))} ]
        [:label {:for "normal"} "Normal"]]
       [:div
        [:input#hard {:name "difficulty"
                      :type "radio"
                      :value "hard"
                      :checked (= @difficulty "hard")
                      :on-change (fn [_]
                                   (re-frame/dispatch [::events/set-difficulty "hard"]))}]
        [:label {:for "hard"} "Hard"]]])))

(defn app []
  [:div.container
   [:div.buttons
    (select-difficulty-view)
    [:div.button
     [:button {:on-click (fn [_]
                           (re-frame/dispatch [::events/init-game]))} "New Game"]]
    [:div.button
     [:button {:on-click (fn [_]
                           (re-frame/dispatch [::events/reveal]))} "Reveal"]]]
   (main-panel)
   (score-view)])
