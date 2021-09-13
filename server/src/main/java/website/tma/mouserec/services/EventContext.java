package website.tma.mouserec.services;

import lombok.Builder;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

@Builder
public record EventContext(@NonNull WebSocketSession session,
                           @NonNull WebSocketMessage message) {
}
