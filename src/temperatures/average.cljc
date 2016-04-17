(ns temperatures.average)

(defn average [data]
  (-> data
      (->> (apply +))
      (/ (count data))))

(defn moving-average [n data]
  (map average (partition n 1 data)))

(def mydata (read-string (slurp "resources/abbotsford_downsample.edn")))

;; test it
(moving-average 5 (map :temperature mydata))
