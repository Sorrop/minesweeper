(ns minesweeper.db)


(def default-db
  {:init-difficulty "easy"
   :difficulty "easy"
   :minefield []
   :mines #{}
   :score {}
   :game-state ""})
