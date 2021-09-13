package website.tma.mouserec.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import website.tma.mouserec.api.queries.SessionDetails;
import website.tma.mouserec.converters.SessionDetailsConverter;
import website.tma.mouserec.services.SessionService;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
@RequiredArgsConstructor
public class SessionHandler {

    private final SessionService sessionService;

    private final SessionDetailsConverter sessionDetailsConverter;

    public Mono<ServerResponse> listSessions(ServerRequest request) {
        var sessions = sessionService.listAll()
                .map(sessionDetailsConverter::convert);

        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(sessions, SessionDetails.class);
    }

}
