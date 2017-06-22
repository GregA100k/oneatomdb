(ns oneatomdb.core-test
  (:require [clojure.test :refer :all]
            [oneatomdb.core :as oa :refer :all]))

(def db1 (atom
           {:list1 [{:key1 "value1a" :key2 "value1b"}
                    {:key1 "value2a" :key2 "value2b"} ] }
))

(deftest get-the-top-level
  (testing "retrieving an existing top level key"
     (is (= (first (oa/select @db1 list1))
           {:key1 "value1a" :key2 "value1b"}
         )))
  (testing "retrieving a non-existing top level key"
     (is (nil? (oa/select @db1 notthere)))
   )
)

(deftest filter-top-level-results
  (testing "existing top level key and existing inner key"
    (is (= '({:key1 "value1a" :key2 "value1b"})
           (oa/select @db1 list1 where :key2 = "value1b"))))
  (testing "existing top level key and non-existing inner key"
    ;; nothing should match a non existing field so an empty list is returned
    (is (empty? (oa/select @db1 list1 where :notthere = "anything")))))

(def db2 (atom
           {:runners [{:firstname "Greg" :lastname "Allen" :racenumber "3"}
                      {:firstname "Another" :lastname "Allen" :racenumber "4"}
                      {:firstname "Anon" :lastname "Ymous" :racenumber "5"}
                     ] 
            :laps [{:runnernumber "3" :course "l" :elapsedtime 20778} ]
            :courses [{:name "Long Loop" :id "l" :distance 3.35}
                      {:name "Short Loop" :id "s" :distance 1}
                     ]
           }
         ))

(deftest multiple-and-filters
  (testing "existing two existing fields with and"
    (is (= '({:firstname "Another" :lastname "Allen" :racenumber "4"})
           (oa/select @db2 runners where :lastname = Allen andthe racenumber = 4))))
)


(deftest function-test
  (testing "single filter"
    (is (= '({:firstname "Greg" :lastname "Allen" :racenumber "3"})
           (oa/select @db2 :runners where :firstname = "Greg")))))
(deftest function-multiple-and-filters
  (testing "existing two existing fields with and"
    (is (= '({:firstname "Another" :lastname "Allen" :racenumber "4"})
           (oa/select @db2 :runners where (andthe :lastname = "Allen" :racenumber = "4")))))
)


(deftest or-filter
  (testing "single or filter"
    (is (= '({:firstname "Greg" :lastname "Allen" :racenumber "3"}
             {:firstname "Anon" :lastname "Ymous" :racenumber "5"}
            )
           (oa/select @db2 :runners where (orthe :firstname = "Greg" :firstname = "Anon"))))))

(deftest test-build-compare
  (testing "build compare function match"
    (is ((where :fname = "test") {:fname "test"})))
  (testing "build compare function does not match"
    (is (not ((where :fname = "test") {:fname "nomatch"}))))
)

(deftest compare-either
  (testing "or conditions"
     (is (= '({:firstname "Another" :lastname "Allen" :racenumber "4"}
              {:firstname "Anon" :lastname "Ymous" :racenumber "5"})
            (oa/select @db2 :runners where (orthe :racenumber = "4" :racenumber = "5"))))
)

  (testing "combinations of and and or"
    (let 
        [l ["andthe" :lastname = "Allen" ["orthe" :racenumber = "3" :racenumber = "5"]]
         compare-function (where l)]
     (is (= true (compare-function (get (:runners @db2) 0))))
     (is (= false (compare-function (get (:runners @db2) 1))))
     (is (= false (compare-function (get (:runners @db2) 2))))
    ))
)

(deftest compare-with-function
  (testing "a function as a filter"
    (let [fullname (fn [m] (str (:firstname m) " " (:lastname m)))]
      (is (= '({:firstname "Greg" :lastname "Allen" :racenumber "3"})
              (oa/select @db2 :runners where fullname = "Greg Allen"))))))

(deftest join
  (testing "combining the tablename and the column name into join name"
    (is (= :testtable.testcolumn (oa/build-join-name :testtable :testcolumn)))
  )
  (testing "joining runners with laps"
    (is (= '({:runners.firstname "Greg" :runners.lastname "Allen" :runners.racenumber "3"
            :laps.runnernumber "3" :laps.course "l" :laps.elapsedtime 20778})
           (oa/seethe @db2 ["jointhe" :runners :laps "onthe" :runners.racenumber = :laps.runnernumber] where :runners.racenumber = "3")))
  )
  (testing "joining runners with laps and courses"
    (is (= '({:runners.firstname "Greg" :runners.lastname "Allen" :runners.racenumber "3"
            :laps.runnernumber "3" :laps.course "l" :laps.elapsedtime 20778 
            :courses.id "l" :courses.name "Long Loop" :courses.distance 3.35})
           (oa/seethe @db2 ["jointhe" :runners :laps "onthe" :runners.racenumber = :laps.runnernumber :courses "onthe" :laps.course = :courses.id] where :runners.racenumber = "3")))
  )
)

