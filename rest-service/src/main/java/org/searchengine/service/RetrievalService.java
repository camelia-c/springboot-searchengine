package org.searchengine.service;

import org.searchengine.dto.SearchMultiResult;
import org.searchengine.entities.SimpleDocument;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import org.apache.commons.codec.language.Soundex;
import org.apache.commons.codec.language.Metaphone;

import java.util.stream.Collectors;
import java.util.*;
import org.javatuples.*;
import java.lang.Math;

import org.searchengine.repositories.DocumentRepository;
import org.searchengine.repositories.DocumentCollectionRepository;
import org.searchengine.repositories.BasicTokenRepository;

import org.searchengine.entities.DocumentCollection;
import org.searchengine.dto.SearchPhoneticResult;
import org.searchengine.dto.SearchPrefixResult;

@Service
public class RetrievalService {
     
    @Autowired
    private DocumentRepository docRepo;

    @Autowired
    private DocumentCollectionRepository doccollRepo;

    @Autowired
    private BasicTokenRepository tokensRepo;

    @Autowired
    private IndexingService idx;

    private Metaphone mtph;
    private Soundex sdx;

    public RetrievalService(){
        this.sdx = new Soundex();
        this.mtph = new Metaphone();
        this.mtph.setMaxCodeLen(10);
    }


    //---------------------- for serving searches:

    public List<SearchMultiResult> searchKeywords(String keywordsCommaSep, String collectionName, boolean bylemma) {
        String [] keywordsArrayInput = keywordsCommaSep.split(",");
        String punctuations = ".,;:!?\"()”-“–";
        //exclude any punctuation among searched keywords
        String [] keywordsArray = Arrays.stream(keywordsArrayInput)
                                        .filter(e -> punctuations.contains(e) == false)
                                        .map( e -> e.toLowerCase())
                                        .toArray(String[]::new);

        //will keep the IDF score of each keyword a the collection level
        double [] keywordsIDF = new double[keywordsArray.length];
        Arrays.fill(keywordsIDF, 0.0);
        int ii = 0;

        //a list of pairs of ints that will hold spans locations in text where a keyword was found
        List<Pair<Integer,Integer>> locSpans = null;
        //the intermediary results, a map  docid -> searchmultiresult (keeping id, text, map keyword -> score, map keyword -> spanslocations)
        Map<Long, SearchMultiResult> hmResMulti = new HashMap<Long, SearchMultiResult>();
        //the returned results
        List<SearchMultiResult> results = new ArrayList<SearchMultiResult>();

        //begin by checking if the collection with specified name really exists
        Optional<DocumentCollection> doccollOpt = doccollRepo.findById(collectionName);
        if(doccollOpt.isPresent()) {
            DocumentCollection doccoll = doccollOpt.get();
            int sizeCollection = doccoll.getNumDocs();

            //if the collection is empty we can already return result
            if (sizeCollection == 0) {
                return results;
            }

            //for each searched keyword
            for (String keyword : keywordsArray) {

                //use index to get the subset of documents from the specified collectionName that contain keyword
                List<Pair<SimpleDocument, Long>> candidateDocsTF = idx.generateCandidateDocs(keyword, collectionName, bylemma);

                if(candidateDocsTF.size() > 0) {
                    //the keyword's IDF can be computed
                    //note: we use a "smoothing correction" to account for terms that appear in all documents (which otherwise would get lg(N/N) = 0
                    double IDF = Math.log10(1.0 + (1.0 * sizeCollection / candidateDocsTF.size()));
                    keywordsIDF[ii] = IDF;

                    //compute TF-IDF score of document for the current keyword
                    List<Pair<SimpleDocument, Double>> candidateDocsTFIDF =
                            candidateDocsTF.stream()
                                    .map(e -> new Pair<SimpleDocument, Double>(e.getValue0(), (1.0 * e.getValue1() / e.getValue0().getNumFilteredTokens()) * IDF))
                                    .collect(Collectors.toList());


                    //for each scored candidate doc
                    for (Pair<SimpleDocument, Double> candidate : candidateDocsTFIDF) {

                        SimpleDocument doc = candidate.getValue0();
                        Double tfidfScore = candidate.getValue1();

                        //locate the spans of the matches within this doc's text
                        if (bylemma == false) {
                            locSpans = tokensRepo.locateRawKeywordHits(keyword, doc);
                        } else {
                            locSpans = tokensRepo.locateLemmaKeywordHits(keyword, doc);
                        }

                        //now create (if needed) a SearchMultiResult instance
                        if (hmResMulti.containsKey(doc.getId()) == false) {
                            SearchMultiResult smr = new SearchMultiResult(doc.getId(), doc.getText(), keywordsArray);
                            hmResMulti.put(doc.getId(), smr);
                        }

                        //update the score for the current keyword
                        hmResMulti.get(doc.getId()).updateScore(keyword, tfidfScore);
                        //update the spans locations for current keyword
                        hmResMulti.get(doc.getId()).updateSpansLocations(keyword, locSpans);

                    }//foreach doc containing current keyword

                }//if at least one matching doc for this keyword

                ii += 1;

            }// foreach keyword

            /** note:
             *  - until here we have an entry in hmresmulti for each doc that contains at least one keyword,
             *  - for each entry in hmresmulti we grab the array with individual scores
             *     (i.e. for each entry we have a n-dim vector where n is the number of keywords in this search)
             *
             *  - consolidate results, so that we remain with an aggregate score for each doc, used for ranking docs in the returned result
             */
            //on the search side, if we had 3 kewords than we have a vector [1/3, 1/3, 1/3] which comes weigted by the [keywordsIDF[0],keywordsIDF[1],keywordsIDF[2]]

            if (keywordsArray.length > 1) {
                //if the search is by multiple words, then scale their importance in the searchvector
                double coef = Double.valueOf(1.0/keywordsArray.length);
                double[] searchVector = Arrays.stream(keywordsIDF).map(e -> e * coef).toArray();

                //compute dot product between this search vector and the vector of each doc in hmResMulti
                hmResMulti.entrySet()
                        .stream()
                        .forEach(e -> hmResMulti.get(e.getKey()).setConsolidatedScore(dotProduct(e.getValue().getScoresArray(), searchVector)) );

                //now sort result based on desc order of consolidated scores just set
                results = hmResMulti.entrySet()
                        .stream()
                        .map(e -> e.getValue())
                        .sorted(Comparator.comparingDouble(SearchMultiResult::getConsolidatedScore).reversed())
                        .collect(Collectors.toList());
            }
            else {
                //if the search is only by one word, we use in ranking the already computed doc scores for that keyword
                Comparator<SearchMultiResult> comp = Comparator.comparingDouble(e -> e.getScoresArray()[0]);

                hmResMulti.entrySet()
                        .stream()
                        .forEach(e -> hmResMulti.get(e.getKey()).setConsolidatedScore(e.getValue().getScoresArray()[0]) );

                results = hmResMulti.entrySet()
                        .stream()
                        .map(e -> e.getValue())
                        .sorted(comp.reversed())
                        .collect(Collectors.toList());
            }

        }//collection exists
        return results;
    }


