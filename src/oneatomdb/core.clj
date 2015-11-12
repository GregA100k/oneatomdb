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

(defn seethefun 
  ([db-map topic] (topic db-map))
  ([db-map topic & filterlist]
    (let [[operator field-name comparison field-value & therest] filterlist
         ]
      (filter #(comparison field-value (field-name %))  (apply seethefun topic db-map therest))
    )
))




