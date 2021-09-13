package website.tma.mouserec.services.processors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import website.tma.mouserec.model.Event;
import website.tma.mouserec.services.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersistProcessor implements CommandProcessor {

    private final SessionMapWrapper sessionMapWrapper;

    private final ConcurrentHashMap<UUID, Instant> sessionToLastEvent = new ConcurrentHashMap<>();

    private final EventService eventService;

    @Override
    public Flux<String> handle(EventContext ctx) {
        var wsSessionId = ctx.session().getId();
        var sessionId = sessionMapWrapper.get(wsSessionId);
        if (Objects.isNull(sessionId)) {
            return Flux.error(() -> new ProcessingException("Unknown session"));
        }

        var msgParts = ctx.message().getPayloadAsText().split(" ");
        try {
            var now = Instant.now();
            var lastEventSubmittedAt = Optional.ofNullable(sessionToLastEvent.get(sessionId))
                    .orElse(now);
            sessionToLastEvent.put(sessionId, now);

            var event = Event.builder()
                    .sessionId(sessionId)
                    .x(Integer.parseInt(msgParts[1]))
                    .y(Integer.parseInt(msgParts[2]))
                    .delay(Duration.between(lastEventSubmittedAt, now).toMillis())
                    .build();

            return eventService.save(event)
                    .thenMany(Flux.empty());
        } catch (Exception ex) {
            log.error("Failed to parse event", ex);

            return Flux.error(() -> new ProcessingException("Unknown command"));
        }
    }

    @Override
    public boolean canHandle(EventContext ctx) {
        return Optional.of(ctx.message().getPayloadAsText())
                .map(msg -> msg.startsWith("persist"))
                .orElse(false);
    }

}
