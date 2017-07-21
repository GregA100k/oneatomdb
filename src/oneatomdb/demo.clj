(ns oneatomdb.demo
  [require [oneatomdb.core :as oa :refer :all]
           [clojure.pprint]]) 


(def dbmap (atom
          {:runners [{:firstname "Fast" :lastname "Runner" :racenumber 3}
                     {:firstname "Slow" :lastname "Runner" :racenumber 4}
                     {:firstname "Anon" :lastname "Ymous" :racenumber 5}
                    ] }))



(defn -main [& args]

  (println "Initial atom database for a list of runners")
  (println)
  (clojure.pprint/pprint @dbmap)

  (println)
  (println)
  (println "Select those runners using '(select @dbmap :runners)")
  (println)
  (clojure.pprint/pprint (oa/select @dbmap :runners))

  (println "Add some courses for the runners to run")
  (oa/insert! dbmap :courses [{:name "Long Loop" :id 1 :distance 3.35}
                              {:name "Short Loop" :id 2 :distance 1.0}])

  (println)
  (clojure.pprint/pprint @dbmap)


  (println)
  (println)
  (println "now have the runners run some laps")
  (oa/insert! dbmap :laps {:runnernumber 5 :course 2 :elapsedtime (* 60 10 1.0)})
  (oa/insert! dbmap :laps {:runnernumber 3 :course 1 :elapsedtime (* 60 10 3.35)})
  (oa/insert! dbmap :laps {:runnernumber 4 :course 1 :elapsedtime (* 60 10.5 3.35)})
  (oa/insert! dbmap :laps {:runnernumber 5 :course 1 :elapsedtime (* 60 11 3.35)})
  (clojure.pprint/pprint @dbmap)
  
  (println)
  (println "join the runner information with the course and lap information")
  (println)
  (def runner-laps (oa/select @dbmap (join :runners :laps "onthe" :runners.racenumber = :laps.runnernumber
                                           :courses "onthe" :laps.course = :courses.id)))

  (clojure.pprint/pprint runner-laps)

  (println)
  (println)
  (clojure.pprint/pprint "this output can then be operated on to calculate the distance travelled by each runner")

  (println)
  (clojure.pprint/pprint
    (map (fn [[k v]] 
           [k (reduce #(+ %1 (:courses.distance %2)) 0 v)]) 
           (group-by :runners.racenumber runner-laps)))

)
