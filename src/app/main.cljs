(ns app.main
  (:require [app.lib :as lib]
            [reagent.core :as r :refer [atom]]
            [reagent.dom :as rd]
            [re-frame.core :as rf]
            [aws-sdk :as aws]
            [clojure.string :refer [split]]))

;-- the tailwindui demo ---------------------------------------------------------------------------

(defonce b 0)

(defonce aki (atom ""))
(defonce sak (atom ""))
(defonce st (atom ""))

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
        @(rf/subscribe [:counter])]]]]]])

(defn bump []
  (rf/dispatch [:increment]))

;-- the memtest game ---------------------------------------------------------------------------

(def colors ; colors match the cell number
  {1 "#677685", 2 "#FFB492", 3 "#8EE6CA", 4 "#92387E",
   5 "#FFF6C9", 6 "#5C58EB", 7 "#D1052D", 8 "#857A67"})

(def res (atom {}))
(def err_ (atom {}))
(def score (atom 0))                ; generates unique ids for each cell
(def gameboard (atom (sorted-map))) ; gameboard is sorted to preserve cell order
(def matched (atom #{}))            ; numbers that have been matched
(def clicked (atom #{}))            ; numbers that have been clicked
(def selected (atom nil))           ; cell that was last selected
(def highlighted (atom #{}))        ; cells that are highlighted

(def button-style
  "
  inline-flex items-center px-2.5 py-1.5 border border-transparent text-xs
  leading-4 font-medium rounded text-indigo-700 bg-indigo-100
  hover:bg-indigo-50 focus:outline-none focus:border-indigo-300
  focus:shadow-outline-indigo active:bg-indigo-200 transition ease-in-out
  duration-150
  ")

; A gameboard is a grid of cells, each uniquely identified, but two cells will
; have the same number and colors.  The game is won when all cells have been
; matched

(defn won-game?
  []
  ; game is won when count of matches is equal to half of gameboard, because
  ; cells contain duplicate numbers
  (and (= (/ (count @gameboard) 2) (count @matched))
       (not= (count @matched) 0)))

(defn cheated?
  []
  ; game is cheated when count of clicks is less than half of gameboard
  (not (and (= (/ (count @gameboard) 2) (count @clicked))
            (not= (count @clicked) 0))))

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
  (reset! clicked #{})
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
  (swap! matched conj number)            ; mark number as matched
  (swap! clicked conj number))           ; mark number as clicked

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
        "Memory Game..."]

       [:div (str (js->clj @res))]

       [:div (str (js->clj @err_))]

       [:div {:class "px-5"}
        [:div
         ; the gameboard
         [:div {:class "py-5"}
          [:table#gameboard [:tbody
                             ; taking 4 cells at a time for each row
                             (map-indexed
                              (fn [idx row] ^{:key idx} [board-row row])
                              (partition 4 cells))]]]
         ; the buttons
         [:span {:class "relative z-0 inline-flex shadow-sm"}
          [:button {:type "button"
                    :on-click #(new-game)
                    :href "#"
                    :class "
                           relative inline-flex items-center px-4 py-2
                           rounded-l-md border border-gray-300 bg-white text-sm
                           leading-5 font-medium text-gray-700
                           hover:text-gray-500 focus:z-10 focus:outline-none
                           focus:border-blue-300 focus:shadow-outline-blue
                           active:bg-gray-100 active:text-gray-700 transition
                           ease-in-out duration-150"}
           "Restart"]

          [:button {:type "button"
                    :on-click #(win-game)
                    :href "#"
                    :class "
                           -ml-px relative inline-flex items-center px-4 py-2
                           rounded-r-md border border-gray-300 bg-white text-sm
                           leading-5 font-medium text-gray-700
                           hover:text-gray-500 focus:z-10 focus:outline-none
                           focus:border-blue-300 focus:shadow-outline-blue
                           active:bg-gray-100 active:text-gray-700 transition
                           ease-in-out duration-150"}
           "Cheat"]]]]

       ; win status


       (if (won-game?)
         [:h2 {:class "px-5 py-5 text-lg leading-6 font-medium text-gray-900"}
          (if (cheated?)
            (do (bump) "You cheating bastard")
            "You won!")])])))

;-- main ---------------------------------------------------------------------------
(def identity-pool-id
  "us-west-2:33612cb4-5d92-45c3-a4e9-f1f45cc70625")

(def user-pool-id
  "cognito-idp.us-west-2.amazonaws.com/us-west-2_X0vt0G7r4")

(defn id-token
  []
  (nth (split (nth (split (nth (split js/document.location.href "#") 1) "&") 0) "=") 1))

(defn cb
  [err, data]
     (swap! err_
            #(identity err))
     (swap! res
            #(identity data)))

(defn sts
  []
  (.getCallerIdentity
   (aws/STS.)
   cb))

(defn assume-role-oidc
  [token]
  (.assumeRoleWithWebIdentity
   (aws/STS.)
   (clj->js { "RoleArn" "arn:aws:iam::844609041254:role/fogg_circus_cljs"
              "RoleSessionName" "app1", 
              "WebIdentityToken" token})
   #(if %2 (sts))))

(defn get-oidc-token
  []
  (.getOpenIdToken
   (aws/CognitoIdentity.)
   (clj->js {"IdentityId" aws/config.credentials.identityId
             "Logins" {user-pool-id (id-token)}})
   (fn [e, d] (if d (assume-role-oidc (aget d "Token"))))))

(defn aws-config
  []
  (aset aws/config
        "region"
        "us-west-2")
  (aset aws/config
        "credentials"
        (aws/CognitoIdentityCredentials. (clj->js {"IdentityPoolId" identity-pool-id
                                                   "Logins" {user-pool-id (id-token)}})))
  (aset aws/config.credentials
        "expired"
        (clj->js true))
  
  (get-oidc-token))

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

  (aws-config)

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
