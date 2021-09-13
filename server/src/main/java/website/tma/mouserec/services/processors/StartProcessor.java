package website.tma.mouserec.services.processors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import website.tma.mouserec.model.Session;
import website.tma.mouserec.services.*;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StartProcessor implements CommandProcessor {

    private final SessionMapWrapper sessionMapWrapper;

    private final SessionService sessionService;

    @Override
    public Flux<String> handle(EventContext ctx) {
        var wsSessionId = ctx.session().getId();

        if (sessionMapWrapper.containsKey(wsSessionId)) {
            return Flux.error(() -> new ProcessingException("Session was already started"));
        }

        return sessionService.startNew()
                .mapNotNull(session -> update(wsSessionId, session))
                .flux()
                .map(UUID::toString);
    }

    private UUID update(String wsSessionId, Session session) {
        sessionMapWrapper.put(wsSessionId, session.getId());
        return session.getId();
    }

    @Override
    public boolean canHandle(EventContext ctx) {
        return "start".equalsIgnoreCase(ctx.message().getPayloadAsText());
    }
}
