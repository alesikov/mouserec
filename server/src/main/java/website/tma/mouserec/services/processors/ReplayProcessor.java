package website.tma.mouserec.services.processors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverRecord;
import website.tma.mouserec.model.MouseEvent;
import website.tma.mouserec.services.*;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReplayProcessor implements CommandProcessor {

    private static final Duration TIMEOUT = Duration.ofMillis(500);

    private final SessionMapWrapper sessionMapWrapper;

    private final SessionService sessionService;

    private final KafkaReceiver<UUID, MouseEvent> kafkaReceiver;

    @Override
    public Flux<String> handle(EventContext ctx) {
        var wsSessionId = ctx.session().getId();
        var sessionId = sessionMapWrapper.get(wsSessionId);
        if (Objects.isNull(sessionId)) {
            return Flux.error(() -> new ProcessingException("Unknown session"));
        }

        try {
            return kafkaReceiver.receive()
                    .timeout(TIMEOUT)
                    .map(this::ackAndReturn)
                    .filter(mouseEvent -> sessionId.equals(mouseEvent.sessionId()))
                    .delayUntil(event -> Mono.delay(Duration.ofMillis(event.delay())))
                    .map(this::convert)
                    .onErrorReturn("end");
        } catch (Exception ex) {
            log.error("Failed receive events from kafka", ex);

            return Flux.error(() -> new ProcessingException("Failed to replay"));
        }
    }

    private MouseEvent ackAndReturn(ReceiverRecord<UUID, MouseEvent> record) {
        ReceiverOffset offset = record.receiverOffset();
        offset.acknowledge();

        return record.value();
    }

    private String convert(MouseEvent event) {
        log.info("Event {}", event);
        return String.format("event %s %s", event.x(), event.y());
    }

    @Override
    public boolean canHandle(EventContext ctx) {
        return "replay".equalsIgnoreCase(ctx.message().getPayloadAsText());
    }

}
