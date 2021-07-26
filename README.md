
## Prototyping a Spring Boot application for a Text Search Engine built from scratch

#### Author: Camelia Ciolac

**Introduction**

In this demo I develop a Spring Boot application that, via REST APIs, allows:  
- managing collections of texts documents 
- searching by multiple keywords and returning documents from a specified collection, ranked by their relevancy score
- retrieving documents that contain a term with similar pronounciation as the provided example, ranked by the frequency of that term in their text
- listing documents that contain tokens starting with a given prefix, ranked by the aggregated frequency of those terms in their text.

To accomplish this, this project uses:  
- a Derby in-memory database
- PATRICIA Trie strings indexing data structure
- CoreNLP for tokenization, lemmatization, part-of-speech tagging
- Apache Audio Commons for its Soundex or Metaphone encoding of strings
- Spring Boot initializer (https://start.spring.io/)


#### Layered architecture:

**Controllers:**

RestControllers are in charge with the REST API endpoints:  

1) IngestionController with routes:  

- /collections/create
- /collections/{coll}/insert
- /collections/list
- /collections/{coll}/show

2) DebugController with routes:  

- /debug/doctokens?docid={id}
- /debug/indexedterm?term={word}&collection={coll}&bylemma={true|false}
- /debug/vocabulary?bylemma={true|false}

3) RetrievalController with routes:  

- /search/multiterm?collection={coll}&keywords={word1,word2,..,wordn}&bylemma={true|false}
- /search/phonetic?collection={coll}&pronounced={word}&algo={soundex|metaphone}
- /search/prefix?collection={coll}&prefix={wordprefix}


Note:  
For the purpose of this demo, updates and deletes are not granted.  


**Services:**

Services implement the application logic and are injected (@Autowired) into the controllers.  
The IndexingService contains the logic around the PATRICIA Trie lookups, keyword-based or prefix-based. Its functionality is used by the RetrievalService.  

**Persistence:**

At the persistance layer, there is the in-memory Derby database and the @Repository components that perform operations on it.  
JPQL queries as well as native SQL queries with named parameters are used.  
The repositories are injected (@Autowired) in the services components.  
The configuration for Derby DB was set in :  src/main/resources/application.properties  based on the advice in https://github.com/spring-projects/spring-boot/issues/7706  


#### Build:


**Build project:**

```
 cd rest-service

 ./mvnw clean package

```

**Generate Javadocs:**

A folder holds images of Sequence Diagrams (generated with an InteliJ IDEA plugin), that come inserted in the JavaDocs of the controllers' methods  

```

tree src/main/java/org/searchengine/controller/doc-files

src/main/java/org/searchengine/controller/doc-files
├── DebugController_debugIndexedTerm.png
├── DebugController_debugIndexVocabulary.png
├── DebugController_debugtokensOfDocument.png
├── IngestionController_createCollection.png
├── IngestionController_insertDocs.png
├── IngestionController_listCollectionsNames.png
├── IngestionController_showCollectionInclDocs.png
├── RetrievalController_phoneticsearch.png
├── RetrievalController_prefixsearch.png
└── RetrievalController_searchmultikeyword.png

0 directories, 10 files

```

Generate:  

```
mvn javadoc:javadoc
```

Now open in browser `target/site/apidocs/index.html`   


**Run**

Start Tomcat web server on custom port 8083 with our text search engine app deployed:  

```
java -Dserver.port=8083 -jar target/rest-service-0.0.1-SNAPSHOT.jar
```

A set of bash scripts in folder /bash_scripts are provided as end-to-end examples based on selected texts about riddles (demo 1) and about planets (demo 2):  

```
cd bash_scripts

./script_riddles.sh  > outputs/output_riddles.txt

./script_planets.sh > outputs/output_planets.txt
```

The results are included in the folder: `bash_scripts/outputs/` and each demo was executed independently (i.e. server restarted).  

