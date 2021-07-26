package org.searchengine.entities;

import javax.persistence.*;
import java.io.Serializable;

import java.util.*;


@Entity
@Table(name = "DOCCOLLECTION")
public class DocumentCollection implements Serializable{

    @Id
    private String name;

    @OneToMany(targetEntity = SimpleDocument.class, mappedBy="coll", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SimpleDocument> docs = new ArrayList<SimpleDocument>();

    public DocumentCollection() {
        //no-args constructor required by jpa
    }
    public DocumentCollection(String name) {

        this.name = name;
    }

    public void setName(String name) {

        this.name = name;
    }
    
    public String getName() {

        return name;
    }

    public void setDocs( List<SimpleDocument> docs){
        this.docs = docs;
    }

    public  List<SimpleDocument> getDocs(){
        return docs;
    }

    public int getNumDocs() {
        return this.docs.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("COLLECTION:");
        sb.append(this.name);
        sb.append("\n");
        sb.append("DOCUMENTS:");

        for (SimpleDocument doc : this.docs){
            sb.append(doc.toString());
            sb.append("\n");
        }

        return sb.toString();

    }
}