(deftest add-map-name-to-keys
  (testing "add map name"
    (let [m {:key1 "key1" :key2 "key2"}
          mapname :m1]
     (is (= {:m1.key1 "key1" :m1.key2 "key2"}
            (oa/re-key m mapname))))
  ))

(def db3 (atom {:runners [{:firstname "Greg" :lastname "Allen" :racenumber 3}
                      {:firstname "Another" :lastname "Allen" :racenumber 4}
                      {:firstname "Anon" :lastname "Ymous" :racenumber 5} ] 
            :laps [{:runnernumber 3 :course "l" :elapsedtime 20778} ]
            :courses [{:name "Long Loop" :id "l" :distance 3.35}
                      {:name "Short Loop" :id "s" :distance 1}]
           }
         ))

(deftest numeric-values
  (testing "joining runners with laps and courses"
    (is (= '({:runners.firstname "Greg" :runners.lastname "Allen" :runners.racenumber 3
            :laps.runnernumber 3 :laps.course "l" :laps.elapsedtime 20778 
            :courses.id "l" :courses.name "Long Loop" :courses.distance 3.35})
           (oa/seethe @db3 ["jointhe" :runners :laps "onthe" :runners.racenumber = :laps.runnernumber :courses "onthe" :laps.course = :courses.id] where :runners.racenumber = 3)))
  )
)

(deftest inserts
  (testing "adding a map to an existing list"
    (let [ dbi (atom {:runners [{:firstname "Existing" :lastname "Runner" :racenumber 7}]})
          newrunner {:firstname "Brandnew" :lastname "Runner" :racenumber 8}
          existing-runners (oa/select @dbi :runners)
          insert-row (oa/insertthe dbi :runners newrunner)
          selectedrunner (oa/select @dbi :runners where :racenumber = 8)
         ]
     (is (= (inc (count existing-runners)) (count (oa/select @dbi :runners))))
     (is (= newrunner (first selectedrunner)))
    ))

  (testing "adding a list of maps to an existing list"
    (let [dbi (atom {:runners [{:firstname "Existing" :lastname "Runner" :racenumber 7}]})
          newrunner1 {:firstname "Brandnew" :lastname "Runner" :racenumber 8}
          newrunner2 {:firstname "Secondnew" :lastname "Runner" :racenumber 9}
          newrunners [newrunner1 newrunner2]
          existing-runners (oa/select @dbi :runners)
          insert-row (oa/insertthe dbi :runners newrunners)
          selectedfirst (oa/select @dbi :runners where :racenumber = 8)
          selectedsecond (oa/select @dbi :runners where :racenumber = 9)
         ]
     (is (= (inc (inc (count existing-runners))) (count (oa/select @dbi :runners))))
     (is (= newrunner1 (first selectedfirst)))
     (is (= newrunner2 (first selectedsecond)))
    ))

  (testing "adding a map to a non-existing list"
    (let [dbi (atom {:runners [{:firstname "Existing" :lastname "Runner" :racenumber 7}]})
          newcourse {:name "Long Loop" :code "l" :distance 8}
          existing-courses (oa/select @dbi :courses)
          insert-row (oa/insertthe dbi :courses newcourse)
          selectedcourse (oa/select @dbi :courses where :code = "l")
         ]
     (is (= (inc (count existing-courses)) (count (oa/select @dbi :courses))))
     (is (= newcourse (first selectedcourse)))
    ))
) 

(deftest updates
  (testing "changing a specific value in a list"
    (let [first-runner {:firstname "Existing" :lastname "Runner" :racenumber 7}
          second-runner {:firstname "Fast" :lastname "Runner" :racenumber 8}
          dbi (atom {:runners [first-runner second-runner]})
          update-db (oa/updatethe dbi :runners "setthe" :lastname "Racer" "where" :racenumber = 8)
          updated-runner (oa/select @dbi :runners where :racenumber = 8)
         ]
     (is (= '({:firstname "Fast" :lastname "Racer" :racenumber 8})
	    updated-runner))
    ))
)
