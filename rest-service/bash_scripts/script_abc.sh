#!/bin/bash

#this script contains the abc of using these APIs, as included in the Javadocs too

### routes from IngestionController --------------------------------------------------------------------------
echo -e "\n\n  ----- create collections"

curl -i -H "Accept: application/json" -H "Content-Type: application/json" \
            -X POST --data '{"name" : "mycoll"}' http://localhost:8083/collections/create

curl -i -H "Accept: application/json" -H "Content-Type: application/json" \
            -X POST --data '{"name" : "auxcoll"}' http://localhost:8083/collections/create


echo -e "\n\n  ----- insert two documents in collections"

curl -i -H "Accept: application/json" -H "Content-Type: application/json" \
             -X POST  --data '["Short text", "The second text out of many texts!!!"]' \
             http://localhost:8083/collections/mycoll/insert

curl -i -H "Accept: application/json" -H "Content-Type: application/json" \
             -X POST  --data '["Third short text"]' \
             http://localhost:8083/collections/auxcoll/insert

echo -e "\n\n  ----- list collections"

curl -i -H "Accept: application/json" -X GET http://localhost:8083/collections/list

echo -e "\n\n  ----- show docs from collection"

curl -s -H "Accept: application/json" -X GET  http://localhost:8083/collections/mycoll/show | python -m json.tool



### routes from DebugController --------------------------------------------------------------------------

echo -e "\n\n  ----- Show tokens of one document in collection"

curl -s -H "Accept: application/json" -X GET  http://localhost:8083/debug/doctokens?docid=2 | jq -c '.[]'

echo -e "\n\n  -----  Show the documents found at specified key in index"

curl -s -H "Accept: application/json" -X GET  "http://localhost:8083/debug/indexedterm?term=text&collection=mycoll&bylemma=true"  | python -m json.tool

echo -e "\n\n  ----- Show all keys in index based on raw tokens text"

curl -s -H "Accept: application/json" -X GET  http://localhost:8083/debug/vocabulary

echo -e "\n\n  ----- Show all keys in index based on lemmatized tokens"

curl -s -H "Accept: application/json" -X GET  http://localhost:8083/debug/vocabulary?bylemma=true


### routes from RetrievalController --------------------------------------------------------------------------

echo -e "\n\n  ----- Search collection by 2 keywords, based on lemmatized tokens  (text,short)"

curl -s -H "Accept: application/json" -X GET "http://localhost:8083/search/multiterm?collection=mycoll&keywords=text,short&bylemma=true" | jq -c '.[]'

echo -e "\n\n  ----- Search collection phonetically using soundex encoding   (sekont)"

curl -s -H "Accept: application/json" -X GET "http://localhost:8083/search/phonetic?collection=mycoll&pronounced=sekont&algo=soundex"

echo -e "\n\n  ----- Search collection phonetically using metaphone encoding  (sekont)"

curl -s -H "Accept: application/json" -X GET "http://localhost:8083/search/phonetic?collection=mycoll&pronounced=sekont&algo=metaphone"

echo -e "\n\n  ----- Search collection by prefix string  (sho)"

curl -s -H "Accept: application/json" -X GET "http://localhost:8083/search/prefix?collection=mycoll&prefix=sho"

