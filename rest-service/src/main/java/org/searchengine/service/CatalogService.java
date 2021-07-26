package org.searchengine.service;

import org.javatuples.Septet;
import org.searchengine.dto.BasicTokenView;
import org.searchengine.dto.DocCollectionView;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import org.searchengine.repositories.DocumentRepository;
import org.searchengine.repositories.DocumentCollectionRepository;
import org.searchengine.repositories.BasicTokenRepository;

import org.searchengine.entities.SimpleDocument;
import org.searchengine.entities.DocumentCollection;
import org.searchengine.entities.BasicToken;

import java.util.stream.Collectors;
import java.util.*;
import java.util.Optional;


@Service
public class CatalogService {
     
    @Autowired
    private DocumentRepository docRepo;

    @Autowired
    private DocumentCollectionRepository doccollRepo;

    @Autowired
    private BasicTokenRepository tokensRepo;

    public boolean checkCollectionExists(String collectionName){

        return doccollRepo.existsById(collectionName);

    }  


    public List<String> listCollectionsNames(){
        List<DocumentCollection> doccollList = doccollRepo.findAll();

        List<String> result = doccollList.stream()
                                        .map(item -> item.getName())
                                        .collect(Collectors.toList() );
        return result;

    } 

    public DocCollectionView listDocumentsInCollection(String collectionName){
        DocCollectionView dcview = new DocCollectionView();

        Optional<DocumentCollection> doccollOpt = doccollRepo.findById(collectionName);
        if(doccollOpt.isPresent()) {
            DocumentCollection doccoll = doccollOpt.get();
            dcview = new DocCollectionView(doccoll);
        }
        return dcview;
    }


    public List<BasicTokenView> debugTokensInDoc(long docid){
        List<BasicTokenView> listTokensV = new ArrayList<BasicTokenView>();

        //obtain the simple doc by id, if it exists
        Optional<SimpleDocument> docOpt  = docRepo.findById(docid);
        if(docOpt.isPresent()) {
            SimpleDocument doc = docOpt.get();
            //take its tokens to be returned
            List<BasicToken> listTokens = doc.getTokens();
            listTokensV = listTokens.stream().map(e -> new BasicTokenView(e)).collect(Collectors.toList());
        }
        return listTokensV;
    }


    // --------- dump everything ----------------------
    public List<BasicToken> listAllTokens(){
        return tokensRepo.findAll();
    }

    public List<SimpleDocument> listAllDocuments(){
        return docRepo.findAll();
    }

    public List<DocumentCollection> listAllDocumentCollections() {
        return doccollRepo.findAll();
    }
}




