package com.sgg.seasons;

import com.sgg.seasons.model.SeasonDao;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;

import java.util.Optional;

@Repository
public
interface SeasonRepository extends PageableRepository<SeasonDao, Long> {
    Optional<SeasonDao> findByNameIgnoreCase(String name);

    @Query(
            value = """
        SELECT s FROM SeasonDao s
        WHERE s.creator.userId = :userId
           OR EXISTS (
                SELECT 1 FROM SeasonStandingDao st
                WHERE st.season = s AND st.user.userId = :userId
           )
        ORDER BY s.startDate DESC
        """,
            countQuery = """
        SELECT COUNT(s) FROM SeasonDao s
        WHERE s.creator.userId = :userId
           OR EXISTS (
                SELECT 1 FROM SeasonStandingDao st
                WHERE st.season = s AND st.user.userId = :userId
           )
        """
    )
    Page<SeasonDao> findSeasonsForUser(Long userId, Pageable pageable);
}
