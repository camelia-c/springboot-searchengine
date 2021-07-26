package org.searchengine.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.javatuples.*;
import java.util.*;

import org.searchengine.dto.BasicTokenView;
import org.searchengine.entities.SimpleDocument;
import org.searchengine.service.CatalogService;
import org.searchengine.service.IndexingService;
import org.searchengine.service.RetrievalService;



@RestController
@RequestMapping("/debug")
public class DebugController {

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private RetrievalService retrievalService;

    @Autowired
    private IndexingService idx;

    /**
     * <p> Endpoint for debugging tokens of a given document (specified by docid)</p>
     * <br>
     * Example request: <br><br>
     * <pre>
     *     curl -s -H "Accept: application/json" -X GET  http://localhost:8083/debug/doctokens?docid=2
     * </pre>
     *
     * Example response:<br><br>
     *
     * <pre>
     *   [
     *     {"id":3,"startchar":4,"endchar":10,"text":"second","lemma":"second","pos":"JJ","soundexcode":"S253","metaphonecode":"SKNT","parentdocId":2}
     *     {"id":4,"startchar":11,"endchar":15,"text":"text","lemma":"text","pos":"NN","soundexcode":"T230","metaphonecode":"TKST","parentdocId":2}
     *     {"id":5,"startchar":23,"endchar":27,"text":"many","lemma":"many","pos":"JJ","soundexcode":"M500","metaphonecode":"MN","parentdocId":2}
     *     {"id":6,"startchar":28,"endchar":33,"text":"texts","lemma":"text","pos":"NNS","soundexcode":"T232","metaphonecode":"TKSTS","parentdocId":2}
     *    ]
     * </pre>
     *
     * Sequence diagram: <br><br>
     *
     * <img src="../../../doc-files/DebugController_debugtokensOfDocument.png" style="width:50%;"/>
     *
     * @param docid is a number uniquely identifying the target document
     * @return a json payload with json serialized instances of BasicTokenView
     * @since 1.0
     */
    @GetMapping("/doctokens")
    @ResponseBody
    public  List<BasicTokenView> debugtokensOfDocument(@RequestParam(required = false, defaultValue = "-1") long docid) {
        List<BasicTokenView> listtokens = null;

        if (docid != -1) {

            listtokens = catalogService.debugTokensInDoc(docid);
        }
        else {
            listtokens = new ArrayList<BasicTokenView>();
        }
        return listtokens;
    }


    /**
     * <p> Endpoint for debugging a specific key in the PatriciaTrie indexes and compare to the aggregation results computed with JPQL</p>
     * <br>
     * Example request: <br><br>
     * <pre>
     *    curl -s -H "Accept: application/json" -X GET  "http://localhost:8083/debug/indexedterm?term=text&collection=mycoll&bylemma=true"
     * </pre>
     *
     * Example response:<br><br>
     *
     * <pre>
     *  {
     *     "INDEX_HITS": [
     *         { "f0": "Document[ID=1, TEXT=Short text]",  "f1": "1" },
     *         { "f0": "Document[ID=2, TEXT=The second text out of many texts!!!]", "f1": "2" }
     *     ],
     *     "JPQL_HITS": [
     *         { "f0": "Document[ID=1, TEXT=Short text]", "f1": "1" },
     *         { "f0": "Document[ID=2, TEXT=The second text out of many texts!!!]", "f1": "2" }
     *     ]
     * }
     * </pre>
     *
     * Sequence diagram: <br><br>
     *
     * <img src="../../../doc-files/DebugController_debugIndexedTerm.png" style="width:60%;"/>
     *
     * @param term string of the searched term
     * @param collection is a string identifying the target collection
     * @param bylemma boolean flag, whether to treat word in raw text form or its lemma
     * @return a json payload with json serialized instances of BasicTokenView
     * @since 1.0
     */
    @GetMapping("/indexedterm")
    @ResponseBody
    public  Map<String, List<Pair<SimpleDocument,Long>>> debugIndexedTerm(
                                                             @RequestParam(required = true) String term,
                                                             @RequestParam(required = true) String collection,
                                                             @RequestParam(required = false, defaultValue = "false") boolean bylemma) {
        Map<String, List<Pair<SimpleDocument,Long>>> res = new HashMap<String, List<Pair<SimpleDocument,Long>>>();

        //compare the rsults from the index with the results from the JPQL
        List<Pair<SimpleDocument,Long>> idxHITS = idx.generateCandidateDocs(term, collection, bylemma);
        List<Pair<SimpleDocument,Long>> jpqlHITS =  retrievalService.queryCandidateDocs(collection, term, bylemma);

        res.put("INDEX_HITS", idxHITS);
        res.put("JPQL_HITS", jpqlHITS);
        return res;
    }


    /**
     * <p> Endpoint for debugging the vocabulary indexed in the PatriciaTrie indexes </p>
     * <br>
     * Example request: <br><br>
     * <pre>
     *          curl -s -H "Accept: application/json" -X GET  http://localhost:8083/debug/vocabulary
     *    vs.
     *          curl -s -H "Accept: application/json" -X GET  http://localhost:8083/debug/vocabulary?bylemma=true
     * </pre>
     *
     * Example response:<br><br>
     *
     * <pre>
     *          ["many","second","short","text","texts"]
     *    vs.
     *          ["many","second","short","text"]
     * </pre>
     *
     * Sequence diagram: <br><br>
     *
     * <img src="../../../doc-files/DebugController_debugIndexVocabulary.png" style="width:30%;"/>
     *
     * @param bylemma boolean flag, whether to treat word in raw text form or its lemma
     * @return a json payload with json serialized set of strings
     * @since 1.0
     */
    @GetMapping("/vocabulary")
    @ResponseBody
    public  Set<String> debugIndexVocabulary(
            @RequestParam(required = false, defaultValue = "false") boolean bylemma) {
        return idx.dumpVocabulary(bylemma);
    }


    /**
     * <p>This is a fallback when no other route was matched,
     *    e.g. http://localhost:8083/debug/abcd
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
