package org.searchengine.dto;

import java.util.*;
import org.javatuples.*;

public class SearchMultiResult {

    private final long docid;
    private final String doctext;
    private double consolidatedScore;
    private Map<String, Double> keywordsWithScores  = new LinkedHashMap<String, Double>();
    private Map<String, List<Pair<Integer,Integer>>> keywordsPositions = new LinkedHashMap<String, List<Pair<Integer,Integer>>>();

    public SearchMultiResult(long docid, String txt, String[] keywords) {
        this.docid = docid;
        this.doctext = txt;
        for (String s: keywords) {
            this.keywordsWithScores.put(s, 0.0);
            this.keywordsPositions.put(s, new ArrayList<Pair<Integer,Integer>>());
        }
    }

    //ensure that all fields we want to expose have getter methods
    public long getDocid(){
        return docid;
    }

    public String getDoctext() {
        return doctext;
    }

    public Map<String, Double> getKeywordsWithScores(){
        return keywordsWithScores;
    }

    public Map<String, List<Pair<Integer,Integer>>> getKeywordsSpans(){
        return keywordsPositions;
    }

    public double getConsolidatedScore(){
        return consolidatedScore;
    }

    //--------------------------------------------------------------------

    //methods for updating info upon its calculation for this doc in relation with searched keywords
    public void updateScore(String s, Double d){
        this.keywordsWithScores.put(s, d);
    }

    public void updateSpansLocations(String s, List<Pair<Integer,Integer>> listSpansPositions){
        this.keywordsPositions.put(s, listSpansPositions);
    }

    public void setConsolidatedScore(double score){
        this.consolidatedScore = score;
    }

    //extract the array of individual TFIDF scores of each keyword, to be used in dot product computation
    public double[] getScoresArray(){
        return keywordsWithScores.values().stream().mapToDouble(e -> e).toArray();
    }



}
