(ns app.main
  (:require [app.lib :as lib]
            [reagent.core :as r]
            [reagent.dom :as rd]
            [re-frame.core :as rf]))

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

  (rd/render
    [counter-view]
    (js/document.getElementById "app")))

(defn main! []
  (println "[main]: loading")

  (reload!))

(defn bump []
  (rf/dispatch [:increment2]))
