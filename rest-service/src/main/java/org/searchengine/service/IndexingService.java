package org.searchengine.service;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.searchengine.entities.SimpleDocument;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import org.javatuples.*;

@Service
public class IndexingService {

       //PATRICIA Trie (Practical Algorithm to Retrieve Information Coded in Alphanumeric).
       //will keep mapping word -> [<doc1, counter1>, <doc2,counter2>, ...]
       private Trie<String, List<Pair<SimpleDocument,Long>>> idxRawText;
       private Trie<String, List<Pair<SimpleDocument,Long>>> idxLemma;

       public IndexingService() {
           Map<String, List<Pair<SimpleDocument,Long>>> maux1 = new HashMap<String, List<Pair<SimpleDocument,Long>>>();
           Map<String, List<Pair<SimpleDocument,Long>>> maux2 = new HashMap<String, List<Pair<SimpleDocument,Long>>>();
           this.idxRawText = new PatriciaTrie<>(maux1);
           this.idxLemma = new PatriciaTrie<>(maux2);

       }

       public void indexTokenTextDoc(String toktxt, SimpleDocument doc, long counter){

           if (idxRawText.containsKey(toktxt) == false){
               idxRawText.put(toktxt, new ArrayList<Pair<SimpleDocument,Long>>());
           }
           this.idxRawText.get(toktxt).add(new Pair<SimpleDocument,Long>(doc, counter));
       }

        public void indexTokenLemmaDoc(String toklema, SimpleDocument doc, long counter){

            if (idxLemma.containsKey(toklema) == false){
                idxLemma.put(toklema, new ArrayList<Pair<SimpleDocument,Long>>());
            }
            this.idxLemma.get(toklema).add(new Pair<SimpleDocument,Long>(doc, counter));
        }

       public List<Pair<SimpleDocument,Long>> generateCandidateDocs(String searchedWord, String collectionName, boolean bylemma){
           List<Pair<SimpleDocument,Long>> indexHits = new ArrayList<Pair<SimpleDocument,Long>>();

           if (bylemma == false) {
               indexHits = this.idxRawText.get(searchedWord);
           }
           else {
               indexHits = this.idxLemma.get(searchedWord);
           }

           //filter based on collectionname, info embedded in simpledocument's coll
           List<Pair<SimpleDocument,Long>> filteredIndexHits = indexHits.stream()
                                                                  .filter(e -> e.getValue0().getDocumentCollection().getName().equalsIgnoreCase(collectionName))
                                                                  .collect(Collectors.toList());

           return filteredIndexHits;
       }


       public Set<String> dumpVocabulary(boolean bylemma){
           if (bylemma == false) {
               return this.idxRawText.keySet();
           }
           else {
               return this.idxLemma.keySet();
           }
       }

       public List<Triplet<SimpleDocument,Long,List<String>>> generatePrefixedCandidateDocs(String searchedPrefix, String collectionName){
           List<Triplet<SimpleDocument,Long,List<String>>> res;

           SortedMap<String, List<Pair<SimpleDocument,Long>>> indexHits = this.idxRawText.prefixMap(searchedPrefix);

           //since there may be multiple index hits (for various terms starting with same prefix) in a doc, we need to aggregate at doc level
           Map<Long, Triplet<SimpleDocument,Long,List<String>>> docAggreg = new HashMap<Long, Triplet<SimpleDocument,Long,List<String>>>();

           for (Map.Entry<String, List<Pair<SimpleDocument,Long>>> entry : indexHits.entrySet()){
               String term = entry.getKey();
               for(Pair<SimpleDocument,Long> pair : entry.getValue()) {
                   SimpleDocument doc = pair.getValue0();
                   long freq = pair.getValue1();
                   if (doc.getDocumentCollection().getName().equalsIgnoreCase(collectionName)) {

                       if (docAggreg.containsKey(doc.getId()) == false){
                           List<String> laux = new ArrayList<String>();
                           laux.add(term);
                           docAggreg.put(doc.getId(), new Triplet<SimpleDocument,Long,List<String>>(doc, freq, laux));
                       }
                       else {
                           Triplet<SimpleDocument,Long,List<String>> oldTriplet = docAggreg.get(doc.getId());
                           long newfreq = oldTriplet.getValue1() + freq;
                           List<String> newlaux = oldTriplet.getValue2();
                           newlaux.add(term);
                           Triplet<SimpleDocument,Long,List<String>> newTriplet = oldTriplet.setAt1(newfreq)
                                                                                            .setAt2(newlaux);
                           docAggreg.put(doc.getId(), newTriplet);
                       }

                   }

               }//for pair

           }//for entry

           res =  docAggreg.entrySet().stream()
                                      .map(e -> e.getValue())
                                      .collect(Collectors.toList());

           return res;
       }

}
