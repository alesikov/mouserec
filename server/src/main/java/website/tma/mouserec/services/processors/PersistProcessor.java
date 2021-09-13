package website.tma.mouserec.services.processors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import website.tma.mouserec.model.MouseEvent;
import website.tma.mouserec.services.CommandProcessor;
import website.tma.mouserec.services.EventContext;
import website.tma.mouserec.services.ProcessingException;
import website.tma.mouserec.services.SessionMapWrapper;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static website.tma.mouserec.configs.KafkaConfig.TOPIC_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersistProcessor implements CommandProcessor {

    private final SessionMapWrapper sessionMapWrapper;

    private final ConcurrentHashMap<UUID, Instant> sessionToLastEvent = new ConcurrentHashMap<>();

    private final KafkaSender<UUID, MouseEvent> kafkaSender;

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
            var lastEventSubmittedAt = Optional.ofNullable(sessionToLastEvent.get(sessionId)).orElse(now);
            sessionToLastEvent.put(sessionId, now);

            var event = MouseEvent.builder()
                    .sessionId(sessionId)
                    .x(Integer.valueOf(msgParts[1]))
                    .y(Integer.valueOf(msgParts[2]))
                    .delay(Duration.between(lastEventSubmittedAt, now).toMillis())
                    .build();

            log.info("Persisting {}", event);

            return send(sessionId, event);
        } catch (Exception ex) {
            log.error("Failed to parse event", ex);

            return Flux.error(() -> new ProcessingException("Unknown command"));
        }
    }

    private Flux<String> send(UUID sessionId, MouseEvent event) {
        return kafkaSender.send(Mono.just(SenderRecord.create(
                        TOPIC_NAME,
                        null,
                        null,
                        UUID.randomUUID(),
                        event,
                        null)))
                .thenMany(Flux.empty());
    }

    @Override
    public boolean canHandle(EventContext ctx) {
        return Optional.of(ctx.message().getPayloadAsText())
                .map(msg -> msg.startsWith("persist"))
                .orElse(false);
    }

}
