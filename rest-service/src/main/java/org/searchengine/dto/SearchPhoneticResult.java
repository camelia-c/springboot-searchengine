package org.searchengine.dto;


public class SearchPhoneticResult {

    private long docid;

    private String doctext;

    private Long freq;

    public SearchPhoneticResult(long docid, String text, Long freq) {
        this.docid = docid;
        this.doctext = text;
        this.freq = freq;
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
	this.freq = freq;
    }

    public Long getFreq() {
	return freq;
    }


    public String toString() {
        String template = "Match[docid=%d, TEXT=%s , FREQ=%d]";
        String actualStr = String.format(template, this.docid, this.doctext, this.freq);
        return actualStr;        
    }
}
