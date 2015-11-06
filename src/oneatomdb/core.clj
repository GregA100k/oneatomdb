(ns oneatomdb.core)

(defmacro seethe [db-map topic & [_ field-name _ field-value :as args]]
  (if args
    `(filter #(= ~(str field-value) (~(keyword field-name) %)) (~(keyword topic) ~db-map))
    `(~(keyword topic) ~db-map)))

