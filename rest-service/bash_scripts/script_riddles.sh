#!/bin/bash

#source the script with the functions
source script_functions.sh --source-only

echo -e "\n\n =========================================== START DEMO 1 - RIDDLES \n\n"

#---------------------------------------------------------------------------------

declare -a riddles_arr=(
"Name three consecutive days without naming any of the seven days of the week. Answer: Yesterday, today, and tomorrow." 
"Where does today come before yesterday? Answer: In the dictionary." 
"I'm where yesterday follows today and tomorrow is in the middle. What am I? Answer: An English dictionary."  
"What is two days after the day after the day before yesterday? Answer: tomorrow"
)

#dump array to txt file
printf "%s\n" "${riddles_arr[@]}" > /tmp/riddles.txt

#remove last empty line in file
head -c -1 "/tmp/riddles.txt" > "/tmp/riddles2.txt"

#---------------------------------------------------------------------------------

#invoke function from sourced script
populate_collection "riddles" "/tmp/riddles2.txt"


#-------------------------------------------------------------------------------

#check created collection
myecho "check collection names so far"
curl -s -H "Accept: application/json" -X GET "http://localhost:8083/collections/list"

myecho "check riddles collection details"
curl -s -H "Accept: application/json" -X GET "http://localhost:8083/collections/riddles/show" | python -m json.tool

for ((ii=1;ii<=4;ii++))
do
    myecho "debug tokens of document $ii in riddles"
    curl -s -H "Accept: application/json" -X GET "http://localhost:8083/debug/doctokens?docid=$ii"  | jq -c '.[]'

done

#check indexed vocabulary
myecho "check indexed vocabulary (raw)"
curl -s -H "Accept: application/json" -X GET "http://localhost:8083/debug/vocabulary?bylemma=false" | python -m json.tool

myecho "check indexed vocabulary (lemmas)"
curl -s -H "Accept: application/json" -X GET "http://localhost:8083/debug/vocabulary?bylemma=true" | python -m json.tool


#check indexed docs for keyword
myecho "check indexed docs for keyword day (raw)"
curl -s -H "Accept: application/json" -X GET "http://localhost:8083/debug/indexedterm?collection=riddles&term=day" | python -m json.tool

myecho "check indexed docs for keyword day (lemma)"
curl -s -H "Accept: application/json" -X GET "http://localhost:8083/debug/indexedterm?collection=riddles&term=day&bylemma=true" | python -m json.tool

#---------------------------------------------------------------------------------

#searches with results printed in nd-json format using jq

declare -a searches=(
"http://localhost:8083/search/multiterm?collection=riddles&keywords=today"
"http://localhost:8083/search/multiterm?collection=riddles&keywords=day"
"http://localhost:8083/search/multiterm?collection=riddles&keywords=day&bylemma=true"
"http://localhost:8083/search/multiterm?collection=riddles&keywords=day,week&bylemma=true"
"http://localhost:8083/search/phonetic?collection=riddles&pronounced=tiksionari&algo=metaphone"
)

#invoke function from sourced script, passing array as arg
search_collection "${searches[@]}"

echo -e "\n\n =========================================== DEMO 1 SUCCEEDED \n\n"

