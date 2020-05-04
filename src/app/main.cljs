(ns app.main
  (:require [app.lib :as lib]
            [reagent.core :as r]
            [reagent.dom :as rd]
            [re-frame.core :as rf]))

(defonce b 3)

(rf/reg-event-db
 :increment
 (fn [db _]
   (update-in db [:counter] (fnil inc b))))

(rf/reg-event-db
 :increment2
 (fn [db _]
   (update-in db [:counter] (fnil (fn [a] (+ a 100)) b))))

(rf/reg-event-db
 :decrement
 (fn [db _]
   (update-in db [:counter] (fnil dec b))))

(rf/reg-event-db
 :reset
 (fn [db _]
   (update-in db [:counter] #(identity b))))

(rf/reg-sub
 :counter
 (fn [db _]
   (get-in db [:counter] b)))

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
              :class "bg-blue hover:bg-blue-light text-white font-bold py-2 px-4 my-10 mx-10 rounded"} "reset!!!"]]
   ])

(defn ^:dev/after-load start []
  (rd/render
   [counter-view]
   (js/document.getElementById "app")))

(defn main! []
  (println "[main]: loading")
  (start))
