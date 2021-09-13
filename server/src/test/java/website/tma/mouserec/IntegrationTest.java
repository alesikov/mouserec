package website.tma.mouserec;

import org.assertj.core.data.Index;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import website.tma.mouserec.api.queries.SessionDetails;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = "classpath:postgres/sessions.sql")
@Sql(executionPhase = AFTER_TEST_METHOD, scripts = "classpath:postgres/clear_all.sql")
public class IntegrationTest extends IntegrationTestBase {

    private static final Duration TIMEOUT = Duration.ofMillis(5000);

    private static final Duration ELEMENT_INTERVAL = Duration.ofMillis(300);

    @Autowired
    private WebTestClient webTestClient;

    @LocalServerPort
    private String port;

    private final WebSocketClient client = new ReactorNettyWebSocketClient();

    @Test
    void givenExistingSession_whenListed_thenOk() {
        assertSessionList(2);
    }

    @Test
    void givenNewSession_whenStarted_thenIdIsReturned() throws Exception {
        var actualRef = new AtomicReference<String>();

        client.execute(getURL("/events"),
                        session -> session
                                .send(Mono.just("start").map(session::textMessage))
                                .then(session.receive().next().map(WebSocketMessage::getPayloadAsText))
                                .doOnNext(actualRef::set)
                                .then())
                .block(TIMEOUT);

        assertThat(actualRef).isNotNull()
                .hasValueMatching(Objects::nonNull);

        assertSessionList(3);
    }

    @Test
    void givenStartedSession_whenStartedAgain_thenErrorIsReturned() throws Exception {
        var payload = Flux.fromStream(Stream.of("start", "start"))
                .delayElements(ELEMENT_INTERVAL);
        var actualRef = new AtomicReference<List<String>>();

        client.execute(getURL("/events"),
                        session -> session
                                .send(payload.map(session::textMessage))
                                .thenMany(session.receive()
                                        .take(2)
                                        .map(WebSocketMessage::getPayloadAsText))
                                .collectList()
                                .doOnNext(actualRef::set)
                                .then())
                .block(TIMEOUT);

        assertThat(actualRef).isNotNull()
                .hasValueSatisfying(list ->
                        assertThat(list).hasSize(2)
                                .contains("Session was already started", Index.atIndex(1)));
    }

    @Test
    void givenNonStartedSession_whenStopped_thenErrorIsReturned() throws Exception {
        var actualRef = new AtomicReference<String>();

        client.execute(getURL("/events"),
                        session -> session
                                .send(Mono.just("stop").map(session::textMessage))
                                .then(session.receive()
                                        .next()
                                        .map(WebSocketMessage::getPayloadAsText))
                                .doOnNext(actualRef::set)
                                .then())
                .block(TIMEOUT);

        assertThat(actualRef).isNotNull()
                .hasValueSatisfying(val -> assertThat(val).isEqualTo("Unknown session"));
    }

    @Test
    void givenNewSession_whenStopped_thenErrorIsReturned() throws Exception {
        var actualRef = new AtomicReference<String>();

        client.execute(getURL("/events"),
                        session -> session
                                .send(Mono.just("stop").map(session::textMessage))
                                .then(session.receive()
                                        .next()
                                        .map(WebSocketMessage::getPayloadAsText))
                                .doOnNext(actualRef::set)
                                .then())
                .block(TIMEOUT);

        assertThat(actualRef).isNotNull()
                .hasValueSatisfying(val -> assertThat(val).isEqualTo("Unknown session"));
    }

    @Test
    void givenNewSession_whenStartedAndStopped_thenOk() throws Exception {
        var payload = Flux.fromStream(Stream.of("start", "stop"))
                .delayElements(ELEMENT_INTERVAL);
        var actualRef = new AtomicReference<List<String>>();

        client.execute(getURL("/events"),
                        session -> session
                                .send(payload.map(session::textMessage))
                                .thenMany(session.receive()
                                        .take(2)
                                        .map(WebSocketMessage::getPayloadAsText))
                                .collectList()
                                .doOnNext(actualRef::set)
                                .then())
                .block(TIMEOUT);

        assertThat(actualRef).isNotNull()
                .hasValueSatisfying(list ->
                        assertThat(list).hasSize(2)
                                .contains("stopped", Index.atIndex(1)));

        assertSessionList(3);
    }

    @Test
    void givenValidScenario_whenRun_thenEverythingIsOk() throws Exception {
        var payload = Flux.fromStream(Stream.of(
                        "start",
                        "persist 123 321",
                        "persist 651 1",
                        "persist 3 14",
                        "stop",
                        "replay"))
                .delayElements(ELEMENT_INTERVAL);
        var actualRef = new AtomicReference<List<String>>();

        client.execute(getURL("/events"),
                        session -> session
                                .send(payload.map(session::textMessage))
                                .thenMany(session.receive()
                                        .take(6)
                                        .map(WebSocketMessage::getPayloadAsText)
                                        .log()
                                )
                                .collectList()
                                .doOnNext(actualRef::set)
                                .then())
                .block(TIMEOUT);

        assertThat(actualRef).isNotNull()
                .hasValueSatisfying(list ->
                        assertThat(list).hasSize(6)
                                .contains("stopped", Index.atIndex(1))
                                .contains("event 123 321", Index.atIndex(2))
                                .contains("event 651 1", Index.atIndex(3))
                                .contains("event 3 14", Index.atIndex(4))
                                .contains("end", Index.atIndex(5)));

        assertSessionList(3);
    }

    private void assertSessionList(int count) {
        var result = webTestClient.get()
                .uri("/sessions")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<SessionDetails>>() {
                })
                .returnResult()
                .getResponseBody();

        assertThat(result)
                .isNotNull()
                .hasSize(count)
                .anySatisfy(sessionDetails ->
                        assertThat(sessionDetails)
                                .hasFieldOrProperty("id")
                                .hasFieldOrProperty("startedAt"));
    }

    private URI getURL(String path) throws URISyntaxException {
        return new URI("ws://localhost:" + this.port + path);
    }

}
