#!/bin/bash

#source the script with the functions
source script_functions.sh --source-only

echo -e "\n\n =========================================== START DEMO 2 - PLANETS \n\n"

#---------------------------------------------------------------------------------

#data source:  https://solarsystem.nasa.gov/

declare -a planets_arr=(
"One day on Mercury (the time it takes for Mercury to rotate or spin once with respect to the stars) takes 59 Earth days. One day-night cycle on Mercury takes 175.97 Earth days. Mercury makes a complete orbit around the Sun (a year in Mercury time) in just 88 Earth days."  
"Venus rotates very slowly on its axis – one day on Venus lasts 243 Earth days. The planet orbits the Sun faster than Earth, however, so one year on Venus takes only about 225 Earth days, making a Venusian day longer than its year! Venus rotates backward on its axis compared to most planets in our solar system. This means the Sun rises in the west and sets in the east, opposite of what we see on Earth." 
"A day on Earth is 24 hours. Earth makes a complete orbit around the sun (a year in Earth time) in about 365 days."  
"One day on Mars takes a little over 24 hours. Mars makes a complete orbit around the Sun (a year in Martian time) in 687 Earth days."  
"Jupiter rotates once about every 10 hours (a Jovian day), but takes about 12 Earth years to complete one orbit of the Sun (a Jovian year)." 
"Saturn takes about 10.7 hours (no one knows precisely) to rotate on its axis once - a Saturn “day” - and 29 Earth years to orbit the sun." 
"Uranus takes about 17 hours to rotate once (a Uranian day), and about 84 Earth years to complete an orbit of the Sun (a Uranian year). Like Venus, Uranus rotates east to west. But Uranus is unique in that it rotates on its side." 
"Neptune takes about 16 hours to rotate once (a Neptunian day), and about 165 Earth years to orbit the sun (a Neptunian year)."
"A year on Pluto is 248 Earth years. A day on Pluto lasts 153 hours, or about 6 Earth days."  
"Ceres completes one rotation around its axis every 9 hours. Ceres takes 1,682 Earth days, or 4.6 Earth years, to make one trip around the sun."
"Eris takes 557 Earth years to make one trip around the Sun. The plane of Eris' orbit is well out of the plane of the solar system's planets and extends far beyond the Kuiper Belt, a zone of icy debris beyond the orbit of Neptune. As Eris orbits the Sun, it completes one rotation every 25.9 hours, making its day length similar to ours. "  
"Haumea takes 285 Earth years to make one trip around the Sun. As Haumea orbits the Sun, it completes one rotation every 4 hours, making it one of the fastest rotating large objects in our solar system. It is possible a massive impact billions of years ago set off Haumea's spin and created its moons. "

)

#dump array to txt file
printf "%s\n" "${planets_arr[@]}" > /tmp/planets.txt

#remove last empty line in file
head -c -1 "/tmp/planets.txt" > "/tmp/planets2.txt"

#---------------------------------------------------------------------------------

#invoke function from sourced script
populate_collection "planets" "/tmp/planets2.txt"

#-------------------------------------------------------------------------------

#check created collection

myecho "check planets collection so far"
curl -s -H "Accept: application/json" -X GET "http://localhost:8083/collections/planets/show"  |  python -m json.tool

myecho "debug tokens of document 5 in planets"
curl -s -H "Accept: application/json" -X GET "http://localhost:8083/debug/doctokens?collection=planets&docid=5"  | jq -c '.[]'



#check indexed vocabulary (the index has no knowledge of collections)
myecho "check indexed vocabulary (lemmas)"
curl -s -H "Accept: application/json" -X GET "http://localhost:8083/debug/vocabulary?bylemma=true" | python -m json.tool

#check indexed docs for keyword
myecho "check indexed docs for keyword rotate (raw)"
curl -s -H "Accept: application/json" -X GET "http://localhost:8083/debug/indexedterm?collection=planets&term=rotate" | python -m json.tool

myecho "check indexed docs for keyword rotate (lemma)"
curl -s -H "Accept: application/json" -X GET "http://localhost:8083/debug/indexedterm?collection=planets&term=rotate&bylemma=true" | python -m json.tool


#-------------------------------------------------------------------------------

#searches with results printed in nd-json format using jq

declare -a searches=(
"http://localhost:8083/search/prefix?collection=planets&prefix=rotat"
"http://localhost:8083/search/multiterm?collection=planets&keywords=day"
"http://localhost:8083/search/multiterm?collection=planets&keywords=day&bylemma=true"
"http://localhost:8083/search/multiterm?collection=planets&keywords=rotate"
"http://localhost:8083/search/multiterm?collection=planets&keywords=rotate&bylemma=true"
"http://localhost:8083/search/multiterm?collection=planets&keywords=rotate,day&bylemma=true"
"http://localhost:8083/search/multiterm?collection=planets&keywords=orbit,hour&bylemma=true"
"http://localhost:8083/search/multiterm?collection=planets&keywords=orbit,hour,axis&bylemma=true"
"http://localhost:8083/search/phonetic?collection=planets&pronounced=Cherress&algo=soundex"
"http://localhost:8083/search/phonetic?collection=planets&pronounced=Veenuz&algo=soundex"
"http://localhost:8083/search/phonetic?collection=planets&pronounced=Serris&algo=metaphone"
"http://localhost:8083/search/phonetic?collection=planets&pronounced=Gioveen&algo=metaphone"

)

#invoke function from sourced script, passing array as arg
search_collection "${searches[@]}"

echo -e "\n\n =========================================== DEMO 2 SUCCEEDED \n\n"
