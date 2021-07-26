#!/bin/bash

myecho() {
    echo -e "\n ----------------------------------------- $1 ------------------------------------------------------- \n"
}

generate_json_name() {

cat << EOF
{"name" : "$1"}
EOF

}


populate_collection() {

    echo -e "\n =================== START POPULATE COLLECTION \n"

    collname="$1"
    filename="$2"
    json_payload=$(jq -R -s 'split("\n") | [.] | add' $filename)
    echo "$json_payload"
    echo -e "\n \n"

    myecho "create collection"

    curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X POST --data "$(generate_json_name $collname)" http://localhost:8083/collections/create  

    myecho "list collections"

    curl -i -H "Accept: application/json" -X GET http://localhost:8083/collections/list

    myecho "insert docs in collection"

    curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X POST \
         --data "$json_payload" http://localhost:8083/collections/$collname/insert

    echo -e "\n =================== FINISHED POPULATE COLLECTION \n"
}


search_collection() {

    echo -e "\n =================== START SEARCH COLLECTION \n"
    #capture array received as arg
    urlsarr=("$@")

    for urlitem in "${urlsarr[@]}"
    do
       myecho "$urlitem"

       #jq to print results in newline delimited json format
       curl -s -H "Accept: application/json" -X GET "$urlitem"  | jq -c '.[]'

       echo -e "\n"
    done

    echo -e "\n =================== FINISHED SEARCH COLLECTION \n"
}

