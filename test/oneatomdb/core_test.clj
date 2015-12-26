(ns oneatomdb.core-test
  (:require [clojure.test :refer :all]
            [oneatomdb.core :as oa :refer :all]))

(def db1 (atom
           {:list1 [{:key1 "value1a" :key2 "value1b"}
                    {:key1 "value2a" :key2 "value2b"} ] }
))

(deftest get-the-top-level
  (testing "retrieving an existing top level key"
     (is (= (first (oa/seethe @db1 list1))
           {:key1 "value1a" :key2 "value1b"}
         )))
  (testing "retrieving a non-existing top level key"
     (is (nil? (oa/seethe @db1 notthere)))
   )
)

(deftest filter-top-level-results
  (testing "existing top level key and existing inner key"
    (is (= '({:key1 "value1a" :key2 "value1b"})
           (oa/seethe @db1 list1 wherethe key2 = value1b))))
  (testing "existing top level key and non-existing inner key"
    ;; nothing should match a non existing field so an empty list is returned
    (is (empty? (oa/seethe @db1 list1 wherethe notthere = anything)))))

(def db2 (atom
           {:runners [{:firstname "Greg" :lastname "Allen" :racenumber "3"}
                      {:firstname "Another" :lastname "Allen" :racenumber "4"}
                      {:firstname "Anon" :lastname "Ymous" :racenumber "5"}
                     ] }
         ))

(deftest multiple-and-filters
  (testing "existing two existing fields with and"
    (is (= '({:firstname "Another" :lastname "Allen" :racenumber "4"})
           (oa/seethe @db2 runners wherethe lastname = Allen andthe racenumber = 4))))
)


(deftest function-test
  (testing "single filter"
    (is (= '({:firstname "Greg" :lastname "Allen" :racenumber "3"})
           (oa/seethefun @db2 :runners "wherethe" :firstname = "Greg")))))
(deftest function-multiple-and-filters
  (testing "existing two existing fields with and"
    (is (= '({:firstname "Another" :lastname "Allen" :racenumber "4"})
           (oa/seethefun @db2 :runners "wherethe" ["andthe" :lastname = "Allen" :racenumber = "4"]))))
)


(deftest or-filter
  (testing "single or filter"
    (is (= '({:firstname "Greg" :lastname "Allen" :racenumber "3"}
             {:firstname "Anon" :lastname "Ymous" :racenumber "5"}
            )
           (oa/seethefun @db2 :runners "wherethe" ["orthe" :firstname = "Greg" :firstname = "Anon"])))))

(deftest test-build-compare
  (testing "build compare function match"
    (is ((wherethe :fname = "test") {:fname "test"})))
  (testing "build compare function does not match"
    (is (not ((wherethe :fname = "test") {:fname "nomatch"}))))
)

(deftest compare-either
  (testing "or conditions"
     (is (= '({:firstname "Another" :lastname "Allen" :racenumber "4"}
              {:firstname "Anon" :lastname "Ymous" :racenumber "5"})
            (oa/seethefun @db2 :runners "wherethe" ["orthe" :racenumber = "4" :racenumber = "5"])))
)

  (testing "combinations of and and or"
    (let 
        [l ["andthe" :lastname = "Allen" ["orthe" :racenumber = "3" :racenumber = "5"]]
         compare-function (wherethe l)]
     (is (= true (compare-function (get (:runners @db2) 0))))
     (is (= false (compare-function (get (:runners @db2) 1))))
     (is (= false (compare-function (get (:runners @db2) 2))))
    ))
)

(deftest compare-with-function
  (testing "a function as a filter"
    (let [fullname (fn [m] (str (:firstname m) " " (:lastname m)))]
      (is (= '({:firstname "Greg" :lastname "Allen" :racenumber "3"})
              (oa/seethefun @db2 :runners "wherethe" fullname = "Greg Allen"))))))
