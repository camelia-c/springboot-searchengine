package org.searchengine.dto;

import org.searchengine.entities.SimpleDocument;

public class SimpleDocumentView {

    private final long id;
    private final String text;
    private final long numInitialTokens;
    private final long numFilteredTokens;

    public SimpleDocumentView(SimpleDocument sd) {
        this.id = sd.getId();
        this.text = sd.getText();
        this.numInitialTokens = sd.getNumTokens();
        this.numFilteredTokens = sd.getNumFilteredTokens();
    }

    public long getId() {

        return id;
    }

    public String getText() {

        return text;
    }

    public long getNumInitialTokens(){

        return numInitialTokens;
    }

    public long getNumFilteredTokens(){

        return numFilteredTokens;
    }

    //final fields have no setters

    public String toString() {
        String template = "DocumentView[ID=%d, TEXT=%s NUMTOKS=%d (out of initial %d)]";
        String actualStr = String.format(template, this.id, this.text, this.numFilteredTokens, this.numInitialTokens);
        return actualStr;        
    }
}
