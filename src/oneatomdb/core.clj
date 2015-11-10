(ns oneatomdb.core)

;(defmacro seethe [db-map topic & [_ field-name _ field-value :as args]]
;  (if args
;    `(filter #(= ~(str field-value) (~(keyword field-name) %)) (~(keyword topic) ~db-map))
;    `(~(keyword topic) ~db-map)))

(defmacro seethe
  ([db-map topic] `(~(keyword topic) ~db-map))
  ([db-map topic filter-list]
    (let [[_ field-name comparison field-value & therest] filter-list]
      (if field-name
        `(filter #(~comparison ~(str field-value) (~(keyword field-name) %))
           (seethe ~db-map ~topic ~therest)
         )
      `(~(keyword topic) ~db-map))))
  ([db-map topic _ field-name comparison field-value & therest]
    (if therest
       `(filter #(~comparison ~(str field-value) (~(keyword field-name) %)) 
          (seethe ~db-map ~topic ~therest)
        )
       `(filter #(~comparison ~(str field-value) (~(keyword field-name) %)) 
          (~(keyword topic) ~db-map)
        )
    )))

