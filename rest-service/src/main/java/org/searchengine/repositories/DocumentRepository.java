package org.searchengine.repositories;

//import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;
import java.util.Optional;

import org.searchengine.entities.SimpleDocument;
import org.searchengine.entities.DocumentCollection;

@Repository
public interface DocumentRepository extends JpaRepository<SimpleDocument, Long> {
   

     //the next two queries are equivalent
     @Query(value = "SELECT d FROM SimpleDocument d WHERE d.coll = :parentdc")
     public List<SimpleDocument> findDocumentsByCollection(@Param("parentdc") final DocumentCollection dc);

     @Query(value = "SELECT * FROM DOCUMENT d WHERE d.coll_name = :parentdc", nativeQuery = true)
     public List<SimpleDocument> findDocumentsByCollection2(@Param("parentdc") final DocumentCollection dc);



    /* out-of-the box, inherited from CrudRepository:

       SimpleDocument save(SimpleDocument);
       Optional<SimpleDocument> findById(ID);
       Iterable<SimpleDocument> findAll();
       long count();
       void delete(SimpleDocument);
       boolean existsById(ID);
    */
}
