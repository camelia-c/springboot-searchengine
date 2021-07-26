package org.searchengine.controller;

import org.searchengine.dto.SearchPhoneticResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import org.searchengine.service.RetrievalService;
import org.searchengine.dto.SearchMultiResult;
import org.searchengine.dto.SearchPrefixResult;

@RestController
@RequestMapping("/search")
public class RetrievalController {

    private final static Logger logger = LoggerFactory.getLogger(RetrievalController.class);

    @Autowired
    private RetrievalService retrievalService;

    /**
     * <p> Endpoint for retrieving documents containing a set of specified words (comma-delimited)</p>
     * <br>
     * Example request: <br><br>
     * <pre>
     *        curl -s -H "Accept: application/json" \
     *        -X GET "http://localhost:8083/search/multiterm?collection=mycoll&keywords=text,short&bylemma=true"
     * </pre>
     *
     * Example response:  (note that scoresArray respects the ordef the comma-separated searched terms, while keywordsWithScores is in lexicographic order of the  hashmap's keys ) <br><br>
     *
     * <pre>
     *  [
     *     {
     *         "consolidatedScore": 0.4218504936826959,
     *         "docid": 1,
     *         "doctext": "Short text",
     *         "keywordsSpans": {
     *             "short": [ {"f0": "0", "f1": "5" }],
     *             "text": [ { "f0": "6", "f1": "10" }
     *             ]
     *         },
     *         "keywordsWithScores": { "short": 0.5493061443340549,  "text": 0.34657359027997264 },
     *         "scoresArray": [  0.34657359027997264, 0.5493061443340549 ]
     *     },
     *     {
     *         "consolidatedScore": 0.12011325347955035,
     *         "docid": 2,
     *         "doctext": "The second text out of many texts!!!",
     *         "keywordsSpans": {
     *             "short": [],
     *             "text": [ { "f0": "11", "f1": "15" }, { "f0": "28", "f1": "33" } ]
     *         },
     *         "keywordsWithScores": { "short": 0.0, "text": 0.34657359027997264 },
     *         "scoresArray": [ 0.34657359027997264, 0.0 ]
     *     }
     *  ]
     *
     * </pre>
     *
     * Sequence diagram: <br><br>
     *
     * <img src="../../../doc-files/RetrievalController_searchmultikeyword.png" style="width:60%;"/>
     *
     * @param collection is a string identifying the target collection
     * @param keywords is the string of the searched keywords delimitedby "|"
     * @return a json payload with json serialized instances of SearchMultiResult
     * @since 1.0
     */
    @GetMapping("/multiterm")
    @ResponseBody
    public List<SearchMultiResult> searchmultikeyword(@RequestParam(required = true) String collection,
                                                      @RequestParam(required = true) String keywords,
                                                      @RequestParam(required = false, defaultValue = "false") boolean bylemma) {

        return retrievalService.searchKeywords(keywords, collection, bylemma);
    }



    /**
     * <p> Endpoint for retrieving documents based on phonetic encoding of ONE token. The sorting of results is based on the term's frequency </p>
     * <br>
     * Example request: <br><br>
     * <pre>
     *        curl -s -H "Accept: application/json" -X GET "http://localhost:8083/search/phonetic?collection=mycoll&pronounced=sekont&algo=soundex"
     *
     *        curl -s -H "Accept: application/json" -X GET "http://localhost:8083/search/phonetic?collection=mycoll&pronounced=sekont&algo=metaphone"
     * </pre>
     *
     * Example response:<br><br>
     *
     * <pre>
     *     [
     *        {"freq":1, "docId":2, "docText":"The second text out of many texts!!!" }
     *     ]
     * </pre>
     *
     * Sequence diagram: <br><br>
     *
     * <img src="../../../doc-files/RetrievalController_phoneticsearch.png" style="width:50%;"/>
     *
     * @param collection is a string identifying the target collection
     * @param pronounced is the string of the searched keyword
     * @param algo is the choice between soundex (default) or metaphone algorithms
     * @return a json payload with json serialized instances of SearchPhoneticResult
     * @since 1.0
     */
    @GetMapping("/phonetic")
    @ResponseBody
    public List<SearchPhoneticResult> phoneticsearch(@RequestParam(required = true) String collection,
                                                     @RequestParam(required = true) String pronounced,
                                                     @RequestParam(required = true) String algo) {

        return retrievalService.searchPhonetic(pronounced, collection, algo);
    }

    /**
     * <p> Endpoint for retrieving documents based on prefix string matching of ONE token. The sorting of results is based on the term's frequency </p>
     * <br>
     * Example request: <br><br>
     * <pre>
     *      curl -s -H "Accept: application/json" -X GET "http://localhost:8083/search/prefix?collection=mycoll&prefix=sho&bylemma=true"
     *
     * </pre>
     *
     * Example response:<br><br>
     *
     * <pre>
     *       [   {"prefixedTokens":["short"],"docId":1,"docText":"Short text","freq":1}   ]
     * </pre>
     *
     * Sequence diagram: <br><br>
     *
     * <img src="../../../doc-files/RetrievalController_prefixsearch.png" style="width:50%;"/>
     *
     * @param collection is a string identifying the target collection
     * @param prefix is the string of the searched prefix
     * @return a json payload with json serialized instances of SearchPhoneticResult
     * @since 1.0
     */
    @GetMapping("/prefix")
    @ResponseBody
    public List<SearchPrefixResult> prefixsearch(@RequestParam(required = true) String collection,
                                                   @RequestParam(required = true) String prefix) {

        return retrievalService.searchPrefix(prefix, collection);
    }



    /**
     * <p>This is a fallback when no other route was matched,
     *    e.g. http://localhost:8083/search/abcd
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