    public double dotProduct(double[] v1, double[] v2){
        double score = 0.0;
        for (int ii = 0; ii < v1.length; ii++ ){
            score += v1[ii] * v2[ii];
        }
        return score;
    }


    // ------------------- for phonetic search:

    public List<SearchPhoneticResult> searchPhonetic(String pronounced, String collectionName, String algo){
        List<SearchPhoneticResult> results = new ArrayList<SearchPhoneticResult>();
        List<Pair<SimpleDocument,Long>> rows = null;
        String[] supportedAlgos = {"soundex", "metaphone"};

        //begin by checking if the collection with specified name really exists
        Optional<DocumentCollection> doccollOpt = doccollRepo.findById(collectionName);
        if(doccollOpt.isPresent()) {
            DocumentCollection doccoll = doccollOpt.get();
            int numdocs = doccoll.getNumDocs();

            //in case the collection is empty or the algo is not supported
            if ((numdocs == 0) || (Arrays.asList(supportedAlgos).contains(algo.toLowerCase()) == false) ){
                //we can alreadu return result
                return results;
            }

            if (algo.equalsIgnoreCase("soundex") == true) {
                String searchedSoundex = this.sdx.encode(pronounced);
                rows = tokensRepo.aggregateSoundexOccurences(searchedSoundex, doccoll);
            }
            else {
                String searchedMetaphone = this.mtph.encode(pronounced);
                rows = tokensRepo.aggregateMetaphoneOccurences(searchedMetaphone, doccoll);
            }

            //sort results by number frequency of matches
            Comparator<Pair<SimpleDocument,Long>> comp = Comparator.comparingLong(e -> e.getValue1());

            results = rows.stream()
                          .sorted(comp.reversed())
                          .map( e -> new SearchPhoneticResult(e.getValue0().getId(), e.getValue0().getText(), e.getValue1()))
                          .collect(Collectors.toList());
        }

        return results;
    }

    //--------------------- for prefix searches:

    public List<SearchPrefixResult> searchPrefix(String prefix, String collectionName) {
        List<SearchPrefixResult> results = new ArrayList<SearchPrefixResult>();
        List<Triplet<SimpleDocument,Long,List<String>>> rows = null;

        //begin by checking if the collection with specified name really exists
        Optional<DocumentCollection> doccollOpt = doccollRepo.findById(collectionName);
        if(doccollOpt.isPresent()) {
            DocumentCollection doccoll = doccollOpt.get();
            int numdocs = doccoll.getNumDocs();

            //in case the collection is empty
            if (numdocs == 0) {
                //we can alreadu return result
                return results;
            }

            rows = idx.generatePrefixedCandidateDocs(prefix, collectionName);

            //sort results by number frequency of matches
            Comparator<Triplet<SimpleDocument,Long,List<String>>> comp = Comparator.comparingLong(e -> e.getValue1());

            results = rows.stream()
                          .sorted(comp.reversed())
                          .map( e -> new SearchPrefixResult(e.getValue0().getId(), e.getValue0().getText(),
                                                            e.getValue1(), e.getValue2() ))
                          .collect(Collectors.toList());

        }

        return results;
    }



    //---------------------- for checks:

    public List<Pair<SimpleDocument,Long>> queryCandidateDocs(String collectionName, String keyword, boolean bylemma) {
        List<Pair<SimpleDocument,Long>> res = new ArrayList<Pair<SimpleDocument,Long>>();

        Optional<DocumentCollection> doccollOpt = doccollRepo.findById(collectionName);
        if(doccollOpt.isPresent()) {
            DocumentCollection doccoll = doccollOpt.get();

            if (bylemma == false) {
                res = tokensRepo.aggregateRawKeywordOccurences(keyword, doccoll);
            } else {
                res = tokensRepo.aggregateLemmaKeywordOccurences(keyword, doccoll);
            }
        }

        return res;
    }


}
