package org.searchengine.dto;

import org.searchengine.entities.BasicToken;

public class BasicTokenView {
    private final long id;
    private final int startchar;
    private final int endchar;
    private final String text;
    private final String lemma;
    private final String pos;
    private final String soundexcode;
    private final String metaphonecode;
    private final long parentdocId;

    public BasicTokenView(BasicToken token){
        this.id = token.getId();
        this.startchar = token.getStartchar();
        this.endchar = token.getEndchar();
        this.text = token.getText();
        this.lemma = token.getLemma();
        this.pos = token.getPos();
        this.soundexcode = token.getSoundexcode();
        this.metaphonecode = token.getMetaphonecode();
        this.parentdocId = token.getParentDoc().getId();
    }

    public long getId() {

        return id;
    }

    public int getStartchar() {

        return startchar;
    }

    public int getEndchar() {

        return endchar;
    }

    public String getText() {

        return text;
    }

    public String getLemma() {

        return lemma;
    }

    public String getPos() {

        return pos;
    }

    public String getSoundexcode() {

        return soundexcode;
    }

    public String getMetaphonecode() {

        return metaphonecode;
    }

    public long getParentdocId(){
        return parentdocId;
    }

}
