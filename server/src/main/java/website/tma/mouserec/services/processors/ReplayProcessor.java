package website.tma.mouserec.services.processors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import website.tma.mouserec.model.Event;
import website.tma.mouserec.services.*;

import java.time.Duration;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReplayProcessor implements CommandProcessor {

    private static final Duration TIMEOUT = Duration.ofMillis(500);

    private final SessionMapWrapper sessionMapWrapper;

    private final SessionService sessionService;

    private final EventService eventService;

    @Override
    public Flux<String> handle(EventContext ctx) {
        var wsSessionId = ctx.session().getId();
        var sessionId = sessionMapWrapper.get(wsSessionId);
        if (Objects.isNull(sessionId)) {
            return Flux.error(() -> new ProcessingException("Unknown session"));
        }

        try {
            return eventService.findBy(sessionId)
                    .delayUntil(event -> Mono.delay(Duration.ofMillis(event.getDelay())))
                    .map(this::convert)
                    .concatWith(Flux.just("end"));
        } catch (Exception ex) {
            log.error("Failed receive events", ex);

            return Flux.error(() -> new ProcessingException("Failed to replay"));
        }
    }

    private String convert(Event event) {
        return String.format("event %s %s", event.getX(), event.getY());
    }

    @Override
    public boolean canHandle(EventContext ctx) {
        return "replay".equalsIgnoreCase(ctx.message().getPayloadAsText());
    }

}
