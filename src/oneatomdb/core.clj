(ns oneatomdb.core) 
(defmacro seeThe
  ([db-map topic] `(~(keyword topic) ~db-map))
  ([db-map topic filter-list]
    (let [[operator field-name comparison field-value & therest] filter-list]
      (if operator
        `(filter #(~comparison ~(str field-value) (~(keyword field-name) %))
           (seeThe ~db-map ~topic ~therest)
         )
      `(~(keyword topic) ~db-map))))
  ([db-map topic operator field-name comparison field-value & therest]
    (if therest
       `(filter #(~comparison ~(str field-value) (~(keyword field-name) %))
          (seeThe ~db-map ~topic ~therest)
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

(defn build-join-condition [f1 comp f2]
  (fn [m1 m2] (comp (f1 m1) (f2 m2))
    ))

(defn build-join-name [mapname columnname]
  (keyword (str (name mapname) "." (name columnname))))

(defn re-key [m mapname]
  (reduce (fn [newmap k] (assoc newmap (build-join-name mapname k) (get m k))) {}  (keys m)))

(defn- re-key-list [l mapname]
  (reduce (fn [newlist m] (conj newlist  (re-key m mapname))) [] l))

(defn- dojoin [firstmap secondmap joincondition]
  (for [a firstmap
        b secondmap
        :when (joincondition a b)]
        (merge a b)))


(defn jointhe
  "db is the map that topics are being selected from
   and the fargs will be the join conditions"
  [db & fargs]

  (let [[t1 t2 & args] fargs
        firstmap  (if (coll? t1) t1 (re-key-list (t1 db) t1))
        secondmap (re-key-list (t2 db) t2)
        [onthelable acolumn comparison bcolumn & therest] args
        ;joincolumna (build-join-name t1 acolumn)
        ;joincolumnb (build-join-name t2 bcolumn)
        joincondition (if args (build-join-condition acolumn comparison bcolumn)
                               (fn [m1 m2] true))
        ]
    (if therest
      ;; conj to build new jointhe command with the result 
      ;; of the join of the first parms and then rest of the args
      (apply jointhe (conj therest (dojoin firstmap secondmap joincondition) db))
      (dojoin firstmap secondmap joincondition))
  ))

(defn seethe
  ([db-map topic] (if (coll? topic)
               (let [[jointhelabel & joinargs] topic]
                     (apply jointhe (conj joinargs db-map)))
               (topic db-map)))
  ([db-map topic & filterlist]
      (filter (apply wherethe (rest filterlist)) (seethe db-map topic)) 
))

(defn insertthe [a topic newval]
  (if (vector? newval) 
  (swap! a assoc topic (apply conj (topic @a) newval))
  (swap! a assoc topic (conj (topic @a) newval))
))

