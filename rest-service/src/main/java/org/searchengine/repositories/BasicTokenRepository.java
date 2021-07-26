package org.searchengine.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

import org.searchengine.entities.SimpleDocument;
import org.searchengine.entities.DocumentCollection;
import org.searchengine.entities.BasicToken;
import org.javatuples.*;

@Repository
public interface BasicTokenRepository extends JpaRepository<BasicToken, Long> {

    @Query("   SELECT new org.javatuples.Pair(t.startchar, t.endchar) "
            + " FROM BasicToken t "
            + " WHERE t.text = :searched  "
            + "   AND t.parentdoc = :doc "
    )
    public List<Pair<Integer, Integer>> locateRawKeywordHits(@Param("searched") final String s,
                                                                                @Param("doc") final SimpleDocument doc);



    @Query("   SELECT new org.javatuples.Pair(t.startchar, t.endchar) "
           + " FROM BasicToken t "
           + " WHERE t.lemma = :searched  "
           + "   AND t.parentdoc = :doc "
    )
    public List<Pair<Integer, Integer>> locateLemmaKeywordHits(@Param("searched") final String s,
                                                                                  @Param("doc") final SimpleDocument doc);


    //---------------------------------------------------------------------------------------------------------------


    @Query("   SELECT new org.javatuples.Pair(t.parentdoc, COUNT(1)) "
            + " FROM BasicToken t JOIN SimpleDocument d ON t.parentdoc = d.id "
            + " WHERE t.soundexcode = :pronounced  "
            + "   AND d.coll = :coll "
            + " GROUP BY t.parentdoc "
    )
    public List<Pair<SimpleDocument,Long>> aggregateSoundexOccurences(@Param("pronounced") final String s,
                                                                      @Param("coll") final DocumentCollection dc);



    @Query("   SELECT new org.javatuples.Pair(t.parentdoc, COUNT(1)) "
            + " FROM BasicToken t JOIN SimpleDocument d ON t.parentdoc = d.id "
            + " WHERE t.metaphonecode = :pronounced  "
            + "   AND d.coll = :coll "
            + " GROUP BY t.parentdoc "
    )
    public List<Pair<SimpleDocument,Long>> aggregateMetaphoneOccurences(@Param("pronounced") final String s,
                                                                        @Param("coll") final DocumentCollection dc);


    //---------------------------------------------------------------------------------------------------------------

    // the next 2 queries are alternative to method generateCandidateDocs of IndexingService
    @Query("   SELECT new org.javatuples.Pair(t.parentdoc, COUNT(1)) "
           + " FROM BasicToken t JOIN SimpleDocument d ON t.parentdoc = d.id "
           + " WHERE t.text = :searched  "
           + "   AND d.coll = :coll "
           + " GROUP BY t.parentdoc "
    )
    public List<Pair<SimpleDocument,Long>> aggregateRawKeywordOccurences(@Param("searched") final String s,
                                                                         @Param("coll") final DocumentCollection dc);


    @Query("   SELECT new org.javatuples.Pair(t.parentdoc, COUNT(1)) "
            + " FROM BasicToken t JOIN SimpleDocument d ON t.parentdoc = d.id "
            + " WHERE t.lemma = :searched  "
            + "   AND d.coll = :coll "
            + " GROUP BY t.parentdoc "
    )
    public List<Pair<SimpleDocument,Long>> aggregateLemmaKeywordOccurences(@Param("searched") final String s,
                                                                           @Param("coll") final DocumentCollection dc);


    //--------------------------------------------------------------------------------------


    /* out-of-the box, inherited from CrudRepository:

       BasicToken save(BasicToken);
       Optional<BasicToken> findById(ID);
       Iterable<BasicToken> findAll();
       long count();
       void delete(BasicToken);
       boolean existsById(ID);
    */
}
