(ns app.main
  (:require [app.lib :as lib]
            [reagent.core :as r]
            [reagent.dom :as rd]
            [re-frame.core :as rf]))

(defonce b 3)

(defn increment [db _]
  (update-in db [:counter] (fnil inc b)))

(defn increment2 [db _]
  (update-in db [:counter] (fnil (fn [a] (+ a 1000)) b)))

(defn decrement [db _]
  (update-in db [:counter] (fnil dec b)))

(defn reset [db _]
  (update-in db [:counter] #(identity b)))

(defn counter [db _]
  (get-in db [:counter] b))

(defn button [icon msg style]
  [:button
   {:class style
    :style {:background-color "#000"}}
   [:i {:class icon}]
   (str " " msg)])

(defn counter-view
  []
  [:div
   [:h2 "Counter..."]
   [:div
    [:span @(rf/subscribe [:counter])]]

   [:div {:style {:background-color "#fff"}}
    [:button {:on-click #(rf/dispatch [:decrement])
              :style {:background-color "#000"}
              :class "bg-blue hover:bg-blue-light text-white font-bold py-2 px-4 my-10 mx-10 rounded"} "-"]
    [:button {:on-click #(rf/dispatch [:increment])
              :style {:background-color "#000"}
              :class "bg-blue hover:bg-blue-light text-white font-bold py-2 px-4 my-10 mx-10 rounded"} "+"]
    [:button {:on-click #(rf/dispatch [:increment2])
              :style {:background-color "#000"}
              :class "bg-blue hover:bg-blue-light text-white font-bold py-2 px-4 my-10 mx-10 rounded"} "++"]
    [:button {:on-click #(rf/dispatch [:reset])
              :style {:background-color "#000"}
              :class "bg-blue hover:bg-blue-light text-white font-bold py-2 px-4 my-10 mx-10 rounded"} "0"]]
   ])

(defn ^:dev/after-load start []
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

  (rd/render
    [counter-view]
    (js/document.getElementById "app")))

(defn main! []
  (println "[main]: loading")

  (start))
