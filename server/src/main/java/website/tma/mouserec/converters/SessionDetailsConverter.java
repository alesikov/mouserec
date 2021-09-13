package website.tma.mouserec.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import website.tma.mouserec.api.queries.SessionDetails;
import website.tma.mouserec.model.Session;

import java.util.Optional;

import static java.time.ZoneOffset.UTC;

@Component
public class SessionDetailsConverter implements Converter<Session, SessionDetails> {

    @Nullable
    public SessionDetails convert(Session source) {
        var builder = SessionDetails.builder()
                .id(source.getId())
                .startedAt(source.getStartedAt().atOffset(UTC));

        Optional.ofNullable(source.getStoppedAt())
                .map(instant -> instant.atOffset(UTC))
                .ifPresent(builder::stoppedAt);

        return builder.build();
    }
}
