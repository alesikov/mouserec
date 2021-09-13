package website.tma.mouserec.services;

import reactor.core.publisher.Flux;

public interface CommandProcessor {

    Flux<String> handle(EventContext ctx);

    boolean canHandle(EventContext ctx);
}
