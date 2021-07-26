package org.searchengine.controller;

import org.searchengine.dto.DocCollectionView;
import org.searchengine.service.CatalogService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import org.searchengine.service.IngestionService;


@RestController
@RequestMapping("/collections")
public class IngestionController {

    private final static Logger logger = LoggerFactory.getLogger(IngestionController.class);
    

    @Autowired
    private IngestionService ingestService; 

    @Autowired
    private CatalogService catalogService;


    /**
     * <p> Endpoint for creating a new collection of documents, initially empty</p>
     * <br>
     * Example request: <br><br>
     * <pre>
     *         curl -i -H "Accept: application/json" -H "Content-Type: application/json" \
     *           -X POST --data '{"name" : "mycoll"}' http://localhost:8083/collections/create  
     * </pre>
     *
     * Example response:<br><br>
     *
     * <pre>
     *     {"status":"SUCCESS: Collection **mycoll** created"}
     *  or
     *     {"status":"ISSUE: Collection **mycoll** exists already"}
     * </pre>
     *
     * Sequence diagram: <br><br>
     *
     * <img src="../../../doc-files/IngestionController_createCollection.png" style="width:60%;"/>
     *
     * @param payload in json format, captured as a map of strings key-value pairs 
     * @return a json payload with status as key and information in value
     * @since 1.0
    */
    @PostMapping("/create")
    @ResponseBody
    public Map<String, String> createCollection(@RequestBody Map<String, String> payload) {

        Map<String, String> hm = new HashMap<String, String>();

        try{
            String collectionName = payload.get("name");

            //validate that the collection doesn't exist already
            if (catalogService.checkCollectionExists(collectionName) == false) {
                ingestService.createCollection(collectionName);
                hm.put("status", "SUCCESS: Collection **" + collectionName + "** created");
            }
            else {
                hm.put("status", "ISSUE: Collection **" + collectionName + "** exists already");
            }
        }
        catch(Exception e){
            hm.put("status", "ISSUE:" + e);
        }
        
        return hm;
    }


    /**
     * <p> Endpoint for inserting new documents to an existing collection of documents</p>
     * <br>
     * Example request: <br><br>
     * <pre>
     *         curl -i -H "Accept: application/json" -H "Content-Type: application/json" \
     *              -X POST  --data '["Short text", "The second text out of many texts!!!"]' \
     *              http://localhost:8083/collections/mycoll/insert
     * </pre>
     *
     * Example response:<br><br>
     *
     * <pre>
     *     {"status":"SUCCESS: Insert into collection **mycoll** "}
     *  or
     *     {"status":"ISSUE: The target collection **mycoll** needs to be created first"}
     * </pre>
     *
     * Sequence diagram: <br><br>
     *
     * <img src="../../../doc-files/IngestionController_insertDocs.png" style="width:60%;"/>
     *
     * @param name is a string identifying the target collection
     * @param payload in json format, captured as a list of strings
     * @return a json payload with status as key and information in value
     * @since 1.0
    */
    @PostMapping("/{name}/insert")
    @ResponseBody
    public  Map<String, String> insertDocs(@PathVariable(required = true) String name, 
                                           @RequestBody List<String> payload) {
        Map<String, String> hm = new HashMap<String, String>();
        String collectionName = name;

        try{
            //validate that the collection exists
            if (catalogService.checkCollectionExists(collectionName) == true) {
                ingestService.insertCollection(collectionName, payload);
                hm.put("status", "SUCCESS: Insert into collection **" + collectionName + "** ");
            }
            else {
                hm.put("status", "ISSUE: The target collection **" + collectionName+ "** needs to be created first");
            }
        }
        catch(Exception e){
            hm.put("status", "ISSUE:" + e);
        }
        return hm;
    }
    
 
    //-------------------------------------------------------------------------------

     /**
     * <p> Endpoint for getting list on the available collections of docs </p>
     * <br>
     * Example request: <br><br>
     * <pre>
     *         curl -i -H "Accept: application/json" -X GET http://localhost:8083/collections/list
     *         curl -i -H "Accept: application/json" -X GET http://localhost:8083/collections/
     * </pre>
     *
     * Example response:<br><br>
     *
     * <pre>
     *         ["mycoll"]
     * </pre>
     *
     * Sequence diagram: <br><br>
     *
     * <img src="../../../doc-files/IngestionController_listCollectionsNames.png" style="width:30%;"/>
     *
     * @return a json list of strings of existing documents collections names
     * @since 1.0
    */
    @GetMapping(value = {"/","/list"})
    @ResponseBody
    public List<String> listCollectionsNames() {
        List<String> result = catalogService.listCollectionsNames();
        return result;
    }


    /**
     * <p> Endpoint for getting list of all documents in an existing collection of documents</p>
     * <br>
     * Example request: <br><br>
     * <pre>
     *         curl -s -H "Accept: application/json" -X GET  http://localhost:8083/collections/mycoll/show
     * </pre>
     *
     * Example response:<br><br>
     *
     * <pre>
     *   {
     *     "collectionName": "mycoll",
     *     "docViews": [
     *         {
     *             "id": 1,
     *             "numFilteredTokens": 2,
     *             "numInitialTokens": 2,
     *             "text": "Short text"
     *         },
     *         {
     *             "id": 2,
     *             "numFilteredTokens": 4,
     *             "numInitialTokens": 7,
     *             "text": "The second text out of many texts!!!"
     *         }
     *     ],
     *     "numDocs": 2
     * }
     * </pre>
     *
     * Sequence diagram: <br><br>
     *
     * <img src="../../../doc-files/IngestionController_showCollectionInclDocs.png" style="width:60%;"/>
     *
     * @param name is a string identifying the target collection
     * @return a json payload with the name of the collection, number of docs in it,  and a list of documents views
     * @since 1.0
    */
    @GetMapping("/{name}/show")
    @ResponseBody
    public DocCollectionView showCollectionInclDocs(@PathVariable(required = true) String name) {

        DocCollectionView dcview = catalogService.listDocumentsInCollection(name);
        return dcview;
    }

    /**
     * <p>This is a fallback when no other route was matched,  
     *    e.g. http://localhost:8083/collections/abcd
     * </p>
     * @return a json payload with key status and information in value
     * @since 1.0
    */
    @GetMapping(value = {"*"})
    @ResponseBody
    public Map<String, String> welcome() {

        Map<String, String> hm = new HashMap<String, String>();
        hm.put("status", "ISSUE: endpoint not implemented");
        return hm;
    }

}
