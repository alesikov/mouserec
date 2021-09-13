package website.tma.mouserec.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import website.tma.mouserec.handlers.SessionHandler;

@EnableWebFlux
@Configuration
@RequiredArgsConstructor
public class RoutesConfig {

    private final SessionHandler sessionHandler;

    @Bean
    public RouterFunction<ServerResponse> sessionRoutes() {
        return RouterFunctions.route()
                .GET("/sessions", sessionHandler::listSessions)
                .build();
    }

}

