package website.tma.mouserec.services.processors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import website.tma.mouserec.model.Session;
import website.tma.mouserec.services.*;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class StopProcessor implements CommandProcessor {

    private final SessionMapWrapper sessionMapWrapper;

    private final SessionService sessionService;

    @Override
    public Flux<String> handle(EventContext ctx) {
        var wsSessionId = ctx.session().getId();

        var sessionId = sessionMapWrapper.get(wsSessionId);
        if (Objects.isNull(sessionId)) {
            return Flux.error(() -> new ProcessingException("Unknown session"));
        }

        return sessionService.findBy(sessionId)
                .flatMapMany(this::tryToStop);
    }

    public Flux<String> tryToStop(Session session) {
        if (Objects.isNull(session.getStoppedAt())) {
            return sessionService.stop(session.getId())
                    .thenMany(Flux.just("stopped"));
        } else {
            return Flux.error(() -> new ProcessingException("Session already stopped"));
        }
    }

    @Override
    public boolean canHandle(EventContext ctx) {
        return "stop".equalsIgnoreCase(ctx.message().getPayloadAsText());
    }
}
