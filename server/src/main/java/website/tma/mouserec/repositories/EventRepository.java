package website.tma.mouserec.repositories;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import website.tma.mouserec.model.Event;

import java.util.UUID;

@Repository
public interface EventRepository extends R2dbcRepository<Event, UUID> {

    Flux<Event> findBySessionIdEquals(UUID sessionId);

}
