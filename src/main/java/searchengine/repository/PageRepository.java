package searchengine.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import searchengine.model.SitePage;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    @Query(value = "select * from page t where t.site_id = :siteId and t.path = :path limit 1", nativeQuery = true)
    Page findPageBySiteIdAndPath(@Param("path") String path, @Param("siteId") Integer siteId);

    @Query(value = "select count(p) from Page p where p.siteId = :siteId")
    Integer findCountRecordBySiteId(@Param("siteId") Integer siteId);

    @Query(value = "SELECT COUNT(p) FROM Page p WHERE (:siteId IS NULL OR p.siteId = :siteId)")
    Integer getCountPages(@Param("siteId") Integer siteId);
}
