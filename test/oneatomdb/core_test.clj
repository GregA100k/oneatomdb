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

