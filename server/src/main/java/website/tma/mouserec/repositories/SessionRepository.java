package website.tma.mouserec.repositories;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import website.tma.mouserec.model.Session;

import java.util.UUID;

@Repository
public interface SessionRepository extends R2dbcRepository<Session, UUID> {

    @Modifying
    @Query("UPDATE session SET stopped_at=NOW() WHERE id=:id")
    Mono<Void> updateStopped(@Param("id") UUID id);
}
