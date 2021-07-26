package org.searchengine.dto;

import org.searchengine.entities.DocumentCollection;
import org.searchengine.entities.SimpleDocument;

import java.util.*;

public class DocCollectionView {

    String collectionName;
    List<SimpleDocumentView> docViews = new ArrayList<SimpleDocumentView>();
    int numDocs;

    public DocCollectionView(){}

    public DocCollectionView(DocumentCollection dc){
        this.collectionName = dc.getName();
        for (SimpleDocument sd : dc.getDocs()){
            this.docViews.add(new SimpleDocumentView(sd));
        }
        this.numDocs = dc.getNumDocs();
    }

    public String getcollectionName(){
        return this.collectionName;
    }

    public List<SimpleDocumentView> getDocViews(){
        return this.docViews;
    }

    public int getNumDocs() {
        return this.numDocs;
    }
}
