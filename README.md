# oneatomdb

A Clojure library designed to do queries against a map as if it were a database.

The React inspired clojure libraries like Om and Reframe use an atom to store
the state of the application.  This library treats that atom like a database and 
provides quering tools to pull data out of that one atom database.

In order for this to work, the map-database needs to have top level keys
that represent the names of the database tables 
with values that are lists of maps.  The inner maps represent the rows
of the database table.  The keys of the inner map are the column 
names and the values are the data.

## Usage

The "select" function is used to get data out of the map

>(select mapdb toplevelkey)
>(select mapdb toplevelkey where columnname comparison columnvalue)

>(def dbmap (atom
>          {:runners [{:firstname "Fast" :lastname "Runner" :racenumber 3}
>                     {:firstname "Slow" :lastname "Runner" :racenumber 4}
>                     {:firstname "Anon" :lastname "Ymous" :racenumber 5}
>                    ] }))
>
>(select @dbmap :runners where :lastname = "Runner")
>
>(select @dbmap :runners where (orthe :racenumber = 3 :racenumber = 4))

The columnname doesn't have to be a keyword, it just needs to be a function
which will operate on the map.  If there were a fullname function that 
put together the first name and lastname, then it can be used in a "where"

>(defn fullname [m] (str (:firstname m) " " (:lastname m)))
>(seethe @db2 :runners where fullname = "Fast Runner")


More than one list can be joined together using a format similar to the 
where format.  The keys for the joined together maps will be prefixed
with the key to the list, :firstname in :runners becomes :runners.firstname.
This joined keyword has to be used in where parameters.

>(def app-state (atom
>           {:runners [{:firstname "Greg" :lastname "Allen" :racenumber 3}
>                      {:firstname "Another" :lastname "Allen" :racenumber 4}
>                      {:firstname "Anon" :lastname "Ymous" :racenumber 5} ]
>            :laps    [{:runnernumber "3" :course "l" :elapsedtime 20778} ]
>            :courses [{:name "Long Loop" :id "l" :distance 3.35}
>                      {:name "Short Loop" :id "s" :distance 1} ]
>           }))
>

Here is an example query given the atom with :runners, :courses, and :laps.

>(seethe @app-state (join :runners :laps "onthe" :runners.racenumber = :laps.runnernumber :courses "onthe" :laps.course = :courses.id) where :runners.racenumber = 3)
>
>({:runners.firstname "Greg" :runners.lastname "Allen" :runners.racenumber 3
            :laps.runnernumber 3 :laps.course "l" :laps.elapsedtime 20778
            :courses.id "l" :courses.name "Long Loop" :courses.distance 3.35})

Coming Up
=========
- project only certain keys from the returned list
- order by option for sorting the results


## License

Copyright © 2017 GregA100k

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
