package website.tma.mouserec.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import website.tma.mouserec.model.Session;
import website.tma.mouserec.repositories.SessionRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository repository;

    public Flux<Session> listAll() {
        return repository.findAll();
    }

    public Mono<Session> startNew() {
        return repository.save(Session.builder()
                .startedAt(Instant.now())
                .build());
    }

    public Mono<Session> findBy(UUID id) {
        return repository.findById(id);
    }

    public Mono<Void> stop(UUID id) {
        return repository.updateStopped(id);
    }

}
