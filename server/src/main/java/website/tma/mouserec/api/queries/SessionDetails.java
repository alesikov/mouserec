package website.tma.mouserec.api.queries;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

@Builder
@JsonSerialize
@JsonAutoDetect(fieldVisibility = ANY)
public record SessionDetails(@NonNull UUID id,
                             @NonNull OffsetDateTime startedAt,
                             @Nullable OffsetDateTime stoppedAt) {

}
