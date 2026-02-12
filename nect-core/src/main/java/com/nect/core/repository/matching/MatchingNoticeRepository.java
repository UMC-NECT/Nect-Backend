package com.nect.core.repository.matching;

import com.nect.core.entity.matching.MatchingNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchingNoticeRepository extends JpaRepository<MatchingNotice, Long> {

    @Query("""
        select mn
        from MatchingNotice mn
        order by mn.sortOrder asc
    """)
    List<MatchingNotice> findAllOrderBySortOrder();
}
