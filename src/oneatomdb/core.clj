(ns oneatomdb.core)

(defmacro seethe [db-map topic]
  (list (keyword topic) db-map))

