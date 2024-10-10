package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;

import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    @Query(value = "select * from lemma t where t.lemma = :lemma and t.site_id = :siteId for update", nativeQuery = true)
    Lemma lemmaExist(String lemma, Integer siteId);

    @Query(value = "select count(l) from Lemma l where l.siteId = :siteId")
    Integer findCountRecordBySiteId(Integer siteId);

    @Query(value = "SELECT COUNT(*) FROM lemma l WHERE l.someField = :someValue AND l.anotherField = :anotherValue", nativeQuery = true)
    Integer findCountPageByLemma(@Param("someValue") Integer someValue, @Param("anotherValue") Integer anotherValue);

    @Query(value = "select l.id from Lemma l where l.lemma = :lemma")
    Integer findIdByLemma(String lemma);

    @Query("SELECT l FROM Lemma l WHERE l.lemma = :lemma AND (:siteId IS NULL OR l.siteId = :siteId)")
    List<Lemma> findLemmasByLemmaAndSiteId(@Param("lemma") String lemma, @Param("siteId") Integer siteId);

}
