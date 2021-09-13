package website.tma.mouserec.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;

import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

@Builder
@JsonSerialize
@JsonAutoDetect(fieldVisibility = ANY)
public record MouseEvent(UUID sessionId,
                         int x,
                         int y,
                         Long delay) {

}
