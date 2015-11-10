# oneatomdb

A Clojure library designed to do queries against a map as if it were a database.

All the data that drives an om site is stored inside an atom.  One atom.
This library treats that atom like a database and provides quering tools
to pull data out of that one atom database.

In order for this to work, the map-database needs to have top level keys
that represent the names of the database tables 
with values that are lists of maps.  The inner maps represent the rows
of the database table.  The keys of the inner map are the column 
names and the values are the data.

In the old TV series [Adam-12](https://en.wikipedia.org/wiki/Adam-12) the
radio calls always started out "One Adam-12. One Adam-12.  See the ..." and
the officers were off in pursuit.  

## Usage

The seethe function is used to get data out of the map

>(seethe mapdb toplevelkey)
>(seethe mapdb toplevelkey wherethe columnname comparison columnvalue)

>(def dbmap (atom
>          {:runners [{:firstname "Fast" :lastname "Runner" :racenumber "3"}
>                     {:firstname "Slow" :lastname "Runner" :racenumber "4"}
>                     {:firstname "Anon" :lastname "Ymous" :racenumber "5"}
>                    ] }))
>
>(seethe @dbmap runners wherethe lastname = Runner andthe racenumber = 4)

## License

Copyright © 2015 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
