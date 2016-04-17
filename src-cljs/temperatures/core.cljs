(ns temperatures.core
  (:require [sablono.core :as sab]
            [cljs.reader :as reader]
            [goog.events :as events]
            [reagent.core :as reagent]
            [cljsjs.nvd3]
            [temperatures.average :as avg])
  (:require-macros [devcards.core :refer [defcard defcard-rg]])
  (:import [goog.net XhrIo]
           [goog.net.EventType]))

;;; Full data set:
;;; http://bosclj.xngns.net:3449/data/abbotsford.edn

;;; Tiny subset of the data:
;;; http://bosclj.xngns.net:3449/data/sample.edn

;;; TODO: I need a cache (and/or fetching via websockets)
(defn edn-xhr [{:keys [method url data on-complete]}]
  (let [xhr  (new XhrIo)
        meth (clojure.string/upper-case (name method))]
    (events/listen xhr goog.net.EventType.COMPLETE
                   (fn [e]
                     (on-complete (reader/read-string (.getResponseText xhr)))))
    (.send xhr url meth (when data (pr-str data))
           #js {"Content-Type" "application/edn"})))

;; http://zachcp.org/blog/2015/reagent-d3/

(def data
  "This is the temperature data..."
  (atom nil))

;;; Load a tiny subset of the data for demo purposes
(edn-xhr {:method :get
          :url "data/abbotsford_downsample.edn"
          :on-complete #(reset! data %)})

(defcard temp-data
  "This is some temperature data from Canada in the 50s..."
  (take 3 @data))
(count @data)
(first @data)

(defn render-nvd3 [el]
  ;; cf http://nvd3.org/examples/line.html;
  ;; look how trivially simple the data generator becomes in cljs!
  ;; NOTE: nvd3 line example is out of date wrt options
  ;; `transitionDuration` and `useInteractiveGuideline`
  (nv.addGraph
   (fn []
     (let [nv-chart (doto (nv.models.lineChart)
                      (.options (clj->js {:transitionDuration 300
                                          :useInteractiveGuideline true}))
                      (.margin (clj->js {:left 100}))
                      (.showLegend false) ;; leave this to react
                      (.showYAxis true)
                      (.showXAxis true))]
       (doto (.-xAxis nv-chart)
         (.axisLabel "index")
         (.tickFormat (d3.format ",r")))
       (doto (.-yAxis nv-chart)
         (.axisLabel "temperature")
         (.tickFormat (d3.format ".02f")))
       (nv.utils.windowResize #(.update nv-chart))
       (-> (d3.select el)
           (.datum (clj->js [{:values
                              (let [partition-size 182
                                    step-size 1
                                    outlier-threshold 200]
                                (->> @data
                                     (remove
                                      (fn [item]
                                        (< outlier-threshold (:temperature item))))
                                     ((fn [all-data]
                                        (->> all-data
                                             (partition partition-size step-size)
                                             (map-indexed
                                              (fn [idx item-seq]
                                                {:x idx
                                                 :y (/ (apply + (map :temperature item-seq))
                                                       (count item-seq))})))))
                                     ;; old method (no averaging)
                                     ;; (map-indexed
                                     ;;  (fn [i item]
                                     ;;    {:x i
                                     ;;     :y (:temperature item)}))
                                     ))}]))
           (.call nv-chart))))))

(defcard-rg nvd3-plot
  ;; this is tricky because we need to tell reagent
  ;; to tell react to not update the chart drawing area
  [(with-meta identity
     {:component-did-mount
      (fn [el]
        (render-nvd3 (reagent/dom-node el)))})
   [:svg
    {:id "testchart"
     :style {:width "100%"
             :height "500px"
             :border "1px solid red"}}]])
