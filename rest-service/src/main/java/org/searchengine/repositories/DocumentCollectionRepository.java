package org.searchengine.repositories;

//import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.Optional;

import org.searchengine.entities.DocumentCollection;

@Repository
public interface DocumentCollectionRepository extends JpaRepository<DocumentCollection, String> {
   
    /* out-of-the box, inherited from CrudRepository:

       DocumentCollection save(DocumentCollection);
       Optional<DocumentCollection> findById(ID);
       Iterable<DocumentCollection> findAll();
       long count();
       void delete(DocumentCollection);
       boolean existsById(ID);
    */

}
