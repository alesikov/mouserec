package website.tma.mouserec.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import website.tma.mouserec.model.Event;
import website.tma.mouserec.repositories.EventRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository repository;

    public Flux<Event> listAll() {
        return repository.findAll();
    }

    public Mono<Event> save(Event event) {
        return repository.save(event);
    }

    public Flux<Event> findBy(UUID sessionId) {
        return repository.findBySessionIdEquals(sessionId);
    }

}
