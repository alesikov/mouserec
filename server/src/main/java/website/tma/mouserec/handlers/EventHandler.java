package website.tma.mouserec.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import website.tma.mouserec.services.CommandProcessor;
import website.tma.mouserec.services.EventContext;
import website.tma.mouserec.services.ProcessingException;
import website.tma.mouserec.services.SessionService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventHandler implements WebSocketHandler {

    private final List<CommandProcessor> commandProcessors;

    private final SessionService sessionService;

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        var result = session
                .receive()
                .concatMap(message -> process(EventContext.builder()
                        .session(session)
                        .message(message)
                        .build()));

        return session.send(result);
    }

    private Flux<WebSocketMessage> process(EventContext ctx) {
        return Flux.fromStream(commandProcessors.stream())
                .filter(cmdProcessor -> cmdProcessor.canHandle(ctx))
                .next()
                .flatMapMany(cmdProcessor -> cmdProcessor.handle(ctx))
                .onErrorResume(this::mapError)
                .map(ctx.session()::textMessage);
    }

    private Mono<String> mapError(Throwable err) {
        return (err instanceof ProcessingException pEx)
                ? Mono.just(pEx.getMessage())
                : Mono.empty();
    }
}
