(ns oneatomdb.core) 
(defmacro seethe
  ([db-map topic] `(~(keyword topic) ~db-map))
  ([db-map topic filter-list]
    (let [[operator field-name comparison field-value & therest] filter-list]
      (if operator
        `(filter #(~comparison ~(str field-value) (~(keyword field-name) %))
           (seethe ~db-map ~topic ~therest)
         )
      `(~(keyword topic) ~db-map))))
  ([db-map topic operator field-name comparison field-value & therest]
    (if therest
       `(filter #(~comparison ~(str field-value) (~(keyword field-name) %))
          (seethe ~db-map ~topic ~therest)
        )
       `(filter #(~comparison ~(str field-value) (~(keyword field-name) %))
          (~(keyword topic) ~db-map)
        )
    )))

(declare wherethe)

(defn build-comparison [field-name comparison field-value]
  (fn [m]
    (comparison field-value (field-name m))))

(defn build-comparison-list [& filterlist]
  (cond (empty? filterlist) []
        (coll? (first filterlist)) (conj (apply build-comparison-list (rest filterlist)) (apply wherethe (first filterlist)))
        :else (let [[field-name comparison field-value & therest] filterlist]
                (conj (apply build-comparison-list therest) (build-comparison field-name comparison field-value)))
  ))
  
(defn andthe [& args]
  (fn [m] (every? #(% m) (apply build-comparison-list args))
  ))

(defn orthe [& args]
  (fn [m] (if (some #(% m) (apply build-comparison-list args))
            true
            false)
))


(defn wherethe [& args]
  (cond (empty? args) (fn [m] true)
        (= "andthe" (first args)) (apply andthe (rest args))
        (= "orthe"  (first args)) (apply orthe (rest args))
        (coll? (first args)) (apply wherethe (first args))
        :else (let [[fieldname comparison fieldvalue & therest] args]
                (build-comparison fieldname comparison fieldvalue))))

(defn seethefun
  ([db-map topic] (topic db-map))
  ([db-map topic & filterlist]
    (if filterlist
      (filter (apply wherethe (rest filterlist)) (seethefun topic db-map)) )
  ))

