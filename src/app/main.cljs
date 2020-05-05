(ns app.main
  (:require [app.lib :as lib]
            [reagent.core :as r :refer [atom]]
            [reagent.dom :as rd]
            [re-frame.core :as rf]))

;-- the tailwindui demo ---------------------------------------------------------------------------

(defonce b 0)

(defn increment [db _]
  (update-in db [:counter] (fnil inc b)))

(defn increment2 [db _]
  (update-in db [:counter] (fnil (fn [a] (+ a 300)) b)))

(defn decrement [db _]
  (update-in db [:counter] (fnil dec b)))

(defn reset [db _]
  (update-in db [:counter] #(identity b)))

(defn counter [db _]
  (get-in db [:counter] b))

(defn counter-view
  []
  [:div {:class "px-10 py-10"}
   [:h3 {:class "text-lg leading-6 font-medium text-gray-900"}
      "Last 30 days"]
    [:div {:class "mt-5 grid grid-cols-1 gap-5 sm:grid-cols-3"}
      [:div {:class "bg-white overflow-hidden shadow rounded-lg"}
       [:div {:class "px-4 py-5 sm:p-6"}
        [:dl
          [:dt {:class "text-sm leading-5 font-medium text-gray-500 truncate"} 
            "Total Subscribers"]
          [:dd {:class "mt-1 text-3xl leading-9 font-semibold text-gray-900"} 
            @(rf/subscribe [:counter])]]]]
      [:div {:class "bg-white overflow-hidden shadow rounded-lg"}
       [:div {:class "px-4 py-5 sm:p-6"}
        [:dl
          [:dt {:class "text-sm leading-5 font-medium text-gray-500 truncate"} 
            "Total Subscribers"]
          [:dd {:class "mt-1 text-3xl leading-9 font-semibold text-gray-900"} 
            @(rf/subscribe [:counter])]]]]
      [:div {:class "bg-white overflow-hidden shadow rounded-lg"}
       [:div {:class "px-4 py-5 sm:p-6"}
        [:dl
          [:dt {:class "text-sm leading-5 font-medium text-gray-500 truncate"} 
            "Total Subscribers"]
          [:dd {:class "mt-1 text-3xl leading-9 font-semibold text-gray-900"} 
            @(rf/subscribe [:counter])]]]]
      ]])

(defn bump []
  (rf/dispatch [:increment2]))

;-- the memtest game ---------------------------------------------------------------------------

(def colors ; colors match the cell number
  {1 "#677685", 2 "#FFB492", 3 "#8EE6CA", 4 "#92387E",
   5 "#FFF6C9", 6 "#5C58EB", 7 "#D1052D", 8 "#857A67"})

(def score (atom 0))                ; generates unique ids for each cell
(def gameboard (atom (sorted-map))) ; gameboard is sorted to preserve cell order
(def matched (atom #{}))            ; numbers that have been matched
(def selected (atom nil))           ; cell that was last selected
(def highlighted (atom #{}))        ; cells that are highlighted

; A gameboard is a grid of cells, each uniquely identified, but two cells will
; have the same number and colors.  The game is won when all cells have been
; matched

(defn won-game?
  []
  ; game is won when count of matches is equal to half of gameboard, because
  ; cells contain duplicate numbers
  (and (= (/ (count @gameboard) 2) (count @matched))
       (not= (count @matched) 0)))

(defn win-game
  []
  (doseq [cell (range 1 9)]
   (swap! matched conj cell)))

(defn add-cell [n]
  ; add a numbered cell with a unique id
  (let [id (swap! score inc)]
    (swap! gameboard
           assoc id {:id id
                     :number n
                     :color (colors n)})))

(defn new-game
  []
  (.log js/console "new-game")
  ; game starts out with an empty board, no cell selected, nothing hilighted,
  ; and no matches
  (reset! score 0)
  (reset! gameboard (sorted-map))
  (reset! selected nil)
  (reset! matched #{})
  (reset! highlighted #{})
  ; take two sets of numbers (1..8) and randomize their order, then add them as
  ; cells
  (doseq [cell (shuffle (into (range 1 9) (range 1 9)))]
    (add-cell cell)))

(defn select-cell
  [cell]                   ; ensures one cell is colored via selection
  (reset! highlighted #{}) ; dont highlight anything
  (reset! selected cell))  ; mark cell as selected

(defn lose-cell
  [cell]                                 ; ensures two cells are colored via highlighting
  (reset! highlighted #{cell @selected}) ; highlight selected and current cell
  (reset! selected cell))                ; mark cell as selected

(defn win-cell
  [{:keys [number]}]                     ; ensures two more cells are colored via match
  (reset! selected nil)                  ; dont select anything
  (reset! highlighted #{})               ; dont highlight anything
  (swap! matched conj number))           ; mark number as matched

(defn winning-click?
  [{:keys [number id]}]
  (and (= (:number @selected) number) ; win if the number matched the selected cell
       (not= (:id @selected) id)))    ; and if it's not the same selected cell

(defn handle-click
  [{:keys [number id] :as cell}]
  (cond
   (= @selected cell) (reset! selected nil) ; reset if selected cell is selected again
   (nil? @selected) (select-cell cell)      ; set as selected if nothing was selected
   (winning-click? cell) (win-cell cell)    ; mark as won if click is a winner
   :else (lose-cell cell)))                 ; else mark as lost

(defn highlighted?
  [cell]
  (or (get @matched (:number cell)) ; color if number matched
      (= @selected cell)            ;       if selected
      (get @highlighted cell)))     ;       if highlighted

(defn board-cell []
  (fn [{:keys [number color id] :as cell}]
    ; display cell with background color
    [:td {:class "game-cell"
          :style (if (highlighted? cell) {:background-color color} {})
          :on-click #(handle-click cell)}]))

(defn board-row []
  (fn [row]
    [:tr
     ; loop through each cell in a row
     (for [{:keys [id] :as cell} row]
       ^{:key id} [board-cell cell])]))

(defn memtest-view []
  (fn []
    (let [cells (vals @gameboard)]
      [:div {:class "px-10"}
       [:h3 {:class "py-5 text-lg leading-6 font-medium text-gray-900"}
        "The Memory Game "
        [:a {:on-click #(new-game)
             :href "#"} "[restart]"]
        [:a {:on-click #(win-game)
             :href "#"} "[cheat]"]]
       ; win status
       (if (won-game?)
         [:h2 {:class "py-5 text-lg leading-6 font-medium text-gray-900"} "You won!!!"])
       ; the gameboard
       [:div {:class "py-5"}
        [:table#gameboard [:tbody
                           ; taking 4 cells at a time for each row
                           (map-indexed
                             (fn [idx row] ^{:key idx} [board-row row])
                             (partition 4 cells))]]]])))

;-- main ---------------------------------------------------------------------------
(defn ^:dev/after-load reload! []
  (rf/reg-event-db
    :increment
    increment)

  (rf/reg-event-db
    :increment2
    increment2)

  (rf/reg-event-db
    :decrement
    decrement)

  (rf/reg-event-db
    :reset
    reset)

  (rf/reg-sub
    :counter
    counter)

  (new-game)

  (rd/render
    [counter-view]
    (js/document.getElementById "app"))

  (rd/render
    [memtest-view]
    (js/document.getElementById "game")))

(defn main! []
  (println "[main]: loading")

  (reload!))

