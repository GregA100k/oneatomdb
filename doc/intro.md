# Introduction to oneatom

Assumptions are:

- A single map will act like a database
- Inside the map will be keys to lists of data objects
- There will be no schema
- if keys are missing, the data just will not be returned
- It will not exactly be SQL that is used to retrieve data from the map

Api
---

seeThe is the original macro version of the api
seethe is the newer function version of the api which has more features

seeThe:
-------
seeThe can return the top level values
seeThe can filter the top level values by a single field
seeThe can filter the top level values by multiple fields 'and'ing the results

The keys being searched are passed to seeThe as strings and are converted 
to keywords. Values are also treated as strings.  This makes for a clean 
looking query, but limits the values to strings.

seethe
------
seethe can filter top level values with combination of and and or 
conditions.  And and Or conditions can be nested to build more complicated 
queries. 

>(seethe @db2 :runners "wherethe" :firstname = "Greg")

>(seethe @app-state :runners "wherethe" ["andthe" :lastname = "Allen" :racenumber = "4"])

jointhe
-------
Jointhe works like a sql join.  Two or more lists can be joined together
based on keys within the contained maps.  Joins are done recursively so
the first two lists are joined and that result is joined with the next 
list.

>(seethe @db3 ["jointhe" :runners :laps "onthe" :runners.racenumber = :laps.runnernumber :courses "onthe" :laps.course = :courses.id] wherethe :runners.racenumber = 3)


