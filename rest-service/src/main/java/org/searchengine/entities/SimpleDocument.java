package org.searchengine.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;


@Entity
@Table(name = "DOCUMENT")
public class SimpleDocument implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name="text", length= 32000, nullable = false)
    private String text;

    //numtokens represents the number of tokens in text, after punctuation removal (but including stopwords)
    @Column(name="numtokens", nullable = false)
    private long numtokens = 0;

    //numfilteredtokens represents the number of tokens remaining after punctuation and stopwords removal
    @Column(name="numfilteredtokens", nullable = false)
    private long numfilteredtokens = 0;

    @ManyToOne
    @JoinColumn(name="coll_name", referencedColumnName="name", nullable = false, updatable = false, insertable = true)
    private  DocumentCollection  coll;    


    @OneToMany(targetEntity = BasicToken.class, mappedBy="parentdoc", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BasicToken> tokens = new ArrayList<BasicToken>();


    public SimpleDocument() {
      //no-args constructor required by jpa
    }

    public SimpleDocument(String text, DocumentCollection dc) {
	    this.text = text;
        this.coll = dc;
    }

    public void setId(long id) {

        this.id = id;
    }
    
    public long getId() {

        return id;
    }
    
    public void setText(String text) {

        this.text = text;
    }    

    public String getText() {

        return text;
    }

    public void setDocumentCollection(DocumentCollection dc){

        this.coll = dc;
    }

    public DocumentCollection getDocumentCollection(){

        return this.coll;
    }

    public void setNumtokens(long num) {

        this.numtokens = num;
    }
    
    public long getNumTokens() {

        return numtokens;
    }

    public void setNumFilteredTokens(long num){

        this.numfilteredtokens = num;
    }

    public long getNumFilteredTokens(){

        return this.numfilteredtokens;
    }

    public List<BasicToken> getTokens(){
        return tokens;
    }

    public void setTokens(List<BasicToken> tokens){
        this.tokens = tokens;
    }

    @Override
    public String toString() {
        String template = "Document[ID=%d, TEXT=%s]";
        String actualStr = String.format(template, this.id, this.text);
        return actualStr;        
    }
}
