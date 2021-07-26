package org.searchengine.dto;

import java.util.*;

public class SearchPrefixResult {

    private long docid;

    private String doctext;

    private Long freqTotal;

    private List<String> prefixedTokens;

    public SearchPrefixResult(long docid, String text, Long freq, List<String> prefixedTokens) {
        this.docid = docid;
        this.doctext = text;
        this.freqTotal = freq;
        this.prefixedTokens = prefixedTokens;
    }

    public void setDocId(long docid) {
        this.docid = docid;
    }

    public long getDocId() {
        return docid;
    }

    public void setDocText(String text) {
        this.doctext = text;
    }

    public String getDocText() {
        return doctext;
    }

    public void setFreq(Long freq) {
        this.freqTotal = freq;
    }

    public Long getFreq() {
        return freqTotal;
    }

    public List<String> getPrefixedTokens() {
        return this.prefixedTokens;
    }


    public String toString() {
        String template = "Match[docid=%d, TEXT=%s , FREQ=%d]";
        String actualStr = String.format(template, this.docid, this.doctext, this.freqTotal);
        return actualStr;
    }
}
